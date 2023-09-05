package com.nelayanku.apps.act.admin

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.nelayanku.apps.R
import com.nelayanku.apps.model.Informasi
import com.nelayanku.apps.model.Product
import com.nelayanku.apps.redirect.AdminActivity
import com.nelayanku.apps.redirect.SellerActivity
import com.nelayanku.apps.tools.Const
import com.nelayanku.apps.tools.Const.PATH_COLLECTION
import com.nelayanku.apps.tools.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID


class InfoEditActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var imagesList = mutableListOf<Uri?>()

    private lateinit var btnUploadCover: LinearLayout
    private lateinit var coverReplace: ImageView
    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var buttonAddProduct: Button
    private lateinit var btnBack: ImageButton
    private lateinit var etNama: EditText
    private lateinit var etDesc: EditText
    // Constants for request codes
    private val REQUEST_CODE_COVER = 1

    lateinit var productId : String
    lateinit var documentId : String
    lateinit var progressDialog2 : ProgressDialog

    val TAG : String = "Edit"
    var tidakganti = false
    lateinit var coverImage: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_edit)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        productId = intent.getStringExtra("productId") ?:""
        documentId = intent.getStringExtra("documentId") ?:""
        Log.d("ID",productId)
        imagesList = mutableListOf(null)
        initial()
        btnOther()
        progressDialog2 = ProgressDialog(this@InfoEditActivity)
        progressDialog2.setMessage("Mengambil data produk...")
        progressDialog2.setCancelable(false)
        progressDialog2.show()
        getData(productId)
    }
    fun getData(productid : String){
        if (productid != null) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val querySnapshot = firestore.collection("info")
                        .whereEqualTo("infoId", productId)
                        .get()
                        .await()

                    if (!querySnapshot.isEmpty) {
                        val documentSnapshot = querySnapshot.documents[0]
                        val product = documentSnapshot.toObject(Informasi::class.java)

                        // Rest of the code to update UI and show product details
                        if (product != null) {
                            val productName = product.title
                            val productDescription = product.description
                             coverImage = product.coverImage.toString()
                            // Update UI with the retrieved product details
                            withContext(Dispatchers.Main) {
                                etNama.setText(productName)
                                etDesc.setText(productDescription)
                                tidakganti=true
                                // Load gambar cover dengan Glide
                                Glide.with(this@InfoEditActivity)
                                    .load(coverImage)
                                    .placeholder(R.drawable.no_image) // Gambar placeholder jika tidak ada gambar
                                    .into(coverReplace)
                                // Load gambar produk dan tampilkan tombol delete jika gambar tersedia
                                // Hide progress dialog
                                progressDialog2.dismiss()
                            }
                        } else {
                            // Product data not found
                            // Hide progress dialog
                            progressDialog2.dismiss()
                        }
                    } else {
                        // No matching products found
                        // Hide progress dialog
                        progressDialog2.dismiss()
                    }
                } catch (e: Exception) {
                    // Common error handling
                    // Hide progress dialog
                    progressDialog2.dismiss()
                    Log.e(TAG, "Error: ${e.message}")
                }
            }
        }else{
            progressDialog2.hide()
            Log.d("Tidak ada data",productId)
        }
    }
    fun initial(){
        btnUploadCover = findViewById(R.id.btnCover)

        coordinatorLayout = findViewById(R.id.coordinator)
        coverReplace = findViewById(R.id.coverReplace)

        btnBack = findViewById(R.id.btnBack)
        buttonAddProduct = findViewById(R.id.buttonAddProduct)
        etNama = findViewById(R.id.editTextProductName)
        etDesc = findViewById(R.id.editTextProductDescription)
    }
    fun btnOther(){
        btnUploadCover.setOnClickListener {
            // Buka galeri untuk memilih foto sampul
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_COVER)
        }
        buttonAddProduct.setOnClickListener {
            val productName = etNama.text.toString()
            val productDescription = etDesc.text.toString()

            // Periksa apakah semua field yang diperlukan terisi
            if (productName.isNotEmpty() && productDescription.isNotEmpty()) {
                // Tampilkan dialog progress saat mengunggah
                val progressDialog = ProgressDialog(this@InfoEditActivity)
                progressDialog.setMessage("Mengunggah informasi...")
                progressDialog.setCancelable(false)
                progressDialog.show()

                // Kompres dan unggah gambar di latar belakang
                lifecycleScope.launch(Dispatchers.IO) {
                    // Kompres dan unggah foto sampul
                    //jika tidakganti = false
                    if(tidakganti == false){
                        val coverImageUrl = uploadImage(imagesList[0])
                        // Tambahkan detail produk ke Firestore
                        // Tambahkan detail produk ke Firestore
                        editProductToFirestore(
                            documentId, // Isi dengan productId yang ingin diubah
                            productName,
                            productDescription,
                            coverImageUrl
                        )
                    }else{
                        editProductToFirestore(
                            documentId, // Isi dengan productId yang ingin diubah
                            productName,
                            productDescription,
                            coverImage
                        )
                    }
                    progressDialog.dismiss()
                }
            } else {
                Snackbar.make(
                    coordinatorLayout,
                    "Harap isi semua field",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
    private suspend fun uploadImage(imageUri: Uri?): String {
        val compressedImageUri = compressImage(this,imageUri)
        val storageReference = FirebaseStorage.getInstance().getReference("info_images")
        val imageFileName = UUID.randomUUID().toString()
        val imageRef = storageReference.child("$imageFileName.jpg")

        return try {
            val uploadTask = imageRef.putFile(compressedImageUri).await()
            val imageUrl = imageRef.downloadUrl.await().toString()
            imageUrl
        } catch (e: Exception) {
            throw e
        }
    }
    private suspend fun compressImage(context: Context, imageUri: Uri?): Uri {
        val originalBitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
        val compressedBitmap = ImageUtils.compressBitmap(originalBitmap)

        val compressedImageUri = ImageUtils.createTempImageFile(context)
        val outputStream = context.contentResolver.openOutputStream(compressedImageUri)
        compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        outputStream?.close()

        return compressedImageUri
    }
    private fun editProductToFirestore(
        productId: String,
        productName: String,
        productDescription: String,
        coverImageUrl: String
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val uid = currentUser?.uid ?: ""

        val productData = hashMapOf(
            "title" to productName,
            "description" to productDescription,
            "coverImage" to coverImageUrl,
        )

        val db = FirebaseFirestore.getInstance()

        // Update the product data in Firestore
        db.collection("info")
            .document(productId) // Gunakan productId yang ada untuk merujuk dokumen yang ingin diubah
            .update(productData as Map<String, Any>)
            .addOnSuccessListener {
                // Product updated successfully
                Snackbar.make(coordinatorLayout, "Informasi berhasil diubah", Snackbar.LENGTH_SHORT).show()
                // Redirect to SellerActivity fragment home
                val intent = Intent(this, AdminActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                intent.putExtra("fragment", "info")
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                // Error occurred while updating product
                Snackbar.make(coordinatorLayout, "Gagal mengubah informasi: ${e.message}", Snackbar.LENGTH_SHORT).show()
            }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_COVER -> {
                    // Ambil URI gambar yang dipilih dari galeri
                    val selectedImageUri = data?.data
                    // Tampilkan gambar yang dipilih ke imageView coverReplace
                    coverReplace.setImageURI(selectedImageUri)
                    // Simpan URI gambar ke dalam list untuk penggunaan nanti
                    imagesList[0] = selectedImageUri
                }
            }
        }
    }

}

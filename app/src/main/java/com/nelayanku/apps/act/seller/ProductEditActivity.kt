package com.nelayanku.apps.act.seller

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
import com.nelayanku.apps.model.Product
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


class ProductEditActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var imagesList = mutableListOf<Uri?>()

    private lateinit var btnUploadCover: LinearLayout
    private lateinit var btn1: LinearLayout
    private lateinit var btn2: LinearLayout
    private lateinit var btn3: LinearLayout
    private lateinit var btn4: LinearLayout
    private lateinit var btn5: LinearLayout
    private lateinit var btn6: LinearLayout
    private lateinit var coverReplace: ImageView
    private lateinit var img1: ImageView
    private lateinit var img2: ImageView
    private lateinit var img3: ImageView
    private lateinit var img4: ImageView
    private lateinit var img5: ImageView
    private lateinit var img6: ImageView
    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var del1: LinearLayout
    private lateinit var del2: LinearLayout
    private lateinit var del3: LinearLayout
    private lateinit var del4: LinearLayout
    private lateinit var del5: LinearLayout
    private lateinit var del6: LinearLayout
    private lateinit var buttonAddProduct: Button
    private lateinit var btnBack: ImageButton
    private lateinit var etNama: EditText
    private lateinit var etHarga: EditText
    private lateinit var etDesc: EditText
    // Constants for request codes
    private val REQUEST_CODE_COVER = 1
    // Constants for request codes
    private val REQUEST_CODE_PRODUCT_1 = 2
    private val REQUEST_CODE_PRODUCT_2 = 3
    private val REQUEST_CODE_PRODUCT_3 = 4
    private val REQUEST_CODE_PRODUCT_4 = 5
    private val REQUEST_CODE_PRODUCT_5 = 6
    private val REQUEST_CODE_PRODUCT_6 = 7

    lateinit var productId : String
    lateinit var progressDialog2 : ProgressDialog

    val TAG : String = "Edit"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_edit)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        productId = intent.getStringExtra("productId") ?:""
        Log.d("ID",productId)
        imagesList = mutableListOf(null, null, null, null, null, null,null)
        initial()
        btnImage()
        btnOther()
        btnDel()
        progressDialog2 = ProgressDialog(this@ProductEditActivity)
        progressDialog2.setMessage("Mengambil data produk...")
        progressDialog2.setCancelable(false)
        progressDialog2.show()
        getData(productId)
    }
    fun getData(productid : String){
        if (productid != null) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val querySnapshot = firestore.collection(PATH_COLLECTION)
                        .whereEqualTo("productId", productId)
                        .get()
                        .await()

                    if (!querySnapshot.isEmpty) {
                        val documentSnapshot = querySnapshot.documents[0]
                        val product = documentSnapshot.toObject(Product::class.java)

                        // Rest of the code to update UI and show product details
                        if (product != null) {
                            val productName = product.name
                            val productPrice = product.price
                            val productDescription = product.description
                            val coverImage = product.coverImage
                            val productImages = product.images

                            // Update UI with the retrieved product details
                            withContext(Dispatchers.Main) {
                                etNama.setText(productName)
                                etHarga.setText(productPrice)
                                etDesc.setText(productDescription)

                                // Load gambar cover dengan Glide
                                Glide.with(this@ProductEditActivity)
                                    .load(coverImage)
                                    .placeholder(R.drawable.no_image) // Gambar placeholder jika tidak ada gambar
                                    .into(coverReplace)
                                // Load gambar produk dan tampilkan tombol delete jika gambar tersedia
                                val images = productImages ?: emptyList()
                                for ((index, imageUrl) in images.withIndex()) {
                                    val imageView = when (index) {
                                        0 -> img1
                                        1 -> img2
                                        2 -> img3
                                        3 -> img4
                                        4 -> img5
                                        5 -> img6
                                        else -> null
                                    }

                                    imageView?.let {
                                        Glide.with(this@ProductEditActivity)
                                            .load(imageUrl)
                                            .placeholder(R.drawable.no_image) // Gambar placeholder jika tidak ada gambar
                                            .into(it)
                                    }
                                }
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
        btn1 = findViewById(R.id.btn1)
        btn2 = findViewById(R.id.btn2)
        btn3 = findViewById(R.id.btn3)
        btn4 = findViewById(R.id.btn4)
        btn5 = findViewById(R.id.btn5)
        btn6 = findViewById(R.id.btn6)
        del1 = findViewById(R.id.del1)
        del2 = findViewById(R.id.del2)
        del3 = findViewById(R.id.del3)
        del4 = findViewById(R.id.del4)
        del5 = findViewById(R.id.del5)
        del6 = findViewById(R.id.del6)
        coordinatorLayout = findViewById(R.id.coordinator)
        coverReplace = findViewById(R.id.coverReplace)
        img1 = findViewById(R.id.img1)
        img2 = findViewById(R.id.img2)
        img3 = findViewById(R.id.img3)
        img4 = findViewById(R.id.img4)
        img5 = findViewById(R.id.img5)
        img6 = findViewById(R.id.img6)
        btnBack = findViewById(R.id.btnBack)
        buttonAddProduct = findViewById(R.id.buttonAddProduct)
        etNama = findViewById(R.id.editTextProductName)
        etHarga = findViewById(R.id.editTextProductPrice)
        etDesc = findViewById(R.id.editTextProductDescription)
    }
    fun btnImage(){
        btn1.setOnClickListener {
            // Buka galeri untuk memilih foto produk 1
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_PRODUCT_1)
        }

        // Implementasikan listener klik untuk btn2 hingga btn6 dengan cara yang sama
        btn2.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_PRODUCT_2)
        }

        btn3.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_PRODUCT_3)
        }

        btn4.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_PRODUCT_4)
        }

        btn5.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_PRODUCT_5)
        }

        btn6.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_PRODUCT_6)
        }
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
            val productPrice = etHarga.text.toString()
            val productDescription = etDesc.text.toString()

            // Periksa apakah semua field yang diperlukan terisi
            if (productName.isNotEmpty() && productPrice.isNotEmpty() && productDescription.isNotEmpty()) {
                // Tampilkan dialog progress saat mengunggah
                val progressDialog = ProgressDialog(this@ProductEditActivity)
                progressDialog.setMessage("Mengunggah produk...")
                progressDialog.setCancelable(false)
                progressDialog.show()

                // Kompres dan unggah gambar di latar belakang
                lifecycleScope.launch(Dispatchers.IO) {
                    // Kompres dan unggah foto sampul
                    val coverImageUrl = uploadImage(imagesList[0])

                    // Kompres dan unggah foto produk
                    val productImageUrls = mutableListOf<String>()
                    for (i in 1 until imagesList.size) {
                        val imageUri = imagesList[i]
                        if (imageUri != null) {
                            val imageUrl = uploadImage(imageUri)
                            productImageUrls.add(imageUrl)
                        }
                    }


                    // Tambahkan detail produk ke Firestore
                    // Tambahkan detail produk ke Firestore
                    editProductToFirestore(
                        productId, // Isi dengan productId yang ingin diubah
                        productName,
                        productPrice,
                        productDescription,
                        coverImageUrl,
                        productImageUrls,
                    )

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
    fun btnDel(){
        del1.setOnClickListener {
            // Reset gambar preview ke no_image
            img1.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.no_image))
            // Hapus gambar dari daftar
            imagesList[1] = null
            // Sembunyikan tombol delete
            del1.visibility = View.GONE
        }
        del2.setOnClickListener {
            // Reset gambar preview ke no_image
            img2.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.no_image))
            // Hapus gambar dari daftar
            imagesList[2] = null
            // Sembunyikan tombol delete
            del2.visibility = View.GONE
        }
        del3.setOnClickListener {
            // Reset gambar preview ke no_image
            img3.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.no_image))
            // Hapus gambar dari daftar
            imagesList[3] = null
            // Sembunyikan tombol delete
            del3.visibility = View.GONE
        }
        del3.setOnClickListener {
            // Reset gambar preview ke no_image
            img3.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.no_image))
            // Hapus gambar dari daftar
            imagesList[3] = null
            // Sembunyikan tombol delete
            del3.visibility = View.GONE
        }
        del4.setOnClickListener {
            // Reset gambar preview ke no_image
            img4.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.no_image))
            // Hapus gambar dari daftar
            imagesList[4] = null
            // Sembunyikan tombol delete
            del4.visibility = View.GONE
        }
        del5.setOnClickListener {
            // Reset gambar preview ke no_image
            img5.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.no_image))
            // Hapus gambar dari daftar
            imagesList[5] = null
            // Sembunyikan tombol delete
            del5.visibility = View.GONE
        }
        del6.setOnClickListener {
            // Reset gambar preview ke no_image
            img6.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.no_image))
            // Hapus gambar dari daftar
            imagesList[6] = null
            // Sembunyikan tombol delete
            del6.visibility = View.GONE
        }
    }
    private suspend fun uploadImage(imageUri: Uri?): String {
        val compressedImageUri = compressImage(this,imageUri)
        val storageReference = FirebaseStorage.getInstance().getReference("product_images")
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
        productPrice: String,
        productDescription: String,
        coverImageUrl: String,
        productImageUrls: List<String>
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val uid = currentUser?.uid ?: ""

        val productData = hashMapOf(
            "uid" to uid,
            "name" to productName,
            "price" to productPrice,
            "description" to productDescription,
            "coverImage" to coverImageUrl,
            "images" to productImageUrls,
            // Anda mungkin tidak perlu mengubah status, stok, atau created_at
        )

        val db = FirebaseFirestore.getInstance()

        // Update the product data in Firestore
        db.collection("products")
            .document(productId) // Gunakan productId yang ada untuk merujuk dokumen yang ingin diubah
            .update(productData)
            .addOnSuccessListener {
                // Product updated successfully
                Snackbar.make(coordinatorLayout, "Produk berhasil diubah", Snackbar.LENGTH_SHORT).show()
                // Redirect to SellerActivity fragment home
                val intent = Intent(this, SellerActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                // Error occurred while updating product
                Snackbar.make(coordinatorLayout, "Gagal mengubah produk: ${e.message}", Snackbar.LENGTH_SHORT).show()
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
                REQUEST_CODE_PRODUCT_1 -> {
                    val selectedImageUri = data?.data
                    img1.setImageURI(selectedImageUri)
                    imagesList[1] = selectedImageUri
                    del1.visibility = View.VISIBLE
                }
                REQUEST_CODE_PRODUCT_2 -> {
                    val selectedImageUri = data?.data
                    img2.setImageURI(selectedImageUri)
                    imagesList[2] = selectedImageUri
                    del2.visibility = View.VISIBLE
                }
                REQUEST_CODE_PRODUCT_3 -> {
                    val selectedImageUri = data?.data
                    img3.setImageURI(selectedImageUri)
                    imagesList[3] = selectedImageUri
                    del3.visibility = View.VISIBLE
                }
                REQUEST_CODE_PRODUCT_4 -> {
                    val selectedImageUri = data?.data
                    img4.setImageURI(selectedImageUri)
                    imagesList[4] = selectedImageUri
                    del4.visibility = View.VISIBLE
                }
                REQUEST_CODE_PRODUCT_5 -> {
                    val selectedImageUri = data?.data
                    img5.setImageURI(selectedImageUri)
                    imagesList[5] = selectedImageUri
                    del5.visibility = View.VISIBLE
                }
                REQUEST_CODE_PRODUCT_6 -> {
                    val selectedImageUri = data?.data
                    img6.setImageURI(selectedImageUri)
                    imagesList[6] = selectedImageUri
                    del6.visibility = View.VISIBLE
                }
            }
        }
    }

}

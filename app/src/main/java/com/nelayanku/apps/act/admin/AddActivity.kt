package com.nelayanku.apps.act.admin

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.nelayanku.apps.R
import com.nelayanku.apps.redirect.AdminActivity
import com.nelayanku.apps.redirect.SellerActivity
import com.nelayanku.apps.tools.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDateTime
import java.util.UUID

class AddActivity : AppCompatActivity() {
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
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_admin)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        imagesList = mutableListOf(null)
        btnUploadCover = findViewById(R.id.btnCover)

        coordinatorLayout = findViewById(R.id.coordinator)
        coverReplace = findViewById(R.id.coverReplace)

        btnBack = findViewById(R.id.btnBack)
        buttonAddProduct = findViewById(R.id.buttonAddProduct)
        etNama = findViewById(R.id.editTextProductName)
        etDesc = findViewById(R.id.editTextProductDescription)
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
                val progressDialog = ProgressDialog(this@AddActivity)
                progressDialog.setMessage("Mengunggah produk...")
                progressDialog.setCancelable(false)
                progressDialog.show()

                // Kompres dan unggah gambar di latar belakang
                lifecycleScope.launch(Dispatchers.IO) {
                    // Kompres dan unggah foto sampul
                    val coverImageUrl = uploadImage(imagesList[0])
                    // Tambahkan detail produk ke Firestore
                    addProductToFirestore(
                        productName,
                        productDescription,
                        coverImageUrl
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


    @RequiresApi(Build.VERSION_CODES.O)
    private fun addProductToFirestore(
        productName: String,
        productDescription: String,
        coverImageUrl: String
    ) {
        val now = LocalDateTime.now()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val uid = currentUser?.uid ?: ""
        val productData = hashMapOf(
            "infoId" to UUID.randomUUID().toString(), // Generate unique productId
            "uid" to uid,
            "title" to productName,
            "tanggal" to now.toString(),
            "description" to productDescription,
            "coverImage" to coverImageUrl,
            "status" to "published",
            "created_at" to FieldValue.serverTimestamp() // Set created_at to server timestamp
        )

        val db = FirebaseFirestore.getInstance()

        // Add the product data to Firestore
        db.collection("info")
            .add(productData)
            .addOnSuccessListener { documentReference ->
                // Product added successfully
                Snackbar.make(coordinatorLayout, "Informasi berhasil ditambahkan", Snackbar.LENGTH_SHORT).show()
                // Redirect to SellerActivity fragment home
                val intent = Intent(this, AdminActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                intent.putExtra("fragment", "info")
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                // Error occurred while adding product
                Snackbar.make(coordinatorLayout, "Gagal menambahkan informasi : ${e.message}", Snackbar.LENGTH_SHORT).show()
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

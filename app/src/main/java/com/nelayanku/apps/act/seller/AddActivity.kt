package com.nelayanku.apps.act.seller

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
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
import com.nelayanku.apps.redirect.SellerActivity
import com.nelayanku.apps.tools.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class AddActivity : AppCompatActivity() {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        imagesList = mutableListOf(null, null, null, null, null, null,null)
        btnUploadCover = findViewById(R.id.btnCover)
        btn1 = findViewById(R.id.btn1)
        btn2 = findViewById(R.id.btn2)
        btn3 = findViewById(R.id.btn3)
        btn4 = findViewById(R.id.btn4)
        btn5 = findViewById(R.id.btn5)
        btn6 = findViewById(R.id.btn6)
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
        btnUploadCover.setOnClickListener {
            // Buka galeri untuk memilih foto sampul
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_COVER)
        }

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

        buttonAddProduct.setOnClickListener {
            val productName = etNama.text.toString()
            val productPrice = etHarga.text.toString()
            val productDescription = etDesc.text.toString()

            // Periksa apakah semua field yang diperlukan terisi
            if (productName.isNotEmpty() && productPrice.isNotEmpty() && productDescription.isNotEmpty()) {
                // Tampilkan dialog progress saat mengunggah
                val progressDialog = ProgressDialog(this@AddActivity)
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
                    addProductToFirestore(
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


    private fun addProductToFirestore(
        productName: String,
        productPrice: String,
        productDescription: String,
        coverImageUrl: String,
        productImageUrls: List<String>
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val uid = currentUser?.uid ?: ""

        val productData = hashMapOf(
            "productId" to UUID.randomUUID().toString(), // Generate unique productId
            "uid" to uid,
            "name" to productName,
            "price" to productPrice,
            "description" to productDescription,
            "coverImage" to coverImageUrl,
            "images" to productImageUrls,
            "status" to "pending",
            "stok" to 99,
            "created_at" to FieldValue.serverTimestamp() // Set created_at to server timestamp
        )

        val db = FirebaseFirestore.getInstance()

        // Add the product data to Firestore
        db.collection("products")
            .add(productData)
            .addOnSuccessListener { documentReference ->
                // Product added successfully
                Snackbar.make(coordinatorLayout, "Produk berhasil ditambahkan", Snackbar.LENGTH_SHORT).show()
                // Redirect to SellerActivity fragment home
                val intent = Intent(this, SellerActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                // Error occurred while adding product
                Snackbar.make(coordinatorLayout, "Gagal menambahkan produk: ${e.message}", Snackbar.LENGTH_SHORT).show()
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
                }
                REQUEST_CODE_PRODUCT_2 -> {
                    val selectedImageUri = data?.data
                    img2.setImageURI(selectedImageUri)
                    imagesList[2] = selectedImageUri
                }
                REQUEST_CODE_PRODUCT_3 -> {
                    val selectedImageUri = data?.data
                    img3.setImageURI(selectedImageUri)
                    imagesList[3] = selectedImageUri
                }
                REQUEST_CODE_PRODUCT_4 -> {
                    val selectedImageUri = data?.data
                    img4.setImageURI(selectedImageUri)
                    imagesList[4] = selectedImageUri
                }
                REQUEST_CODE_PRODUCT_5 -> {
                    val selectedImageUri = data?.data
                    img5.setImageURI(selectedImageUri)
                    imagesList[5] = selectedImageUri
                }
                REQUEST_CODE_PRODUCT_6 -> {
                    val selectedImageUri = data?.data
                    img6.setImageURI(selectedImageUri)
                    imagesList[6] = selectedImageUri
                }
            }
        }
    }

}

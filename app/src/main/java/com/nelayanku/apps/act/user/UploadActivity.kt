package com.nelayanku.apps.act.user

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.nelayanku.apps.R
import com.nelayanku.apps.redirect.SellerActivity
import com.nelayanku.apps.redirect.UserActivity
import com.nelayanku.apps.tools.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class UploadActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var imagesList = mutableListOf<Uri?>()
    private lateinit var btnUploadCover: LinearLayout
    private lateinit var coverReplace: ImageView
    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var buttonAddProduct: Button
    private lateinit var btnBack: ImageButton
    var idWallet=""
    var docId=""
    private val REQUEST_CODE_COVER = 1
    lateinit var progressDialog2 : ProgressDialog

    val TAG : String = "Bukti"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        imagesList = mutableListOf(null, null, null, null, null, null,null)
        btnUploadCover = findViewById(R.id.btnCover)
        coordinatorLayout = findViewById(R.id.coordinator)
        coverReplace = findViewById(R.id.coverReplace)
        btnBack = findViewById(R.id.btnBack)
        buttonAddProduct = findViewById(R.id.btnKirim)
        //ambil idwallet dari intent
        val intent = intent
        idWallet = intent.getStringExtra("idWallet").toString()
        docId = intent.getStringExtra("documentId").toString()
        btnUploadCover.setOnClickListener {
            // Buka galeri untuk memilih foto sampul
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_COVER)
        }

        buttonAddProduct.setOnClickListener {
            // Periksa apakah semua field yang diperlukan terisi
            if (docId != "" && imagesList[0] != null ) {
                // Tampilkan dialog progress saat mengunggah
                val progressDialog = ProgressDialog(this)
                progressDialog.setMessage("Mengunggah produk...")
                progressDialog.setCancelable(false)
                progressDialog.show()

                // Kompres dan unggah gambar di latar belakang
                lifecycleScope.launch(Dispatchers.IO) {
                    // Kompres dan unggah foto sampul
                    val coverImageUrl = uploadImage(imagesList[0])
                    // Tambahkan detail produk ke Firestore
                    kirimBukti(
                        docId,
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
        val storageReference = FirebaseStorage.getInstance().getReference("bukti_images")
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
    private fun kirimBukti(
        documentId: String,
        coverImageUrl: String
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val uid = currentUser?.uid ?: ""

        val productData = hashMapOf(
            "buktiTf" to coverImageUrl,
            "status" to "Diproses"
        )

        val db = FirebaseFirestore.getInstance()
        // Update the product data in Firestore
        db.collection("wallets")
            .document(documentId) // Gunakan productId yang ada untuk merujuk dokumen yang ingin diubah
            .update(productData as Map<String, Any>)
            .addOnSuccessListener {
                // Product updated successfully
                Snackbar.make(coordinatorLayout, "Berhasil mengirim bukti", Snackbar.LENGTH_SHORT).show()
                // Redirect to SellerActivity fragment home
                val intent = Intent(this, UserActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                intent.putExtra("fragment", "wallet")
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                // Error occurred while updating product
                Snackbar.make(coordinatorLayout, "Gagal mengirim bukti: ${e.message}", Snackbar.LENGTH_SHORT).show()
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
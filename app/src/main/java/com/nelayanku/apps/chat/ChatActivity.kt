package com.nelayanku.apps.chat

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.nelayanku.apps.R
import com.nelayanku.apps.adapter.chat.ChatAdapter
import com.nelayanku.apps.model.ChatModel
import com.nelayanku.apps.tools.ImageUtils
import com.nelayanku.apps.tools.insertChat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class ChatActivity : AppCompatActivity() {
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatList: MutableList<ChatModel>
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageReference: FirebaseStorage
    private lateinit var auth: FirebaseAuth
    private lateinit var rvChat: RecyclerView
    private lateinit var btnSend: ImageView
    private lateinit var etChat: EditText
    private lateinit var ivChat: ImageView
    private lateinit var shimmerContainer: ShimmerFrameLayout
    var docId = ""
    var isGambar = false
    var senderid = ""

    private lateinit var storage: FirebaseStorage
    private var imagesList = mutableListOf<Uri?>()
    private lateinit var coverReplace: ImageView
    private lateinit var lyCover: LinearLayout
    private lateinit var btnUpload: LinearLayout
    private val REQUEST_CODE_COVER = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Inisialisasi Firebase
        firestore = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        
        btnSend = findViewById(R.id.ivSend)
        etChat = findViewById(R.id.etChat)
        ivChat = findViewById(R.id.ivChat)
        coverReplace = findViewById(R.id.coverReplace)
        lyCover = findViewById(R.id.lyCover)
        btnUpload = findViewById(R.id.btnUpload)
        imagesList = mutableListOf(null)
        //hide lycover
        lyCover.visibility = LinearLayout.GONE
        shimmerContainer = findViewById(R.id.shimmerContainer)
        //docId ambil dari intent
        docId = intent.getStringExtra("documentId").toString()
        // Inisialisasi RecyclerView
        chatList = mutableListOf()
        rvChat = findViewById(R.id.rvChat)
        //ambil uid
        val currentUser = auth.currentUser
        senderid = currentUser!!.uid
        rvChat.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(context, 1)
            // set the custom adapter to the RecyclerView
            chatAdapter = ChatAdapter(
                chatList,
                context,
                senderid
            )
        }

        // Membaca pesan chat dari Firebase Firestore
        readChatData()
        rvChat.adapter = chatAdapter
        // Set listener untuk tombol Kirim
        btnSend.setOnClickListener {
            val message = etChat.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessageToFirestore(message)
                etChat.text.clear()
            }else{
                etChat.error = "Tidak boleh kosong"
            }
        }

        // Set listener untuk tombol Lampirkan Gambar
        ivChat.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_COVER)
        }
        //set listener btnUpload
        btnUpload.setOnClickListener {
            //tampilkan progressdialog saat mengunggah gambar
            val progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Mengirim gambar...")
            progressDialog.setCancelable(false)
            progressDialog.show()
            var coverImageUrl = ""
            lifecycleScope.launch(Dispatchers.IO) {
                // Kompres dan unggah foto sampul
                coverImageUrl = uploadImage(imagesList[0])
                // Switch to the main (UI) thread to update the UI
                withContext(Dispatchers.Main) {
                    isGambar=true
                    sendMessageToFirestore(coverImageUrl)
                    // Dismiss ProgressDialog on the main thread
                    progressDialog.dismiss()
                }
            }
        }
    }

    private fun readChatData() {
        //order berdasarkan tanggal dan jam terlama dahulu
        val chatReference = firestore.collection("ChatHeader").document(docId).collection("Chat")
            .orderBy("tanggal", Query.Direction.DESCENDING) // Urutkan tanggal secara menurun (terlama dahulu)
            .orderBy("jam", Query.Direction.DESCENDING)     // Kemudian urutkan jam secara menurun (terlama dahulu)
        chatReference.addSnapshotListener { value, error ->
            if (error != null) {
                Log.e("Firestore Error", error.message.toString())
                return@addSnapshotListener
            }
            if (value != null) {
                chatList.clear()
                for (document in value.documents) {
                    Log.d("Firestore Data", document.data.toString())
                    val chat = document.toObject(ChatModel::class.java)
                    if (chat != null) {
                        // Tambahkan data baru ke awal list untuk menampilkan data terbaru terlebih dahulu
                        chatList.add(0, chat)
                    }
                }

                // Panggil notifyDataSetChanged() setelah menambahkan data ke chatList
                chatAdapter.notifyDataSetChanged()

                // Scroll ke posisi teratas (data terbaru)
                rvChat.scrollToPosition(0)

                shimmerContainer.stopShimmer()
                shimmerContainer.visibility = ShimmerFrameLayout.GONE
                Log.d("Firestore Data", chatList.toString())
            }
        }
    }
    private fun sendMessageToFirestore(message: String) {
        if(message.isNotEmpty()){
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val uid = UUID.randomUUID().toString()
                // Atur zona waktu ke WIB (Waktu Indonesia Barat)
                val timeZone = TimeZone.getTimeZone("Asia/Jakarta")

                // Buat objek SimpleDateFormat dengan zona waktu yang diatur
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                dateFormat.timeZone = timeZone
                val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                timeFormat.timeZone = timeZone
                // Ambil tanggal dan jam sekarang berdasarkan zona waktu WIB
                val tanggal = dateFormat.format(Date())
                val jam = timeFormat.format(Date())
                if (isGambar){
                    val chat = ChatModel()
                    chat.uid = uid
                    chat.documentId = docId
                    chat.chatHeaderId = docId
                    chat.type = "image"
                    chat.message = message
                    chat.tanggal = ""
                    chat.jam = ""
                    chat.read = false
                    chatList.add(chat)
                    chatAdapter.notifyDataSetChanged()
                    rvChat.scrollToPosition(chatList.size - 1)
                    insertChat(docId,uid,docId,"image",message,tanggal, jam,false,senderid)
                    isGambar=false
                    lyCover.visibility = LinearLayout.GONE
                    //set gambar ke default
                    Glide.with(this)
                        .load(R.drawable.no_image)
                        .into(coverReplace)
                }else{
                    val chat = ChatModel()
                    chat.uid = uid
                    chat.documentId = docId
                    chat.chatHeaderId = docId
                    chat.type = "text"
                    chat.message = message
                    chat.tanggal = ""
                    chat.jam = ""
                    chat.read = false
                    chatList.add(chat)
                    chatAdapter.notifyDataSetChanged()
                    rvChat.scrollToPosition(chatList.size - 1)
                    insertChat(docId,uid,docId,"text",message,tanggal, jam,false,senderid)
                }
            }
        }else{
            etChat.error = "Tidak boleh kosong"
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_COVER -> {
                    // Ambil URI gambar yang dipilih dari galeri
                    val selectedImageUri = data?.data
                    //tampilkan lycover
                    lyCover.visibility = LinearLayout.VISIBLE
                    // Tampilkan gambar yang dipilih ke imageView coverReplace
                    coverReplace.setImageURI(selectedImageUri)
                    // Simpan URI gambar ke dalam list untuk penggunaan nanti
                    imagesList[0] = selectedImageUri
                }
            }
        }
    }
    private suspend fun uploadImage(imageUri: Uri?): String {
        val compressedImageUri = compressImage(this,imageUri)
        val storageReference = FirebaseStorage.getInstance().getReference("chat_images")
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

}


package com.nelayanku.apps.chat

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.nelayanku.apps.R
import com.nelayanku.apps.adapter.chat.ChatAdapter
import com.nelayanku.apps.model.ChatModel
import com.nelayanku.apps.tools.insertChat
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Inisialisasi Firebase
        firestore = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        btnSend = findViewById(R.id.ivSend)
        etChat = findViewById(R.id.etChat)
        ivChat = findViewById(R.id.ivChat)
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
            // Implementasi untuk memilih gambar dari galeri dan mengunggahnya ke Firebase Storage
            // Anda dapat menggunakan Intent untuk memilih gambar dari galeri dan mengunggahnya.
        }
    }

    private fun readChatData() {
        // Ganti "chats" dengan koleksi Firestore Anda
        val chatReference = firestore.collection("ChatHeader").document(docId).collection("Chat")

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
    }
}


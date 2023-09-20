package com.nelayanku.apps.chat

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nelayanku.apps.R
import com.nelayanku.apps.adapter.chat.ChatHeaderAdapter
import com.nelayanku.apps.model.ChatHeaderModel
import com.nelayanku.apps.model.Informasi
import com.nelayanku.apps.model.Product
import com.nelayanku.apps.tools.insertChatAdminHeader
import com.nelayanku.apps.tools.insertChatHeader
import com.nelayanku.apps.tools.readChatHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ChatListActivity : AppCompatActivity() {
    private val mFirestore = FirebaseFirestore.getInstance()
    private lateinit var productAdapter: ChatHeaderAdapter
    private lateinit var btnAdd: Button
    private lateinit var btn_layanan: LinearLayout
    private lateinit var recyclerView: RecyclerView
    val TAG = "LOAD DATA"
    private val productList: MutableList<ChatHeaderModel> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_list)
        recyclerView = findViewById(R.id.rvChat)
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(context, 1)
            // set the custom adapter to the RecyclerView
            productAdapter = ChatHeaderAdapter(
                productList,
                context,
                this@ChatListActivity::cardClick
            )
        }
        val shimmerContainer = findViewById<ShimmerFrameLayout>(R.id.shimmerContainer)
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        uid?.let { readData(mFirestore,shimmerContainer, it) }

        recyclerView.adapter = productAdapter
        productAdapter.filter("")
        btn_layanan = findViewById(R.id.btn_layanan)
        btn_layanan.setOnClickListener {
            chatAdmin()
        }
        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val role = sharedPreferences.getString("userRole", "")
        if(role=="admin"){
            btn_layanan.visibility = View.GONE
        }
        val searchEditText = findViewById<EditText>(R.id.btnCari)
        productAdapter.filter("")
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                productAdapter.filter(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
    fun cardClick(chatHeader:ChatHeaderModel){
        Log.d(TAG, "cardClick: ${chatHeader.uid}")
        //ambil role dan uid dari sharedpreferences
        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val uidUser = sharedPreferences.getString("userUid", "")
        val role = sharedPreferences.getString("userRole", "")
        if(chatHeader.lastSender!==uidUser){
            readChatHeader(chatHeader.documentId.toString(),this)
        }
        //intent ke chatActivity
        val intent = Intent(this, ChatActivity::class.java)
        //intent uid
        intent.putExtra("uid", chatHeader.uid)
        //intent documentId
        intent.putExtra("documentId", chatHeader.documentId)
        startActivity(intent)
    }
    private fun readData(db: FirebaseFirestore, shimmerContainer: ShimmerFrameLayout, uid: String) {
        shimmerContainer.startShimmer() // Start shimmer effect
        //ambil role dari sharedpreferences
        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val role = sharedPreferences.getString("userRole", "")
        GlobalScope.launch(Dispatchers.IO) {
            try {
                //pisahkan result sesuai role admin, user, dan seller
                var result = db.collection("ChatHeader").whereEqualTo("uidUser",uid).get().await()
                when(role){
                    "admin" -> {
                        result = db.collection("ChatHeader").whereEqualTo("uidAdmin",uid).get().await()
                    }
                    "user" -> {
                        result = db.collection("ChatHeader").whereEqualTo("uidUser",uid).get().await()
                    }
                    "seller" -> {
                        result = db.collection("ChatHeader").whereEqualTo("uidSeller",uid).get().await()
                    }
                }
                val products = mutableListOf<ChatHeaderModel>()
                for (document in result) {
                    val product = document.toObject(ChatHeaderModel::class.java)
                    //product.documentId = document.id
                    product.documentId = document.id
                    products.add(product)
                    Log.d(TAG, "Datanya : ${document.id} => ${document.data}")
                }

                withContext(Dispatchers.Main) {
                    productList.addAll(products)
                    productAdapter.filteredProductList.addAll(products)
                    productAdapter.notifyDataSetChanged()
                    shimmerContainer.stopShimmer() // Stop shimmer effect
                    shimmerContainer.visibility = View.GONE // Hide shimmer container
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error getting documents : $e")
                shimmerContainer.stopShimmer() // Stop shimmer effect
                shimmerContainer.visibility = View.GONE // Hide shimmer container
            }
        }
    }
    private fun chatAdmin(){
        //progress dialog
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        //ambil val uid dan nama dari sharedpreferences
        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val uidUser = sharedPreferences.getString("userUid", "")
        val namaUser = sharedPreferences.getString("userName", "")
        insertChatAdminHeader(uidUser.toString(),namaUser.toString(),this)
        progressDialog.dismiss()
    }
}
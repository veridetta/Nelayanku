package com.nelayanku.apps.chat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nelayanku.apps.R
import com.nelayanku.apps.adapter.chat.ChatHeaderAdapter
import com.nelayanku.apps.model.ChatHeaderModel
import com.nelayanku.apps.model.Informasi
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
    }
    fun cardClick(chatHeader:ChatHeaderModel){
        Log.d(TAG, "cardClick: ${chatHeader.uid}")
        //read headerChat
        readChatHeader(chatHeader.documentId.toString(),this)
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
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val result = db.collection("ChatHeader").get().await()
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
}
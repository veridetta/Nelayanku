package com.nelayanku.apps.act.seller

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nelayanku.apps.R
import com.nelayanku.apps.act.admin.AddActivity
import com.nelayanku.apps.adapter.admin.InfoAdapter
import com.nelayanku.apps.model.Informasi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class InfoActivity : AppCompatActivity() {
    private val mFirestore = FirebaseFirestore.getInstance()
    private lateinit var productAdapter: com.nelayanku.apps.adapter.seller.InfoAdapter
    private lateinit var btnAdd: Button
    private lateinit var recyclerView: RecyclerView
    val TAG = "LOAD DATA"
    private val productList: MutableList<Informasi> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        recyclerView = findViewById(R.id.recyclerViewProducts)
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(context, 1)
            // set the custom adapter to the RecyclerView
            productAdapter = com.nelayanku.apps.adapter.seller.InfoAdapter(
                productList,
                context
            )
        }
        val shimmerContainer = findViewById<ShimmerFrameLayout>(R.id.shimmerContainer)
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        uid?.let { readData(mFirestore,shimmerContainer, it) }

        recyclerView.adapter = productAdapter
        productAdapter.filter("")
    }
    private fun readData(db: FirebaseFirestore, shimmerContainer: ShimmerFrameLayout,uid: String) {
        shimmerContainer.startShimmer() // Start shimmer effect
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val result = db.collection("info").get().await()
                val products = mutableListOf<Informasi>()
                for (document in result) {
                    val product = document.toObject(Informasi::class.java)
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
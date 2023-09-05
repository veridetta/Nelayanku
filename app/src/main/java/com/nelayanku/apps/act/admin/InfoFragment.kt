package com.nelayanku.apps.act.admin
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nelayanku.apps.R
import com.nelayanku.apps.adapter.admin.InfoAdapter
import com.nelayanku.apps.model.Informasi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class InfoFragment : Fragment() {

    private val mFirestore = FirebaseFirestore.getInstance()
    private lateinit var productAdapter: InfoAdapter
    private lateinit var btnAdd: Button
    private lateinit var recyclerView: RecyclerView
    val TAG = "LOAD DATA"
    private val productList: MutableList<Informasi> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_info, container, false)
    }
    override fun onViewCreated(itemView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(itemView, savedInstanceState)
        recyclerView = itemView.findViewById(R.id.recyclerViewProducts)
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(activity, 1)
            // set the custom adapter to the RecyclerView
            productAdapter = InfoAdapter(
                productList,
                requireContext()
            ) { product -> editProduct(product) }
        }
        val shimmerContainer = itemView.findViewById<ShimmerFrameLayout>(R.id.shimmerContainer)
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        uid?.let { readData(mFirestore,shimmerContainer, it) }

        recyclerView.adapter = productAdapter
        btnAdd = itemView.findViewById(R.id.btnAdd)
        btnAdd.setOnClickListener {
            startActivity(
                Intent(requireContext(), AddActivity::class.java)
            )
        }
        val searchEditText = itemView.findViewById<EditText>(R.id.btnCari)
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
    private fun readData(db: FirebaseFirestore, shimmerContainer: ShimmerFrameLayout,uid: String) {
        shimmerContainer.startShimmer() // Start shimmer effect
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val result = db.collection("info").whereEqualTo("uid", uid).get().await()
                val products = mutableListOf<Informasi>()
                for (document in result) {
                    val product = document.toObject(Informasi::class.java)
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


    private fun editProduct(product: Informasi) {
        val intent = Intent(requireContext(), InfoEditActivity::class.java)
        intent.putExtra("productId", product.infoId) // Mengirim productId ke EditProductActivity
        intent.putExtra("documentId", product.documentId) // Mengirim productId ke EditProductActivity
        startActivity(intent)
    }

}

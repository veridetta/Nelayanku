package com.nelayanku.apps.act.admin
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nelayanku.apps.R
import com.nelayanku.apps.model.Product
import com.nelayanku.apps.redirect.AdminActivity
import com.nelayanku.apps.tools.Const.PATH_COLLECTION
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private val mFirestore = FirebaseFirestore.getInstance()
    private lateinit var productAdapter: com.nelayanku.apps.adapter.admin.ProductAdapter
    private lateinit var recyclerView: RecyclerView
    val TAG = "LOAD DATA"
    private val productList: MutableList<Product> = mutableListOf()
    private lateinit var coordinatorLayout: CoordinatorLayout
    //progressdialog
    private lateinit var progressDialog: ProgressDialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home_admin, container, false)
    }
    override fun onViewCreated(itemView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(itemView, savedInstanceState)
        coordinatorLayout = itemView.findViewById(R.id.coordinator)
        recyclerView = itemView.findViewById(R.id.recyclerViewProducts)
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(activity, 1)
            // set the custom adapter to the RecyclerView
            productAdapter = com.nelayanku.apps.adapter.admin.ProductAdapter(
                productList,
                requireContext(),
                { product -> accProduct(product) },
                { product -> tolakProduct(product) }
            )
        }
        val shimmerContainer = itemView.findViewById<ShimmerFrameLayout>(R.id.shimmerContainer)
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        uid?.let { readData(mFirestore,shimmerContainer, it) }

        recyclerView.adapter = productAdapter
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
                val result = db.collection(PATH_COLLECTION).get().await()
                val products = mutableListOf<Product>()
                for (document in result) {
                    val product = document.toObject(Product::class.java)
                    //simpan juga document.id ke product
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


    private fun accProduct(product: Product) {
        editProductToFirestore(product.documentId.toString(),"published")
    }
    private fun tolakProduct(product: Product) {
        editProductToFirestore(product.documentId.toString(),"declined")
    }
    private fun editProductToFirestore(
        documentId: String, status:String
    ) {
        //progress
        progressDialog = ProgressDialog(requireContext())
        progressDialog.setTitle("Loading...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val productData = hashMapOf(
            "status" to status
        )
        val db = FirebaseFirestore.getInstance()
        // Update the product data in Firestore
        db.collection(PATH_COLLECTION)
            .document(documentId) // Gunakan productId yang ada untuk merujuk dokumen yang ingin diubah
            .update(productData as Map<String, Any>)
            .addOnSuccessListener {
                progressDialog.dismiss()
                // Product updated successfully
                Snackbar.make(coordinatorLayout, "Produk berhasil diubah", Snackbar.LENGTH_SHORT).show()
                //refresh fragment
                val intent = Intent(requireContext(), AdminActivity::class.java)
                intent.putExtra("fragment", "home")
                startActivity(intent)
                requireActivity().finish()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                // Error occurred while updating product
                Snackbar.make(coordinatorLayout, "Gagal mengubah produk: ${e.message}", Snackbar.LENGTH_SHORT).show()
            }
    }
}

package com.nelayanku.apps.act.seller
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
import com.nelayanku.apps.adapter.seller.OrderAdapter
import com.nelayanku.apps.model.Order
import com.nelayanku.apps.redirect.AdminActivity
import com.nelayanku.apps.redirect.SellerActivity
import com.nelayanku.apps.tools.Const.PATH_COLLECTION
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class OrderFragment : Fragment() {

    private val mFirestore = FirebaseFirestore.getInstance()
    private lateinit var productAdapter: OrderAdapter
    private lateinit var recyclerView: RecyclerView
    val TAG = "LOAD DATA"
    private val productList: MutableList<Order> = mutableListOf()
    private lateinit var coordinatorLayout: CoordinatorLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_order, container, false)
    }
    override fun onViewCreated(itemView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(itemView, savedInstanceState)
        recyclerView = itemView.findViewById(R.id.recyclerView)
        coordinatorLayout = itemView.findViewById(R.id.coordinator)
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(activity, 1)
            // set the custom adapter to the RecyclerView
            productAdapter = OrderAdapter(
                productList,
                requireContext()
            ){ product -> ubahStatus(product) }
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
                val result = db.collection("orders").whereEqualTo("uidPenjual", uid).get().await()
                val products = mutableListOf<Order>()
                for (document in result) {
                    val product = document.toObject(Order::class.java)
                    //tambah documentId
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
    private fun ubahStatus(product: Order) {
        val statusSelected = productAdapter.statusSelected
        editProductToFirestore(product.documentId.toString(),statusSelected, product.idTransaksi.toString())
        if (statusSelected=="Selesai"){
            editWalletToFirestore(product.documentId.toString(),statusSelected, product.idTransaksi.toString())
        }
    }
    private fun editProductToFirestore(
        documentId: String, status:String, transaksiId : String
    ) {
        val productData = hashMapOf(
            "status" to status
        )
        val db = FirebaseFirestore.getInstance()
        // Update the product data in Firestore
        db.collection("orders")
            .document(documentId) // Gunakan productId yang ada untuk merujuk dokumen yang ingin diubah
            .update(productData as Map<String, Any>)
            .addOnSuccessListener {
                // Product updated successfully
                Snackbar.make(
                    coordinatorLayout,
                    "Status pesanan berhasil diubah",
                    Snackbar.LENGTH_SHORT
                ).show()
                //refresh fragment
                val intent = Intent(requireContext(), SellerActivity::class.java)
                intent.putExtra("fragment", "order")
                startActivity(intent)
                requireActivity().finish()
            }
            .addOnFailureListener { e ->
                // Error occurred while updating product
                Snackbar.make(
                    coordinatorLayout,
                    "Gagal mengubah Status pesanan: ${e.message}",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
    }
    //jika status selesai, ubah juga wallets dengan idTransaksi
    private fun editWalletToFirestore(
        documentId: String, status:String, transaksiId : String
    ) {
        val productData = hashMapOf(
            "status" to status
        )
        val db = FirebaseFirestore.getInstance()
        //get wallets where idTransaksi == transaksiId
        db.collection("wallets")
            .whereEqualTo("idTransaksi", transaksiId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    // Update the product data in Firestore
                    db.collection("wallets")
                        .document(document.id) // Gunakan productId yang ada untuk merujuk dokumen yang ingin diubah
                        .update(productData as Map<String, Any>)
                        .addOnSuccessListener {
                            // Product updated successfully
                            Snackbar.make(
                                coordinatorLayout,
                                "Status pesanan berhasil diubah",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { e ->
                            // Error occurred while updating product
                            Snackbar.make(
                                coordinatorLayout,
                                "Gagal mengubah Status pesanan: ${e.message}",
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }
}

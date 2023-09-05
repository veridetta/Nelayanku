package com.nelayanku.apps.act.user

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nelayanku.apps.R
import com.nelayanku.apps.adapter.seller.OrderAdapter
import com.nelayanku.apps.adapter.user.RiwayatAdapter
import com.nelayanku.apps.model.Order
import com.nelayanku.apps.model.Wallet
import com.nelayanku.apps.redirect.UserActivity
import com.nelayanku.apps.tools.Const
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RiwayatFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RiwayatFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val mFirestore = FirebaseFirestore.getInstance()
    private lateinit var productAdapter: RiwayatAdapter
    private lateinit var recyclerView: RecyclerView
    val TAG = "LOAD DATA"
    private val productList: MutableList<Order> = mutableListOf()
    //android:id="@+id/coordinator"
    private lateinit var coordinatorLayout: View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_riwayat, container, false)
    }
    override fun onViewCreated(itemView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(itemView, savedInstanceState)
        recyclerView = itemView.findViewById(R.id.recyclerView)
        coordinatorLayout = itemView.findViewById(R.id.coordinator)
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(activity, 1)
            // set the custom adapter to the RecyclerView
            productAdapter = RiwayatAdapter(
                productList,
                requireContext()
            ){ product -> selesai(product) }
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
    private fun readData(db: FirebaseFirestore, shimmerContainer: ShimmerFrameLayout, uid: String) {
        shimmerContainer.startShimmer() // Start shimmer effect
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val result = db.collection("orders").whereEqualTo("uid", uid).get().await()
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
    private fun selesai(product: Order) {
        editWalletToFirestore(product.uidPenjual.toString(), "Selesai", product.idTransaksi.toString())
        kirimSelesai(product.documentId.toString())
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RiwayatFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RiwayatFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    private fun kirimSelesai(
        orderId: String
    ) {
        val productData = hashMapOf(
            "status" to "Selesai",
        )
        val db = FirebaseFirestore.getInstance()
        // Update the product data in Firestore
        db.collection("orders")
            .document(orderId) // Gunakan productId yang ada untuk merujuk dokumen yang ingin diubah
            .update(productData as Map<String, Any>)
            .addOnSuccessListener {
                // Product updated successfully (snakebar)
                Snackbar.make(coordinatorLayout, "Berhasil", Snackbar.LENGTH_SHORT).show()
                // Redirect to SellerActivity fragment home
                val intent = Intent(requireContext(), UserActivity::class.java)
                intent.putExtra("fragment", "riwayat")
                startActivity(intent)

            }
            .addOnFailureListener { e ->
                // Error occurred while updating product
                Snackbar.make(coordinatorLayout, "Gagal : ${e.message}", Snackbar.LENGTH_SHORT).show()
            }
    }
    //jika status selesai, ubah juga wallets dengan idTransaksi
    private fun editWalletToFirestore(
        uid: String, status:String, transaksiId : String
    ) {
        val productData = hashMapOf(
            "status" to status
        )
        val db = FirebaseFirestore.getInstance()
        //get wallets where idTransaksi == transaksiId
        db.collection("wallets")
            .whereEqualTo("idTransaksi", transaksiId)
            .whereEqualTo("uid", uid)
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
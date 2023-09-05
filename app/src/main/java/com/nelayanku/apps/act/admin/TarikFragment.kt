package com.nelayanku.apps.act.admin

import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nelayanku.apps.R
import com.nelayanku.apps.act.seller.TarikActivity
import com.nelayanku.apps.adapter.admin.TarikAdapter
import com.nelayanku.apps.adapter.seller.WalletAdapter
import com.nelayanku.apps.model.Product
import com.nelayanku.apps.model.Wallet
import com.nelayanku.apps.redirect.AdminActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


/**
 * A simple [Fragment] subclass.
 * Use the [TarikFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TarikFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var recyclerViewTransaksi: RecyclerView
    private val walletList: MutableList<Wallet> = mutableListOf()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var adapter: TarikAdapter
    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var spinnerBulan: Spinner
    private lateinit var spinnerTahun: Spinner
    lateinit var progressDialog : ProgressDialog
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
        return inflater.inflate(R.layout.fragment_tarik, container, false)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewTransaksi = view.findViewById(R.id.recyclerViewTransaksi)
        coordinatorLayout = view.findViewById(R.id.coordinator)
        spinnerBulan = view.findViewById(R.id.spinnerBulan)
        spinnerTahun = view.findViewById(R.id.spinnerTahun)
        progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Mengirim data...")
        progressDialog.setCancelable(false)
        // Set up the RecyclerView
        recyclerViewTransaksi.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(activity, 1)
        }
        // set the custom adapter to the RecyclerView
        adapter = TarikAdapter(
            walletList,
            requireContext(),
            { product -> ubahStatus(product) }
        )
        recyclerViewTransaksi.adapter = adapter
        val shimmerContainer = view.findViewById<ShimmerFrameLayout>(R.id.shimmerContainer)
        val now = LocalDateTime.now()
        // Get the month number
        val monthNumber = now.monthValue
        // Get the year
        val year = now.year.toString()
        // Get the Indonesian month name
        val monthNames = arrayOf(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        )
        val monthName = monthNames[monthNumber - 1]
        val bulanAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.months_array,
            android.R.layout.simple_spinner_item
        )
        bulanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerBulan.adapter = bulanAdapter
        //atur agar bulan sama dengan sekarang
        spinnerBulan.setSelection(monthNumber - 1)
        val tahunAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.years_array,
            android.R.layout.simple_spinner_item
        )
        tahunAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTahun.adapter=tahunAdapter
        //atur agar tahun sama dengan sekarang
        spinnerTahun.setSelection(tahunAdapter.getPosition(year))
        // sesuai dengan bulan dan tahun yang dipilih
        spinnerBulan.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                val bulan = spinnerBulan.selectedItem.toString()
                val tahun = spinnerTahun.selectedItem.toString()
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                uid?.let { readData(firestore,shimmerContainer, it, tahun,bulan) }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
            }
        }
        //spinner tahun selected
        spinnerTahun.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                val bulan = spinnerBulan.selectedItem.toString()
                val tahun = spinnerTahun.selectedItem.toString()
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                uid?.let { readData(firestore,shimmerContainer, it, tahun,bulan) }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
            }
        }
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        uid?.let {
            readData(firestore, shimmerContainer, it, year,monthName)
        }
    }
    private fun ubahStatus(product: Wallet) {
        val statusSelected = adapter.statusSelected
        progressDialog.show()
        editProductToFirestore(product.documentId.toString(),statusSelected)
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TarikFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TarikFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    private fun readData(db: FirebaseFirestore, shimmerContainer: ShimmerFrameLayout,uid: String,tahun:String, bulan:String) {
        shimmerContainer.startShimmer() // Start shimmer effect
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val result = db.collection("wallets").whereEqualTo("jenis","penarikan")
                    .whereEqualTo("tahun",tahun).whereEqualTo("bulan",bulan).get().await()
                val products = mutableListOf<Wallet>()
                for (document in result) {
                    val product = document.toObject(Wallet::class.java)
                    //tambahkan documentId
                    product.documentId = document.id
                    products.add(product)
                    Log.d("Wallet ", "Datanya : ${document.id} => ${document.data}")
                }
                withContext(Dispatchers.Main) {
                    walletList.clear()
                    walletList.addAll(products)
                    adapter.filteredProductList.clear()
                    adapter.filteredProductList.addAll(products)
                    adapter.notifyDataSetChanged()
                    shimmerContainer.stopShimmer() // Stop shimmer effect
                    shimmerContainer.visibility = View.GONE // Hide shimmer container
                }

            } catch (e: Exception) {
                Log.w("Wallet ", "Error getting documents : $e")
                shimmerContainer.stopShimmer() // Stop shimmer effect
                shimmerContainer.visibility = View.GONE // Hide shimmer container
            }
        }
    }
    private fun editProductToFirestore(
        productId: String, status:String
    ) {
        val productData = hashMapOf(
            "status" to status
        )
        val db = FirebaseFirestore.getInstance()
        // Update the product data in Firestore
        db.collection("wallets")
            .document(productId) // Gunakan productId yang ada untuk merujuk dokumen yang ingin diubah
            .update(productData as Map<String, Any>)
            .addOnSuccessListener {
                Handler().postDelayed({
                    progressDialog.dismiss()
                }, 100)
                // Product updated successfully
                Snackbar.make(coordinatorLayout, "Status penarikan berhasil diubah", Snackbar.LENGTH_SHORT).show()
                //refresh fragment
                val intent = Intent(requireContext(), AdminActivity::class.java)
                intent.putExtra("fragment", "tarik")
                startActivity(intent)
                requireActivity().finish()
            }
            .addOnFailureListener { e ->
                // Error occurred while updating product
                Snackbar.make(coordinatorLayout, "Gagal mengubah Status penarikan: ${e.message}", Snackbar.LENGTH_SHORT).show()
            }
    }
}
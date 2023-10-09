package com.nelayanku.apps.act.user

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nelayanku.apps.R
import com.nelayanku.apps.adapter.user.WalletAdapter
import com.nelayanku.apps.model.Product
import com.nelayanku.apps.model.Wallet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.time.LocalDateTime
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [WalletFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class WalletFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var recyclerViewTransaksi: RecyclerView
    //textview textViewSaldo
    private lateinit var textViewSaldo: TextView
    private lateinit var spinnerBulan: Spinner
    private lateinit var spinnerTahun: Spinner
    private lateinit var adapter: WalletAdapter
    private lateinit var btnTarik: Button
    //btn topup
    private lateinit var btnTopup: Button
    private val walletList: MutableList<Wallet> = mutableListOf()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    var saldo = 0

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
        return inflater.inflate(R.layout.fragment_wallet_user, container, false)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewTransaksi = view.findViewById(R.id.recyclerViewTransaksi)
        textViewSaldo = view.findViewById(R.id.textViewSaldo)
        spinnerBulan = view.findViewById(R.id.spinnerBulan)
        spinnerTahun = view.findViewById(R.id.spinnerTahun)
        btnTarik = view.findViewById(R.id.btnTarik)


        //btn topup
        btnTopup = view.findViewById(R.id.btnTopup)
        // Set up the RecyclerView
        recyclerViewTransaksi.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(activity, 1)
        }
        // set the custom adapter to the RecyclerView
        adapter = WalletAdapter(
            walletList,
            requireContext()
        ) { product -> uploadBuktiBayar(product) }
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
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        uid?.let { readData(firestore, shimmerContainer, it, year,monthName)
        }
        // Set up spinners
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
                uid?.let {
                    getSaldo(it)
                    readData(firestore,shimmerContainer, it, tahun,bulan) }
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
        btnTarik.setOnClickListener {
            val intent = Intent(requireContext(), TarikActivity::class.java)
            //tambahkan value saldo ke intent
            intent.putExtra("saldo", saldo)
            startActivity(intent)
        }
        btnTopup.setOnClickListener {
            val intent = Intent(requireContext(), TopupActivity::class.java)
            //tambahkan value saldo ke intent
            intent.putExtra("saldo", saldo)
            startActivity(intent)
        }
    }
    private fun readData(db: FirebaseFirestore, shimmerContainer: ShimmerFrameLayout,uid: String,tahun:String, bulan:String) {
        shimmerContainer.startShimmer() // Start shimmer effect
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val result = db.collection("wallets").whereEqualTo("uid", uid)
                    .whereEqualTo("tahun",tahun).whereEqualTo("bulan",bulan).get().await()
                val products = mutableListOf<Wallet>()
                var totalPemasukan = 0
                var totalPengeluaran = 0
                for (document in result) {
                    val product = document.toObject(Wallet::class.java)
                    //total pemasukan diambil dari nominal where jenisnya = pemasukan
                    if (product.jenis == "pemasukan") {
                        val pemasukan = product.nominal?.toInt()
                        //if status selesai
                        if (product.status == "Selesai") {
                            totalPemasukan += pemasukan!!
                        }

                    }else {
                        val pengeluaran = product.nominal?.toInt()
                        //if status selsai
                        totalPengeluaran += pengeluaran!!
                    }
                    //tambahkan documentid
                    product.documentId = document.id
                    products.add(product)
                    Log.d("Wallet ", "Datanya : ${document.id} => ${document.data}")
                }
                val total = totalPemasukan - totalPengeluaran
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
                //buat shimmer dalam handler
                Handler(Looper.getMainLooper()).postDelayed({
                    shimmerContainer.stopShimmer() // Stop shimmer effect
                    shimmerContainer.visibility = View.GONE // Hide shimmer container
                }, 100)
            }
        }
    }
    fun getSaldo(uid: String){
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val result = firestore.collection("wallets").whereEqualTo("uid", uid)
                    .get().await()
                val products = mutableListOf<Wallet>()
                var totalPemasukan = 0
                var totalPengeluaran = 0
                for (document in result) {
                    val product = document.toObject(Wallet::class.java)
                    //total pemasukan diambil dari nominal where jenisnya = pemasukan
                    if (product.jenis == "pemasukan") {
                        val pemasukan = product.nominal?.toInt()
                        //if status selesai
                        if (product.status == "Selesai") {
                            totalPemasukan += pemasukan!!
                        }

                    }else {
                        val pengeluaran = product.nominal?.toInt()
                        //if status selsai
                        totalPengeluaran += pengeluaran!!

                    }
                    //tambahkan documentid
                    product.documentId = document.id
                    products.add(product)
                    Log.d("Wallet ", "Datanya : ${document.id} => ${document.data}")
                }
                val total = totalPemasukan - totalPengeluaran
                saldo = total
                withContext(Dispatchers.Main) {
                    textViewSaldo.text = formatCurrency(saldo.toDouble()) //ubah ke format rupiah
                }

            } catch (e: Exception) {
                Log.w("Wallet ", "Error getting documents : $e")
            }
        }
    }
    private fun formatCurrency(price: Double): String {
        val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return numberFormat.format(price)
    }
    //fun uploadBuktiBayar
    private fun uploadBuktiBayar(product: Wallet) {
        val intent = Intent(requireContext(), UploadActivity::class.java)
        intent.putExtra("idWallet", product.walletId) // Mengirim productId ke EditProductActivity
        intent.putExtra("documentId", product.documentId) // Mengirim documentId ke EditProductActivity
        startActivity(intent)
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TopupFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            WalletFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
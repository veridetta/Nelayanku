package com.nelayanku.apps.act.seller

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nelayanku.apps.R
import com.nelayanku.apps.adapter.seller.WalletAdapter
import com.nelayanku.apps.model.Wallet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.time.LocalDateTime
import java.util.Locale


class WalletFragment : Fragment() {
    private lateinit var recyclerViewTransaksi: RecyclerView
    private lateinit var textViewTotalPemasukan: TextView
    //textview textViewSaldo
    private lateinit var textViewSaldo: TextView
    private lateinit var spinnerBulan: Spinner
    private lateinit var spinnerTahun: Spinner
    private lateinit var adapter: WalletAdapter
    private lateinit var btnTarik: Button
    private val walletList: MutableList<Wallet> = mutableListOf()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    var saldo = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_wallet, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewTransaksi = view.findViewById(R.id.recyclerViewTransaksi)
        textViewTotalPemasukan = view.findViewById(R.id.textViewTotalPemasukan)
        textViewSaldo = view.findViewById(R.id.textViewSaldo)
        spinnerBulan = view.findViewById(R.id.spinnerBulan)
        spinnerTahun = view.findViewById(R.id.spinnerTahun)
        btnTarik = view.findViewById(R.id.btnTarik)
        btnTarik.setOnClickListener {
            val intent = Intent(requireContext(), TarikActivity::class.java)
            //tambahkan value saldo ke intent
            intent.putExtra("saldo", saldo)
            startActivity(intent)
        }
        // Set up the RecyclerView
        recyclerViewTransaksi.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(activity, 1)}
            // set the custom adapter to the RecyclerView
        adapter = WalletAdapter(
                walletList,
                requireContext())
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
        val tahunAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.years_array,
            android.R.layout.simple_spinner_item
        )
        tahunAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTahun.adapter=tahunAdapter
        // Set the spinner selection based on the current month and year
        val spinnerPosition = bulanAdapter.getPosition(monthName)
        spinnerBulan.setSelection(spinnerPosition)
        val spinnerPosition2 = tahunAdapter.getPosition(year)
        spinnerTahun.setSelection(spinnerPosition2)
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
                    products.add(product)
                    Log.d("Wallet ", "Datanya : ${document.id} => ${document.data}")
                }
                val total = totalPemasukan - totalPengeluaran
                saldo = total
                withContext(Dispatchers.Main) {
                    textViewTotalPemasukan.text = formatCurrency(total.toDouble())
                    textViewSaldo.text = formatCurrency(saldo.toDouble()) //ubah ke format rupiah
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
    private fun formatCurrency(price: Double): String {
        val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return numberFormat.format(price)
    }
}

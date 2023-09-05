package com.nelayanku.apps.act.user

import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.nelayanku.apps.R
import com.nelayanku.apps.model.Product
import com.nelayanku.apps.model.Wallet
import com.nelayanku.apps.redirect.AdminActivity
import com.nelayanku.apps.redirect.UserActivity
import com.nelayanku.apps.tools.Const
import com.nelayanku.apps.tools.formatCurrency
import com.nelayanku.apps.tools.hitungJarak
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.util.UUID

class PembayaranActivity : AppCompatActivity() {
    //init textview dan elemen lain dari xml
    lateinit var tvAlamat : TextView
    lateinit var tvNamaBarang : TextView
    lateinit var tvHarga : TextView
    lateinit var tvTotal : TextView
    lateinit var tvJumlahBarang : TextView
    //tv tv_ongkir
    lateinit var tvOngkir : TextView
    //tv_biaya
    lateinit var tvBiaya : TextView
    //tv_grand_total
    lateinit var tvGrandTotal : TextView
    //btn_checkout
    lateinit var btnCheckout : TextView
    //iv add dan min
    lateinit var ivAdd : ImageView
    lateinit var ivMin : ImageView
    var total : Int = 0
    var grandTotalgrandTotal : Double = 0.0
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    lateinit var productId : String
    lateinit var progressDialog : ProgressDialog
    //nama barang dan harga
    lateinit var namaBarang : String
    lateinit var hargaBarang : String
    var jumlahBarang : Int = 0
    var pendPenjual : Int = 0
    //biaya dan harga string
    lateinit var biaya : String
    lateinit var ongkir : String
    //uid penjual
    lateinit var uidPenjual : String
    //tag
    val TAG = "LOAD DATA"
    var uid = ""
    var saldo = 0.0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pembayaran)
        initial()
        loadIntent()
        loadFirebase()
        klik()
    }
    fun initial(){
        //init textview dan elemen lain dari xml
        tvAlamat = findViewById(R.id.tv_alamat)
        tvNamaBarang = findViewById(R.id.tv_nama_barang)
        tvHarga = findViewById(R.id.tv_harga_barang)
        tvTotal = findViewById(R.id.tv_total)
        tvJumlahBarang = findViewById(R.id.tv_jumlah_barang)
        //tv tv_ongkir
        tvOngkir = findViewById(R.id.tv_ongkir)
        //tv_biaya
        tvBiaya = findViewById(R.id.tv_biaya)
        //tv_grand_total
        tvGrandTotal = findViewById(R.id.tv_grand_total)
        //btn_checkout
        btnCheckout = findViewById(R.id.btn_checkout)
        //iv add dan min
        ivAdd = findViewById(R.id.iv_add)
        ivMin = findViewById(R.id.iv_min)

    }
    fun loadIntent(){
        //load intent
        val intent = intent
        namaBarang = intent.getStringExtra("namaBarang") ?:""
        hargaBarang = intent.getStringExtra("hargaBarang")?:""
        //idProduct
        productId = intent.getStringExtra("productId") ?:""
        //uid
        uidPenjual = intent.getStringExtra("uid") ?:""
        //ongkir
        ongkir = intent.getStringExtra("ongkir") ?:"0.0"
        //layananPembeli
        biaya = intent.getStringExtra("layananPembeli") ?: "0.0"
        tvNamaBarang.text = namaBarang
        tvHarga.text = formatCurrency(hargaBarang.toDouble())
        tvTotal.text = formatCurrency(hargaBarang.toDouble())
        tvOngkir.text = formatCurrency(ongkir.toDouble())
        tvBiaya.text = formatCurrency(biaya.toDouble())
        tvGrandTotal.text = formatCurrency((hargaBarang.toDouble() + ongkir.toDouble() + biaya.toDouble()))
        total = hargaBarang.toInt()
        jumlahBarang = 1
        grandTotalgrandTotal = hargaBarang.toDouble() + ongkir.toDouble() + biaya.toDouble()
    }
    fun loadFirebase(){
        //load data dari firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        //uid
        uid = auth.currentUser?.uid.toString()
    }
    //fungsi klik ivadd dan min
    @RequiresApi(Build.VERSION_CODES.O)
    fun klik(){
        ivAdd.setOnClickListener {
            //tambahkan jumlah barang

            jumlahBarang += 1
            tvJumlahBarang.text = jumlahBarang.toString()
            //hitung total
             total = jumlahBarang * hargaBarang.toInt()
            tvTotal.text = formatCurrency(total.toDouble())
            //hitung grand total
            grandTotalgrandTotal = total + ongkir.toDouble() + biaya.toDouble()
            tvGrandTotal.text = formatCurrency(grandTotalgrandTotal.toDouble())
        }
        ivMin.setOnClickListener {
            //set minimal 0
            if (jumlahBarang == 0){
                tvJumlahBarang.text = "0"
                //snackbar
                Snackbar.make(it, "Jumlah barang minimal 0", Snackbar.LENGTH_SHORT).show()
            }else{
                //kurangi jumlah barang
                jumlahBarang -= 1
                tvJumlahBarang.text = jumlahBarang.toString()
                //hitung total
                total = jumlahBarang * hargaBarang.toInt()
                tvTotal.text = formatCurrency(total.toDouble())
                //hitung grand total
                grandTotalgrandTotal = total + ongkir.toDouble() + biaya.toDouble()
                tvGrandTotal.text = formatCurrency(grandTotalgrandTotal.toDouble())
            }

        }
        btnCheckout.setOnClickListener {
            //cek saldo cukup tidak
            getSaldo()
            if (saldo < grandTotalgrandTotal){
                //snackbar
                Snackbar.make(it, "Saldo tidak cukup", Snackbar.LENGTH_SHORT).show()
            }else{
                //cekout
                //progress dialog
                progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Loading")
                progressDialog.setMessage("Sedang memproses checkout")
                progressDialog.setCancelable(false)
                progressDialog.show()
                cekout()
            }
        }
    }
    //fungsi cekout
    @RequiresApi(Build.VERSION_CODES.O)
    fun cekout(){
        //insert ke collection order
        val order = hashMapOf(
            "uid" to uid,
            "idTransaksi" to UUID.randomUUID().toString(),
            "uidPenjual" to uidPenjual,
            "idProduct" to productId,
            "namaBarang" to namaBarang,
            "hargaBarang" to hargaBarang,
            "jumlahBarang" to tvJumlahBarang.text.toString(),
            "total" to total,
            "ongkir" to ongkir,
            "biaya" to biaya,
            "grandTotal" to grandTotalgrandTotal,
            "status" to "Menunggu Konfirmasi",
            "alamat" to tvAlamat.text.toString()
        )
        firestore.collection("orders").add(order).addOnCompleteListener {
            if (it.isSuccessful){
                //snackbar
                Snackbar.make(btnCheckout, "Berhasil checkout", Snackbar.LENGTH_SHORT).show()
                //update saldo dan ambil idTransaksi
                updateSaldo(order["idTransaksi"].toString())
            }else{
                //snackbar
                Snackbar.make(btnCheckout, "Gagal checkout", Snackbar.LENGTH_SHORT).show()
            }
        }
    }
    fun getSaldo(){
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
                        if (product.status == "Selesai") {
                            totalPengeluaran += pengeluaran!!
                        }
                    }
                    //tambahkan documentid
                    product.documentId = document.id
                    products.add(product)
                    Log.d("Wallet ", "Datanya : ${document.id} => ${document.data}")
                }
                val total = totalPemasukan - totalPengeluaran
                saldo = total.toDouble()
                withContext(Dispatchers.Main) {

                }

            } catch (e: Exception) {
                Log.w("Wallet ", "Error getting documents : $e")
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun updateSaldo(idTransaksi : String){
        //update saldo
        val now = LocalDateTime.now()
        val monthNumber = now.monthValue
        val year = now.year.toString()
        val monthNames = arrayOf(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        )
        val monthName = monthNames[monthNumber - 1]
        val saldoAkhir = saldo - grandTotalgrandTotal
        val wallet = hashMapOf(
            "uid" to uid,
            "jenis" to "pengeluaran",
            "nominal" to grandTotalgrandTotal,
            "saldo" to saldoAkhir,
            "status" to "Selesai",
            "bulan" to monthName,
            "tahun" to year,
            "tanggal" to now.toString(),
            "buktiTf" to "",
            "walletId" to UUID.randomUUID().toString(),
            "idTransaksi" to idTransaksi,
            "nama" to "Pembayaran",
            "bank" to "",
            "norek" to ""
        )
        firestore.collection("wallets").add(wallet).addOnCompleteListener {
            if (it.isSuccessful){
                //hide progress dalam handler
                //snackbar
                Snackbar.make(btnCheckout, "Berhasil update saldo", Snackbar.LENGTH_SHORT).show()
            }else{
                //snackbar
                Snackbar.make(btnCheckout, "Gagal update saldo", Snackbar.LENGTH_SHORT).show()
            }
        }
        //update saldo seller
        //hargabarang dikali jumlah barang dikurangi biaya
        pendPenjual = (hargaBarang.toInt() * jumlahBarang) - biaya.toInt()
        val walletSeller = hashMapOf(
            "uid" to uidPenjual,
            "jenis" to "pemasukan",
            "nominal" to pendPenjual,
            "saldo" to pendPenjual,
            "status" to "Diproses",
            "bulan" to monthName,
            "tahun" to year,
            "tanggal" to now.toString(),
            "buktiTf" to "",
            "walletId" to UUID.randomUUID().toString(),
            "idTransaksi" to idTransaksi,
            "nama" to "Penjualan",
            "bank" to "",
            "norek" to ""
        )
        firestore.collection("wallets").add(walletSeller).addOnCompleteListener {
            if (it.isSuccessful){
                //hide progress dalam handler
                Handler(Looper.getMainLooper()).postDelayed({
                    progressDialog.dismiss()
                }, 100)
                //intent ke riwayatfragment
                val intent = Intent(this, UserActivity::class.java)
                intent.putExtra("fragment", "riwayat")
                startActivity(intent)
                finish()
                //snackbar
                Snackbar.make(btnCheckout, "Berhasil update saldo", Snackbar.LENGTH_SHORT).show()
            }else{
                //snackbar
                Snackbar.make(btnCheckout, "Gagal update saldo", Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}
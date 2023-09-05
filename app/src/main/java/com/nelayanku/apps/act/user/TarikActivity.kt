package com.nelayanku.apps.act.user

import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nelayanku.apps.R
import com.nelayanku.apps.model.Wallet
import com.nelayanku.apps.redirect.SellerActivity
import com.nelayanku.apps.redirect.UserActivity
import com.nelayanku.apps.tools.formatCurrency
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.util.UUID

class TarikActivity : AppCompatActivity() {
    private lateinit var btnTarikDana: Button
    private lateinit var editTextNomorRekening: EditText
    private lateinit var editTextNominal: EditText
    private lateinit var editTextNama: EditText
    private lateinit var editTextNamaBank: EditText
    private lateinit var textViewSaldo: TextView
    private lateinit var db: FirebaseFirestore
    lateinit var progressDialog : ProgressDialog

    var saldo = 0
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tarik2)
        btnTarikDana = findViewById(R.id.btnTarikDana)
        textViewSaldo = findViewById(R.id.textViewSaldo)
        editTextNama = findViewById(R.id.editTextNama)
        editTextNamaBank = findViewById(R.id.editTextNamaBank)
        editTextNomorRekening = findViewById(R.id.editTextNomorRekening)
        editTextNominal = findViewById(R.id.editTextNominal)
        textViewSaldo.text = "Memuat..."
        db = FirebaseFirestore.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Mengirim data...")
        progressDialog.setCancelable(false)
        btnTarikDana.setOnClickListener {
            val rekening = editTextNomorRekening.text.toString()
            val nominal = editTextNominal.text.toString()
            if (rekening.isEmpty()) {
                editTextNomorRekening.error = "Nomor rekening tidak boleh kosong"
                return@setOnClickListener
            }
            if (nominal.isEmpty()) {
                editTextNominal.error = "Nominal tidak boleh kosong"
                return@setOnClickListener
            }
            // nama dan nama bank  wajib diisi
            val nama = editTextNama.text.toString()
            val namaBank = editTextNamaBank.text.toString()
            if (nama.isEmpty()) {
                editTextNama.error = "Nama tidak boleh kosong"
                return@setOnClickListener
            }
            if (namaBank.isEmpty()) {
                editTextNamaBank.error = "Nama bank tidak boleh kosong"
                return@setOnClickListener
            }
            cekSaldo(nominal.toInt())
        }
        //disable klik btnTarikDana
        btnTarikDana.isEnabled = false
        getSaldo(FirebaseAuth.getInstance().currentUser?.uid.toString())
    }
    //fungsi cek kembali saldo dari firestore jika saldo cukup atau lebih besar dari nominal maka lakukan proses tarik dana (insert data ke firestore) status pending jika saldo tidak cukup maka tampilkan pesan saldo tidak cukup
    @RequiresApi(Build.VERSION_CODES.O)
    private fun cekSaldo(nominal: Int) {
        if (saldo >= nominal) {
            progressDialog.show()
            prosesTarikDana(nominal)
        } else {
            editTextNominal.error = "Saldo tidak cukup"
        }
    }
    //fungsi proses tarik dana
    @RequiresApi(Build.VERSION_CODES.O)
    private fun prosesTarikDana(nominal: Int) {
        //progress dialog
        val rekening = editTextNomorRekening.text.toString()
        val nama = editTextNama.text.toString()
        val namaBank = editTextNamaBank.text.toString()
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

        val data = hashMapOf(
            "norek" to rekening,
            "nama" to nama,
            "bank" to namaBank,
            "nominal" to nominal,
            "status" to "pending",
            "uid" to uid,
            "bulan" to monthName,
            "tahun" to year,
            "tanggal" to now.toString(),
            "jenis" to "pengeluaran",
            "walletId" to UUID.randomUUID().toString(),
            "idTransaksi" to ""
        )
        db.collection("wallets")
            .add(data)
            .addOnSuccessListener {
                //sembunyikan dialog dalam handler
                Handler().postDelayed({
                    progressDialog.dismiss()
                }, 100)
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Tarik dana berhasil diminta",
                    Snackbar.LENGTH_SHORT
                ).show()
                Handler().postDelayed({
                    val intent = Intent(this, UserActivity::class.java)
                    intent.putExtra("fragment", "wallet")
                    startActivity(intent)
                    finish()
                }, 1000)
            }
            .addOnFailureListener {
                editTextNominal.error = "Gagal tarik dana"
            }
    }
    fun getSaldo(uid: String){
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val result = db.collection("wallets").whereEqualTo("uid", uid)
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
                saldo = total
                withContext(Dispatchers.Main) {
                    //enablebutton
                    btnTarikDana.isEnabled = true
                    textViewSaldo.text = formatCurrency(saldo.toDouble()) //ubah ke format rupiah
                }

            } catch (e: Exception) {
                Log.w("Wallet ", "Error getting documents : $e")
            }
        }
    }

}
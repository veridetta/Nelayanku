package com.nelayanku.apps.act.user

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContentProviderCompat.requireContext
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.midtrans.sdk.corekit.callback.TransactionFinishedCallback
import com.midtrans.sdk.corekit.core.MidtransSDK
import com.midtrans.sdk.corekit.core.TransactionRequest
import com.midtrans.sdk.corekit.core.UIKitCustomSetting
import com.midtrans.sdk.corekit.core.themes.CustomColorTheme
import com.midtrans.sdk.corekit.models.BillingAddress
import com.midtrans.sdk.corekit.models.CustomerDetails
import com.midtrans.sdk.corekit.models.ItemDetails
import com.midtrans.sdk.corekit.models.ShippingAddress
import com.midtrans.sdk.corekit.models.snap.TransactionResult
import com.midtrans.sdk.uikit.SdkUIFlowBuilder
import com.midtrans.sdk.uikit.api.model.SnapTransactionDetail
import com.midtrans.sdk.uikit.external.UiKitApi
import com.midtrans.sdk.uikit.internal.util.UiKitConstants
import com.nelayanku.apps.R
import com.nelayanku.apps.redirect.UserActivity
import com.nelayanku.apps.tools.formatCurrency
import java.time.LocalDateTime
import java.util.UUID

class TopupActivity : AppCompatActivity(){
    private lateinit var btnTopup: Button
    private lateinit var editTextNominal: EditText
    private lateinit var db: FirebaseFirestore
    private lateinit var tvSaldo: TextView
    private var saldo = 0
    private var transId = ""
    private var name = ""
    private var nominal = 0.0
    private var phone = ""
    private var email = ""
    private var itemDetails = listOf<ItemDetails>()
    private var customerDetails: com.midtrans.sdk.uikit.api.model.CustomerDetails? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_topup)

        val sal = intent.getStringExtra("saldo") ?: "0"
        saldo = sal.toInt()
        tvSaldo = findViewById(R.id.textViewSaldo)
        tvSaldo.text = "Saldo : " + formatCurrency(saldo.toDouble())
        tvSaldo.text = "Saldo : " + formatCurrency(saldo.toDouble())

        btnTopup = findViewById(R.id.btnTopup)
        editTextNominal = findViewById(R.id.editTextNominal)
        db = FirebaseFirestore.getInstance()
        buildUiKit()
        btnTopup.setOnClickListener {
            nominal = editTextNominal.text.toString().toDouble()
            //nama dan email ambil dari sharedpreferences
            getSharedPreferences("MyPrefs", MODE_PRIVATE).apply {
                name = getString("userName", "").toString()
                email = getString("userEmail", "").toString()
            }
            phone = "0882322223"
            itemDetails = listOf(ItemDetails("topup", nominal, 1, "topup-"+nominal))
            customerDetails = com.midtrans.sdk.uikit.api.model.CustomerDetails(
                "topup",
                name,
                " dari Nelayanku-Apps",
                email,
                phone
            )
            if (nominal<1) {
                editTextNominal.error = "Nominal tidak boleh kosong"
                return@setOnClickListener
            }
            if (nominal.toInt() < 20000) {
                editTextNominal.error = "Nominal minimal 20000"
                return@setOnClickListener
            }
            mulai()
            //pergi midtransactivity
            //val intent = Intent(this, MidtransActivity::class.java)
            //tambahkan value saldo ke intent
            //intent.putExtra("saldo", saldo)
            //startActivity(intent)
            //goToPayment()
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun insertPaymentStatus(transactionId: String?) {
        val now = LocalDateTime.now()
        val monthNumber = now.monthValue
        val year = now.year.toString()
        val monthNames = arrayOf(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        )
        val monthName = monthNames[monthNumber - 1]
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val data = hashMapOf(
            "norek" to "",
            "nama" to "Midtrans",
            "bank" to "Midtrans",
            "nominal" to nominal,
            "status" to "Menunggu Konfirmasi",
            "uid" to uid,
            "bulan" to monthName,
            "tahun" to year,
            "tanggal" to now.toString(),
            "jenis" to "pemasukan",
            "buktiTf" to "",
            "walletId" to UUID.randomUUID().toString(),
            "idTransaksi" to transactionId
        )

        db.collection("wallets")
            .add(data)
            .addOnSuccessListener {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Topup dana berhasil diminta",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener {
                editTextNominal.error = "Gagal topup dana"
            }
    }
    private fun buildUiKit() {
        UiKitApi.Builder()
            .withContext(this.applicationContext)
            .withMerchantUrl("http://midtrans.dikmatyasika.com/midtrans.php/")
            .withMerchantClientKey("SB-Mid-client-KZ13jYwCXoOjYgCL")
            .enableLog(true)
            .withColorTheme(
                com.midtrans.sdk.uikit.api.model.CustomColorTheme(
                    "#00A2F3",
                    "#0069BA",
                    "#FFE51255"
                )
            )
            .build()
        uiKitCustomSetting()
    }
    private fun uiKitCustomSetting() {
        val uIKitCustomSetting = UIKitCustomSetting()
        uIKitCustomSetting.setSaveCardChecked(true)
        MidtransSDK.getInstance().setUiKitCustomSetting(uIKitCustomSetting)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun mulai(){
        UiKitApi.getDefaultInstance().startPaymentUiFlow(
            this@TopupActivity,
            launcher,
            initTransactionDetails(),
            customerDetails,
            itemDetails,
        )
    }
    private var status: String = ""
    @RequiresApi(Build.VERSION_CODES.O)
    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result?.resultCode == RESULT_OK) {
                result.data?.let {
                    val transactionResult = it.getParcelableExtra<com.midtrans.sdk.uikit.api.model.TransactionResult>(
                        UiKitConstants.KEY_TRANSACTION_RESULT)
                    //deleteItem()
                    getPaymentStatus(transactionResult?.transactionId)
                    Toast.makeText(this, "Transaksi Berhasil ${transactionResult?.transactionId}", Toast.LENGTH_LONG).show()
                    insertPaymentStatus(transactionResult?.transactionId)
                }
            }
        }
    private fun getPaymentStatus(transactionId: String?) {
        val url = "https://api.sandbox.midtrans.com/v2/$transactionId/status"

        val headers = HashMap<String, String>()
        headers["Accept"] = "application/json"
        headers["Content-Type"] = "application/json"
        headers["Authorization"] = "Basic U0ItTWlkLXNlcnZlci1BVHhkbmowM1Q0bGRmQ3c1TGI1bkVUMkM6"

        val request = object : JsonObjectRequest(
            Method.GET, url, null,
            { response ->
//                val transactionStatus = response.getString("transaction_status")
  //              status = transactionStatus
            },
            { error ->
                Log.d("Error to get payment status", error.toString())
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                return headers
            }
        }

        // Create a request queue
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(request)
    }
    private fun initTransactionDetails() : SnapTransactionDetail {
        val orderID = UUID.randomUUID().toString()
        return SnapTransactionDetail(
            orderId = orderID,
            grossAmount = nominal //GlobalData.totalBayar.toDouble()
        )
    }

}

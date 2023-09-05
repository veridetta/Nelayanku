package com.nelayanku.apps.act.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.midtrans.sdk.corekit.callback.TransactionFinishedCallback
import com.midtrans.sdk.corekit.core.MidtransSDK
import com.midtrans.sdk.corekit.core.TransactionRequest
import com.midtrans.sdk.corekit.core.UIKitCustomSetting
import com.midtrans.sdk.corekit.core.themes.CustomColorTheme
import com.midtrans.sdk.corekit.models.BillingAddress
import com.midtrans.sdk.corekit.models.CustomerDetails
import com.midtrans.sdk.corekit.models.ItemDetails
import com.midtrans.sdk.corekit.models.ShippingAddress
import com.midtrans.sdk.uikit.SdkUIFlowBuilder
import com.midtrans.sdk.uikit.api.model.SnapTransactionDetail
import com.midtrans.sdk.uikit.external.UiKitApi
import com.midtrans.sdk.uikit.internal.util.UiKitConstants
import com.nelayanku.apps.R
import com.nelayanku.apps.tools.SdkConfig.MERCHANT_BASE_CHECKOUT_URL
import com.nelayanku.apps.tools.SdkConfig.MERCHANT_CLIENT_KEY
import java.util.UUID

class MidtransActivity : AppCompatActivity(){
    private var status: String = ""
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
    private fun insertPaymentStatus(transactionId: String?) {
        val url = "http://192.168.1.106/pos/order/midtrans_status.php"
        val request = Volley.newRequestQueue(applicationContext)

        val stringRequest = object : StringRequest(
            Method.GET,
            "$url?order_id=$transactionId&name=Fery&phone=0882192232&payment_status=$status",
            { response ->
                if (response == "1") {
                    //success
                }
            },
            { error ->
                Toast.makeText(this, "Error inserting payment status: ${error.toString()}", Toast.LENGTH_LONG).show()
                Log.d("Error insert payment status", error.toString())
            }) {}

        request.add(stringRequest)
    }
    private fun deleteItem() {
        /*
        val queue = Volley.newRequestQueue(this)
        val stringRequest =
            object : StringRequest(Method.DELETE, GlobalData.BASE_URL+"item/deleteitemall.php", Response.Listener { _ ->
                startActivity(Intent(this, MainActivity::class.java))
            }, { _ ->
                Toast.makeText(this, "Terjadi kesalahan saat menghapus produk", Toast.LENGTH_SHORT)
                    .show()
            }) {}
        queue.add(stringRequest)
         */
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
                val transactionStatus = response.getString("transaction_status")
                status = transactionStatus
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_midtrans)
        buildUiKit()
        mulai()
    }
    private fun initTransactionDetails() : SnapTransactionDetail {
        val orderID = UUID.randomUUID().toString()
        return SnapTransactionDetail(
            orderId = orderID,
            grossAmount = 200000.0 //GlobalData.totalBayar.toDouble()
        )
    }
    val customerDetails = com.midtrans.sdk.uikit.api.model.CustomerDetails(
        "pembayaran",
        "Fery dari Nelayanku Apps",
        " No.HP: 0882212",
        "ichwansholihin03@gmail.com"
    )
    private var itemDetails = listOf(ItemDetails("id-11", 200000.0, 1, "id-11"))
    private fun buildUiKit() {
        UiKitApi.Builder()
            .withContext(this.applicationContext)
            .withMerchantUrl("http://midtrans.dikmatyasika.com/midtrans.php/")
            .withMerchantClientKey("SB-Mid-client-KZ13jYwCXoOjYgCL")
            .enableLog(true)
            .withColorTheme(
                com.midtrans.sdk.uikit.api.model.CustomColorTheme(
                    "#FFE51255",
                    "#B61548",
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
    fun mulai(){
        UiKitApi.getDefaultInstance().startPaymentUiFlow(
            this@MidtransActivity,
            launcher,
            initTransactionDetails(),
            customerDetails,
            itemDetails,
        )
    }

}
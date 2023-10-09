package com.nelayanku.apps

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.ktx.appCheck
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.google.firebase.messaging.FirebaseMessaging
import com.nelayanku.apps.redirect.AdminActivity
import com.nelayanku.apps.redirect.SellerActivity
import com.nelayanku.apps.redirect.UserActivity


class SplashActivity : AppCompatActivity() {
    private var progressBar: ProgressBar? = null
    private var loadingText: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Inisialisasi elemen UI
        progressBar = findViewById<ProgressBar>(R.id.progressBar)
        loadingText = findViewById<TextView>(R.id.loadingText)

        // Simulasikan proses loading
        simulateLoading()

        // Periksa apakah pengguna sudah login
        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLogin", false)
        // Initialize Firebase
        Firebase.initialize(context = this)
        Firebase.appCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance(),
        )
        Handler(Looper.getMainLooper()).postDelayed({
            checkUserLogin()
        }, 1000) // Jeda selama 1 detik

    }

    private fun simulateLoading() {
        // Tampilkan progress bar dan teks "Loading..."
        progressBar!!.visibility = ProgressBar.VISIBLE
        loadingText!!.visibility = TextView.VISIBLE

        // Simulasikan proses loading
        // Misalnya, Anda dapat menggunakan background thread atau AsyncTask
        Handler(Looper.getMainLooper()).postDelayed({

            // Setelah simulasi loading selesai, sembunyikan progress bar dan teks
            progressBar!!.visibility = ProgressBar.GONE
            loadingText!!.visibility = TextView.GONE
        }, 1500) // Simulasi loading selama 1.5 detik
    }
    private fun checkUserLogin() {
        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLogin", false)
        val userRole = sharedPreferences.getString("userRole", "")

        val targetActivity = when {
            isLoggedIn -> {
                //cek token firebase
                when (userRole) {
                    "admin" -> AdminActivity::class.java
                    "user" -> UserActivity::class.java
                    "seller" -> SellerActivity::class.java
                    else -> LoginActivity::class.java // Handle unknown roles
                }
            }
            else -> LoginActivity::class.java
        }
        //jika berhasil login maka update token
        if (isLoggedIn) {
            //cek jika token kosong maka update token
            val tokken = sharedPreferences.getString("token", "")
            if (tokken.isNullOrEmpty()) {
                val uid = sharedPreferences.getString("userUid", "")
                updateToken(uid.toString())
            }
        }

        startActivity(Intent(this@SplashActivity, targetActivity))
        finish()
    }
    private fun updateToken(uid :String){
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("TAG", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            // Get new FCM registration token
            val token = task.result
            if (token.isNotEmpty()) {
                val newUser = hashMapOf(
                    "token" to token,
                )
                //get uid
                val db = FirebaseFirestore.getInstance()
                // Update the product data in Firestore
                db.collection("users")
                    .document(uid) // Gunakan productId yang ada untuk merujuk dokumen yang ingin diubah
                    .update(newUser as Map<String, Any>)
                    .addOnSuccessListener {
                        Log.d("BERHASIL", "DocumentSnapshot successfully updated!")
                    }
            }else {
                Log.d("GAGAL", "Error updating document")
            }
            //simpan token ke shared preferences
            val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("token", token)
            editor.apply()
            //insert textview baru
            loadingText!!.visibility = View.VISIBLE
            loadingText!!.text = token
            Log.d("TAG", token.toString())
        })
    }
}

package com.nelayanku.apps

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.ktx.appCheck
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
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
        }, 2000) // Jeda selama 2 detik
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
                when (userRole) {
                    "admin" -> AdminActivity::class.java
                    "user" -> UserActivity::class.java
                    "seller" -> SellerActivity::class.java
                    else -> LoginActivity::class.java // Handle unknown roles
                }
            }
            else -> LoginActivity::class.java
        }

        startActivity(Intent(this@SplashActivity, targetActivity))
        finish()
    }
}

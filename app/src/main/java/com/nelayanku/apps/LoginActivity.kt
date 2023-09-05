package com.nelayanku.apps

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.nelayanku.apps.account.ForgotActivity
import com.nelayanku.apps.account.RegisterActivity
import com.nelayanku.apps.account.SellerRegisterActivity
import com.nelayanku.apps.redirect.AdminActivity
import com.nelayanku.apps.redirect.SellerActivity
import com.nelayanku.apps.redirect.UserActivity


class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient
    // Declare UI elements
    private lateinit var buttonLogin: Button
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var textViewRegister: TextView
    private lateinit var textViewRegisterSeller: TextView
    private lateinit var textViewForgotPassword: TextView
    private lateinit var buttonGoogle: ImageView
    private lateinit var buttonResendVerification: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        // Initialize UI elements
        buttonLogin = findViewById(R.id.buttonLogin)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        progressBar = findViewById(R.id.progressBar)
        textViewRegister = findViewById(R.id.textViewRegister)
        textViewRegisterSeller = findViewById(R.id.textViewRegisterSeller)
        textViewForgotPassword = findViewById(R.id.textViewForgotPassword)
        buttonGoogle = findViewById(R.id.btnGoogle)
        buttonResendVerification = findViewById(R.id.buttonResendVerification)
        // Tombol "Kirim Ulang Verifikasi" diklik
        buttonResendVerification.setOnClickListener {
            val email = editTextEmail.text.toString()

            if (email.isNotEmpty()) {
                progressBar.visibility = View.VISIBLE

                // Kirim ulang email verifikasi ke alamat email pengguna
                auth.currentUser?.sendEmailVerification()
                    ?.addOnCompleteListener { task ->
                        progressBar.visibility = View.GONE
                        if (task.isSuccessful) {
                            showToast("Email verifikasi telah dikirim ulang.")
                        } else {
                            showToast("Gagal mengirim email verifikasi ulang. Periksa alamat email.")
                        }
                    }
            } else {
                showToast("Masukkan alamat email terlebih dahulu.")
            }
        }
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        buttonLogin.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()

            progressBar.visibility = View.VISIBLE

            // Authenticate using Firebase Authentication
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        // Check if user's email is verified
                        if (user?.isEmailVerified == true) {
                            Log.d("verified: ","email is verified");
                            firestore.collection("users").document(user.uid)
                                .get()
                                .addOnSuccessListener { documentSnapshot ->
                                    val userRole = documentSnapshot.getString("role")

                                    // Save user role to SharedPreferences
                                    val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                                    val editor = sharedPreferences.edit()
                                    editor.putBoolean("isLogin", true)
                                    editor.putString("userRole", userRole)
                                    editor.putString("userAddress", documentSnapshot.getString("address"))
                                    editor.putString("userUid", documentSnapshot.getString("uid"))
                                    editor.putString("userName", documentSnapshot.getString("name"))
                                    editor.putString("userEmail", documentSnapshot.getString("email"))
                                    editor.apply()

                                    // Redirect to appropriate activity based on user role
                                    when (userRole) {
                                        "admin" -> startActivity(
                                            Intent(
                                                this,
                                                AdminActivity::class.java
                                            )
                                        )
                                        "user" -> startActivity(
                                            Intent(
                                                this,
                                                UserActivity::class.java
                                            )
                                        )
                                        "seller" -> startActivity(
                                            Intent(
                                                this,
                                                SellerActivity::class.java
                                            )
                                        )
                                    }
                                    finish()
                                }
                                .addOnFailureListener {
                                    progressBar.visibility = View.GONE
                                    showToast("Failed to get user role.")
                                }
                        } else {
                            progressBar.visibility = View.GONE
                            Log.d("verified: ","email not verified");
                            showToast("Please verify your email before logging in.")
                        }
                    } else {
                        progressBar.visibility = View.GONE
                        showToast("Login failed. Please check your credentials.")
                    }
                }
        }


        textViewRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        textViewRegisterSeller.setOnClickListener {
            startActivity(Intent(this, SellerRegisterActivity::class.java))
        }
        textViewForgotPassword.setOnClickListener {
            val email = editTextEmail.text.toString()
            if (email.isNotEmpty()) {
                progressBar.visibility = View.VISIBLE

                // Kirim ulang email verifikasi ke alamat email pengguna
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        progressBar.visibility = View.GONE
                        if (task.isSuccessful) {
                            showToast("Email reset telah dikirim ulang.")
                        } else {
                            showToast("Gagal mengirim email reset ulang. Periksa alamat email.")
                        }
                    }
            } else {
                showToast("Masukkan alamat email terlebih dahulu.")
            }
        }

        buttonGoogle.setOnClickListener {
            // Implement Google login
            // Handle login success and redirection
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    // Handle Google Sign In result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account?.idToken ?: "")
            } catch (e: ApiException) {
                // Google Sign In failed, handle failure
                progressBar.visibility = View.GONE
                showToast("Google Sign In failed.")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val uid = user?.uid ?: ""
                    val email = user?.email ?: ""
                    val name = user?.displayName ?: ""
                    val providerId = user?.providerId ?: ""

                    // Create a new user document in Firestore
                    val newUser = hashMapOf(
                        "uid" to uid,
                        "email" to email,
                        "name" to name,
                        "address" to "",
                        "role" to "user",
                        "provider" to providerId
                    )

                    firestore.collection("users").document(uid)
                        .set(newUser)
                        .addOnSuccessListener {
                            // User registration successful, continue with login process
                            val userRole = newUser["role"] as String

                            // Save user role to SharedPreferences
                            val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putBoolean("isLogin", true)
                            editor.putString("userRole", userRole)
                            editor.putString("userAddress", newUser["address"])
                            editor.putString("userUid", newUser["uid"])
                            editor.putString("userName", newUser["name"])
                            editor.putString("userEmail", newUser["email"])
                            editor.apply()

                            // Redirect to appropriate activity based on user role
                            when (userRole) {
                                "admin" -> startActivity(
                                    Intent(this, AdminActivity::class.java)
                                )
                                "user" -> startActivity(
                                    Intent(this, UserActivity::class.java)
                                )
                                "seller" -> startActivity(
                                    Intent(this, SellerActivity::class.java)
                                )
                            }
                            finish()
                        }
                        .addOnFailureListener {
                            progressBar.visibility = View.GONE
                            showToast("Failed to register user.")
                        }
                } else {
                    progressBar.visibility = View.GONE
                    showToast("Authentication failed.")
                }
            }
    }


    private fun showToast(message: String) {
        val rootView = findViewById<View>(android.R.id.content)
        Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show()
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}

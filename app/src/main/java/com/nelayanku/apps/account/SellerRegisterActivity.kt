package com.nelayanku.apps.account
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.nelayanku.apps.LoginActivity
import com.nelayanku.apps.MapsActivity
import com.nelayanku.apps.R
import com.nelayanku.apps.redirect.AdminActivity
import com.nelayanku.apps.tools.CustomBottomSheetDialogFragment
import com.nelayanku.apps.tools.PickerActivity
import java.util.*


class SellerRegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var buttonRegister: Button
    private lateinit var textViewLogin: TextView
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextName: EditText
    private lateinit var editTextNoHP: EditText
    private lateinit var editTextAddress: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var buttonPickAddress: TextInputLayout
    private lateinit var pinAddress: EditText
    private lateinit var tvUmum: TextView
    private lateinit var tvRegister: TextView
    private lateinit var tvOlah: TextView
    private lateinit var cbUmum: CheckBox
    private lateinit var cbRegister: CheckBox
    private lateinit var cbOlah: CheckBox

    var pickedAddress = ""
    var pickedLat = 0.0
    var pickedLng = 0.0
    private var selectedPlace: Place? = null
    private var cbUmumChecked: Boolean = false
    private var cbRegisterChecked: Boolean = false
    private var cbOlahChecked: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller_register)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        buttonRegister = findViewById(R.id.buttonRegister)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextName = findViewById(R.id.editTextName)
        editTextNoHP= findViewById(R.id.editTextNoHP)
        editTextAddress = findViewById(R.id.editTextAddress)
        pinAddress = findViewById(R.id.pinAddress)
        progressBar = findViewById(R.id.progressBar)
        buttonPickAddress = findViewById(R.id.buttonPickAddress)
        textViewLogin = findViewById(R.id.textViewLogin)
        tvUmum = findViewById(R.id.tvUmum)
        tvRegister = findViewById(R.id.tvRegister)
        tvOlah = findViewById(R.id.tvOlahan)
        cbUmum = findViewById(R.id.cbUmum)
        cbRegister = findViewById(R.id.cbRegister)
        cbOlah = findViewById(R.id.cbOlahan)
        Places.initialize(applicationContext, getString(R.string.google_maps_key))
        textViewLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        tvUmum.setOnClickListener {
            val intent = Intent(this, SkActivity::class.java)
            intent.putExtra("sk", "umum")
            startActivity(intent)
        }
        tvRegister.setOnClickListener {
            val intent = Intent(this, SkActivity::class.java)
            intent.putExtra("sk", "register")
            startActivity(intent)
        }
        tvOlah.setOnClickListener {
            val intent = Intent(this, SkActivity::class.java)
            intent.putExtra("sk", "olah")
            startActivity(intent)
        }
        buttonPickAddress.setOnClickListener {
            val intent = Intent(this, PickerActivity::class.java)
            if (pickedLat!=0.0){
                intent.putExtra("lat", pickedLat)
                intent.putExtra("lng", pickedLng)
            }
            intent.putExtra("userType", "user") // Tambahkan data tipe pengguna
            startActivityForResult(intent, PICK_LOCATION_REQUEST_CODE)
        }
        cbUmum.setOnCheckedChangeListener { _, isChecked ->
            cbUmumChecked = isChecked

        }

        cbRegister.setOnCheckedChangeListener { _, isChecked ->
            cbRegisterChecked = isChecked

        }

        cbOlah.setOnCheckedChangeListener { _, isChecked ->
            cbOlahChecked = isChecked

        }

        buttonRegister.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()
            val name = editTextName.text.toString()
            val nohp = editTextNoHP.text.toString()
            val address = pickedAddress ?: ""
            val lat = pickedLat ?: 0.0
            val lon = pickedLng ?: 0.0
            val detail =  editTextAddress.text.toString()
            if (cbUmumChecked && cbRegisterChecked && cbOlahChecked &&
                email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty() &&
                address.isNotEmpty() && lat != 0.0 && lon != 0.0) {
                progressBar.visibility = View.VISIBLE
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val uid = user?.uid ?: ""

                            val newUser = hashMapOf(
                                "uid" to uid,
                                "email" to email,
                                "name" to name,
                                "noHP" to nohp,
                                "provider" to "email",
                                "address" to address,
                                "role" to "seller",
                                "isVerified" to false,
                                "latitude" to lat,
                                "longitude" to lon,
                                "detailAddress" to detail
                            )

                            firestore.collection("users").document(uid)
                                .set(newUser)
                                .addOnSuccessListener {
                                    progressBar.visibility = View.GONE
                                    user?.sendEmailVerification()
                                        ?.addOnCompleteListener { verificationTask ->
                                            if (verificationTask.isSuccessful) {
                                                showToast("Registration successful. Verification email sent. Please verify your email.", applicationContext)
                                            } else {
                                                showToast("Registration successful. Failed to send verification email.", applicationContext)
                                            }
                                        }
                                    startActivity(
                                        Intent(
                                            this,
                                            LoginActivity::class.java
                                        ))
                                    finish()
                                }
                                .addOnFailureListener {
                                    progressBar.visibility = View.GONE
                                    showToast("Failed to register user.",applicationContext)
                                }

                            val profileUpdates = UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build()
                            user?.updateProfile(profileUpdates)
                        } else {
                            progressBar.visibility = View.GONE
                            showToast("Registration failed. Please try again.",applicationContext)
                        }
                    }
            }else {
                showToast("Please fill in all required fields and pick an address.",applicationContext)
            }
        }
    }

    private fun showPlacePicker() {
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
            .build(this)
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val place = Autocomplete.getPlaceFromIntent(data!!)
            selectedPlace = place
            editTextAddress.setText(place.address)
        }
        if (requestCode == PICK_LOCATION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Ambil data yang dikirim kembali dari PickerActivity
            pickedAddress = data?.getStringExtra("pickedAddress") ?: ""
            pickedLat = data?.getDoubleExtra("pickedLat",0.0) ?: 0.0
            pickedLng = data?.getDoubleExtra("pickedLng",0.0) ?: 0.0
        }
    }

    private fun showToast(message: String,context : Context) {
        val rootView = findViewById<View>(android.R.id.content)
        Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show()
    }

    companion object {
        private const val AUTOCOMPLETE_REQUEST_CODE = 1001
        private const val PICK_LOCATION_REQUEST_CODE = 1002
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("email", editTextEmail.text.toString())
        outState.putString("password", editTextPassword.text.toString())
        outState.putString("name", editTextName.text.toString())
        outState.putString("noHP", editTextNoHP.text.toString())
        outState.putString("detail", editTextAddress.text.toString())
        outState.putDouble("lat", pickedLat)
        outState.putDouble("lng", pickedLng)
        outState.putString("address", pickedAddress)
        // Simpan status checkbox
        outState.putBoolean("cbUmumChecked", cbUmum.isChecked)
        outState.putBoolean("cbRegisterChecked", cbRegister.isChecked)
        outState.putBoolean("cbOlahChecked", cbOlah.isChecked)
    }
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        editTextEmail.setText(savedInstanceState.getString("email"))
        editTextPassword.setText(savedInstanceState.getString("password"))
        editTextName.setText(savedInstanceState.getString("name"))
        editTextNoHP.setText(savedInstanceState.getString("noHP"))
        editTextAddress.setText(savedInstanceState.getString("detail"))
        if (pickedLat==0.0){
            pickedLat=savedInstanceState.getDouble("lat")
            pickedLng=savedInstanceState.getDouble("lng")
            pickedAddress=savedInstanceState.getString("address")?:""
        }else{
            pinAddress.setText(pickedAddress)
        }
        // Mengembalikan status checkbox
        cbUmumChecked = savedInstanceState.getBoolean("cbUmumChecked")
        cbRegisterChecked = savedInstanceState.getBoolean("cbRegisterChecked")
        cbOlahChecked = savedInstanceState.getBoolean("cbOlahChecked")

        // Set status checkbox sesuai dengan nilai yang disimpan
        cbUmum.isChecked = cbUmumChecked
        cbRegister.isChecked = cbRegisterChecked
        cbOlah.isChecked = cbOlahChecked
    }


}


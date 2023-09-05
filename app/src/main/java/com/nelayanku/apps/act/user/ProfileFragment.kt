package com.nelayanku.apps.act.user

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nelayanku.apps.LoginActivity
import com.nelayanku.apps.R
import com.nelayanku.apps.model.UserDetail
import com.nelayanku.apps.tools.PickerActivity

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    lateinit var mainContent : RelativeLayout
    private lateinit var buttonRegister: Button
    private lateinit var editTextEmail: EditText
    private lateinit var btnPassword: Button
    private lateinit var editTextName: EditText
    private lateinit var editTextAddress: EditText
    private lateinit var progressDialog: ProgressDialog
    private lateinit var buttonPickAddress: TextInputLayout
    private lateinit var pinAddress: EditText
    private lateinit var btnLogout: ImageView
    var pickedAddress = ""
    var pickedLat = 0.0
    var pickedLng = 0.0
    private var selectedPlace: Place? = null

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
        return inflater.inflate(R.layout.fragment_profile_user, container, false)
    }
    override fun onViewCreated(itemView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(itemView, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Mengambil data profil...")
        progressDialog.setCancelable(false)
        initial(itemView)
        klik(itemView)
        fetchAndFillUserData()
    }
    companion object {
        private const val AUTOCOMPLETE_REQUEST_CODE = 1001
        private const val PICK_LOCATION_REQUEST_CODE = 1002
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    fun initial(view : View){
        mainContent = view.findViewById(R.id.mainContent)
        buttonRegister = view.findViewById(R.id.buttonRegister)
        editTextEmail = view.findViewById(R.id.editTextEmail)
        btnPassword = view.findViewById(R.id.btnPassword)
        editTextName = view.findViewById(R.id.editTextName)
        editTextAddress = view.findViewById(R.id.editTextAddress)
        pinAddress = view.findViewById(R.id.pinAddress)
        buttonPickAddress = view.findViewById(R.id.buttonPickAddress)
        btnLogout = view.findViewById(R.id.btnLogout)
    }
    fun klik(view: View){
        Places.initialize(requireContext(), getString(R.string.google_maps_key))
        buttonPickAddress.setOnClickListener {
            val intent = Intent(requireContext(), PickerActivity::class.java)
            if (pickedLat!=0.0){
                intent.putExtra("lat", pickedLat)
                intent.putExtra("lng", pickedLng)
            }
            intent.putExtra("userType", "user") // Tambahkan data tipe pengguna
            startActivityForResult(intent, PICK_LOCATION_REQUEST_CODE)
        }
        buttonRegister.setOnClickListener {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            uid?.let { updateData(view,it) }
        }
        btnPassword.setOnClickListener {
            val email = editTextEmail.text.toString()
            if (email.isNotEmpty()) {
                progressDialog.setMessage("Mengirim request...")
                progressDialog.show()

                // Kirim ulang email verifikasi ke alamat email pengguna
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        progressDialog.hide()
                        if (task.isSuccessful) {
                            showToast(view,"Email reset telah dikirim ulang.")
                        } else {
                            showToast(view,"Gagal mengirim email reset ulang. Periksa alamat email.")
                        }
                    }
            } else {
                showToast(view,"Masukkan alamat email terlebih dahulu.")
            }
        }
        btnLogout.setOnClickListener {
            auth.signOut()
            //hapus sharedpreferences
            val sharedPreferences = requireActivity().getSharedPreferences("user",
                Context.MODE_PRIVATE
            )
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()

            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }
    fun updateData(view: View, uid : String){
        val email = editTextEmail.text.toString()
        val name = editTextName.text.toString()
        val address = pickedAddress ?: ""
        val lat = pickedLat ?: 0.0
        val lon = pickedLng ?: 0.0
        val detail =  editTextAddress.text.toString()
        if (email.isNotEmpty() && name.isNotEmpty() && address.isNotEmpty() && lat != 0.0 && lon != 0.0) {
            progressDialog.setMessage("Mengupdate data profil...")
            progressDialog.show()
            val newUser = hashMapOf(
                "email" to email,
                "name" to name,
                "address" to address,
                "isVerified" to true,
                "latitude" to lat,
                "longitude" to lon,
                "detailAddress" to detail,
            )
            val db = FirebaseFirestore.getInstance()
            // Update the product data in Firestore
            db.collection("users")
                .document(uid) // Gunakan productId yang ada untuk merujuk dokumen yang ingin diubah
                .update(newUser as Map<String, Any>)
                .addOnSuccessListener {
                    Snackbar.make(mainContent, "Profil berhasil diubah", Snackbar.LENGTH_SHORT).show()
                }
            progressDialog.hide()
        }else {
            showToast(view,"Please fill in all required fields and pick an address.")
        }

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
            pinAddress.setText(pickedAddress)
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("email", editTextEmail.text.toString())
        outState.putString("name", editTextName.text.toString())
        outState.putString("detail", editTextAddress.text.toString())
        outState.putDouble("lat", pickedLat)
        outState.putDouble("lng", pickedLng)
        outState.putString("address", pickedAddress)
    }
    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null) {
            editTextEmail.setText(savedInstanceState.getString("email"))
            editTextName.setText(savedInstanceState.getString("name"))
            editTextAddress.setText(savedInstanceState.getString("detail"))
            if (pickedLat == 0.0) {
                pickedLat = savedInstanceState.getDouble("lat")
                pickedLng = savedInstanceState.getDouble("lng")
                pickedAddress = savedInstanceState.getString("address") ?: ""
            } else {
                pinAddress.setText(pickedAddress)
            }
        }
    }
    private fun fetchAndFillUserData() {
        progressDialog.show()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val uid = currentUser.uid
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val userData = documentSnapshot.toObject(UserDetail::class.java)
                        userData?.let { user ->
                            editTextEmail.setText(user.email)
                            editTextName.setText(user.name)
                            editTextAddress.setText(user.address)
                            pinAddress.setText(user.address)
                            editTextAddress.setText(user.detailAddress)
                            pickedAddress = user.address ?: ""
                             pickedLat = user.latitude
                             pickedLng = user.longitude
                        }
                    } else {
                        // Handle case when user data doesn't exist
                    }
                    progressDialog.hide()
                }
                .addOnFailureListener { exception ->
                    // Handle failure to fetch user data
                    progressDialog.hide()
                }
        } else {
            // Handle case when current user is null
            progressDialog.hide()
        }
    }
    private fun showToast(view: View, message: String) {
        Snackbar.make(mainContent, message, Snackbar.LENGTH_SHORT).show()
    }
}
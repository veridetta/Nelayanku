package com.nelayanku.apps.act.admin

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
import com.nelayanku.apps.model.SettingModel
import com.nelayanku.apps.model.UserDetail
import com.nelayanku.apps.tools.PickerActivity

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    lateinit var mainContent : RelativeLayout
    private lateinit var btnSimpan: Button
    private lateinit var editTextLPenjual: EditText
    private lateinit var editTextLPembeli: EditText
    private lateinit var editTextRadius: EditText
    private lateinit var progressDialog: ProgressDialog
    private lateinit var btnPassword: Button
    private lateinit var btnLogout: ImageView

    var pickedAddress = ""
    var pickedLat = 0.0
    var pickedLng = 0.0
    var documentId = ""
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
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }
    override fun onViewCreated(itemView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(itemView, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Mengambil data settings...")
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
            SettingsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    fun initial(view : View){
        mainContent = view.findViewById(R.id.mainContent)
        btnSimpan = view.findViewById(R.id.btnSimpan)
        editTextLPenjual = view.findViewById(R.id.editTextLPenjual)
        editTextLPembeli = view.findViewById(R.id.editTextLPembeli)
        editTextRadius = view.findViewById(R.id.editTextRadius)
        btnPassword = view.findViewById(R.id.btnPassword)
        btnLogout = view.findViewById(R.id.btnLogout)
    }
    fun klik(view: View){
        Places.initialize(requireContext(), getString(R.string.google_maps_key))
        btnSimpan.setOnClickListener {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            uid?.let { updateData(view,documentId) }
        }
        btnPassword.setOnClickListener {
            //ambil email dari sharedpreferences
            val sharedPreferences = requireActivity().getSharedPreferences("MyPrefs",
                Context.MODE_PRIVATE
            )
            val email = sharedPreferences.getString("userEmail", "")?:""
            if (email!="") {
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
    fun updateData(view: View, docId : String){
        val lPenjual =editTextLPenjual.text.toString()
        val lPembeli = editTextLPembeli.text.toString()
        val radius = editTextRadius.text.toString()
        if (lPenjual.isNotEmpty() && lPembeli.isNotEmpty() && radius.isNotEmpty()) {
            progressDialog.setMessage("Mengupdate data settings...")
            progressDialog.show()
            val newUser = hashMapOf(
                "layananPenjual" to lPenjual,
                "layananPembeli" to lPembeli,
                "radius" to radius
            )
            val db = FirebaseFirestore.getInstance()
            // Update the product data in Firestore
            db.collection("setting")
                .document(docId) // Gunakan productId yang ada untuk merujuk dokumen yang ingin diubah
                .update(newUser as Map<String, Any>)
                .addOnSuccessListener {
                    Snackbar.make(mainContent, "Settings berhasil diubah", Snackbar.LENGTH_SHORT).show()
                }
            progressDialog.hide()
        }else {
            showToast(view,"Please fill in all required fields.")
        }

    }

    private fun fetchAndFillUserData() {
        progressDialog.show()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            firestore.collection("setting").whereEqualTo("uid", uid).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.isEmpty) {
                        // Handle case when user data doesn't exist
                    } else {
                        // Handle case when user data exists
                        documentId = documentSnapshot.documents[0].id
                        val userData = documentSnapshot.documents[0].toObject(SettingModel::class.java)
                        userData?.let { user ->
                            editTextLPenjual.setText(user.layananPenjual)
                            editTextLPembeli.setText(user.layananPembeli)
                            editTextRadius.setText(user.radius)
                        }
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
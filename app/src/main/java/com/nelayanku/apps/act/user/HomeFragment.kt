package com.nelayanku.apps.act.user
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nelayanku.apps.R
import com.nelayanku.apps.act.seller.InfoActivity
import com.nelayanku.apps.act.seller.LayananActivity
import com.nelayanku.apps.adapter.user.ProductAdapter
import com.nelayanku.apps.model.Product
import com.nelayanku.apps.model.SettingModel
import com.nelayanku.apps.model.UserDetail
import com.nelayanku.apps.tools.Const.PATH_COLLECTION
import com.nelayanku.apps.tools.hitungJarak
import com.nelayanku.apps.tools.ongkir
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private val mFirestore = FirebaseFirestore.getInstance()
    private lateinit var productAdapter: ProductAdapter
    private lateinit var recyclerView: RecyclerView
    //btnLayanan dan btnInfo
    private lateinit var btnLayanan: LinearLayout
    private lateinit var btnInfo: LinearLayout

    val TAG = "LOAD DATA"
    private val productList: MutableList<Product> = mutableListOf()
    var documentId = ""
    var shimmerContainer: ShimmerFrameLayout? = null
    //lat long pembeli
    var latPembeli : Double = 0.0
    var longPembeli : Double = 0.0
    //lat long penjual
    var latPenjual : Double = 0.0
    var longPenjual : Double = 0.0
    //jarak
    var jarak : Double = 0.0
    var ongkir : Double = 0.0
    var uid = FirebaseAuth.getInstance().currentUser?.uid
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home_user, container, false)
    }
    override fun onViewCreated(itemView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(itemView, savedInstanceState)
        recyclerView = itemView.findViewById(R.id.recyclerViewProducts)
        btnLayanan = itemView.findViewById(R.id.btn_layanan)
        btnInfo = itemView.findViewById(R.id.btn_info)
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(activity, 2)
            // set the custom adapter to the RecyclerView
            productAdapter = ProductAdapter(
                productList,
                requireContext()
            ) { product -> lanjutPembayaran(product) }
        }
        shimmerContainer = itemView.findViewById<ShimmerFrameLayout>(R.id.shimmerContainer)
        readDataUserNow(mFirestore)
        readDataUser(mFirestore)
        getSetting()
        recyclerView.adapter = productAdapter
        val searchEditText = itemView.findViewById<EditText>(R.id.btnCari)
        productAdapter.filter("")
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

                    productAdapter.filter(s.toString())

            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        btnLayanan.setOnClickListener {
            val intent = Intent(requireContext(), LayananActivity::class.java)
            startActivity(intent)
        }
        btnInfo.setOnClickListener {
            val intent = Intent(requireContext(), InfoActivity::class.java)
            startActivity(intent)
        }
    }
    private fun readData(db: FirebaseFirestore, shimmerContainer: ShimmerFrameLayout,uid: String) {
        shimmerContainer.startShimmer() // Start shimmer effect
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val result = db.collection(PATH_COLLECTION)
                    .whereEqualTo("status", "published").get().await()
                val products = mutableListOf<Product>()
                for (document in result) {
                    val product = document.toObject(Product::class.java)
                    //ambil data penjual dari penjualList
                    for (penjual in penjualList){
                        if (product.uid == penjual.uid){
                            latPenjual = penjual.latitude.toString().toDouble()
                            longPenjual = penjual.longitude.toString().toDouble()
                        }
                    }
                    jarak = hitungJarak(latPembeli,longPembeli,latPenjual,longPenjual)
                    //log jarak
                    Log.d(TAG, "Jarak : $jarak")
                    //log radius
                    Log.d(TAG, "Radius : $radius")
                    if (jarak <= radius.toDouble()){
                        ongkir = ongkir(jarak)
                        //log
                        Log.d(TAG, "Ongkir : $ongkir")
                        product.ongkir = ongkir.toString()
                        product.layananPembeli = layananPembeli
                        product.layananPenjual = layananPenjual
                        product.radius = radius
                        products.add(product)
                        //log product
                        Log.d(TAG, "Product : ${product.ongkir + product.layananPembeli + product.layananPenjual + product.radius}")
                    }
                    Log.d(TAG, "Datanya : ${document.id} => ${document.data}")
                }
                withContext(Dispatchers.Main) {
                    productList.addAll(products)
                    productAdapter.filteredProductList.addAll(products)
                    productAdapter.notifyDataSetChanged()
                    shimmerContainer.stopShimmer() // Stop shimmer effect
                    shimmerContainer.visibility = View.GONE // Hide shimmer container
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error getting documents : $e")
                //stop shimmer gunakan runOnUiThread
                activity?.runOnUiThread {
                    shimmerContainer.stopShimmer() // Stop shimmer effect
                    shimmerContainer.visibility = View.GONE // Hide shimmer container
                }
            }
        }
    }
    private fun lanjutPembayaran(product: Product) {
        val intent = Intent(requireContext(), PembayaranActivity::class.java)
        intent.putExtra("productId", product.productId) // Mengirim productId ke EditProductActivity
        //nama dan harga
        intent.putExtra("namaBarang", product.name)
        intent.putExtra("hargaBarang", product.price)
        intent.putExtra("ongkir", product.ongkir)
        intent.putExtra("layananPembeli", product.layananPembeli)
        //uid
        intent.putExtra("uid", product.uid)
        startActivity(intent)
    }
    //ambil settingan dari firebase
    var layananPembeli =""
    var radius =""
    var layananPenjual =""
    private fun getSetting() {
        //firestore
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("setting").get()
            .addOnSuccessListener { documentSnapshot ->

                val uid = FirebaseAuth.getInstance().currentUser?.uid
                uid?.let { readData(mFirestore, shimmerContainer!!, it) }
                //log uid
                Log.d(TAG, "UID : $uid")
                if (documentSnapshot.isEmpty) {
                    // Handle case when user data doesn't exist
                } else {
                    // Handle case when user data exists
                    documentId = documentSnapshot.documents[0].id
                    val userData = documentSnapshot.documents[0].toObject(SettingModel::class.java)
                    userData?.let { user ->
                        radius = user.radius.toString()
                        layananPembeli = user.layananPembeli.toString()
                        layananPenjual = user.layananPenjual.toString()
                    }
                }
            }
            .addOnFailureListener { exception ->

                //log gagal
                Log.e("TAG", "get failed with ", exception)
            }
    }
    //buat untuk store data user
    var penjualList: MutableList<UserDetail> = mutableListOf()
    private fun readDataUser(db: FirebaseFirestore) {
        //tambahkan progress dialog
        //read data dari firebase
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val result = db.collection("users").whereEqualTo("role", "seller").get().await()
                for (document in result) {
                    val user = document.toObject(UserDetail::class.java)
                    penjualList.add(user)
                    Log.d(TAG, "Datanya : ${document.id} => ${document.data}")
                }
                withContext(Dispatchers.Main) {

                }
            } catch (e: Exception) {
                Log.w(TAG, "Error getting documents : $e")
                //stop shimmer gunakan runOnUiThread
                activity?.runOnUiThread {

                }
            }
        }
    }
    //ambil data user saat ini
    private fun readDataUserNow(db: FirebaseFirestore) {
        //read data dari firebase
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val result = db.collection("users").whereEqualTo("uid", uid).get().await()
                for (document in result) {
                    val user = document.toObject(UserDetail::class.java)
                    latPembeli = user.latitude.toString().toDouble()
                    longPembeli = user.longitude.toString().toDouble()
                    Log.d(TAG, "Datanya : ${document.id} => ${document.data}")
                }
                withContext(Dispatchers.Main) {

                }
            } catch (e: Exception) {
                Log.w(TAG, "Error getting documents : $e")

            }
        }
    }
}

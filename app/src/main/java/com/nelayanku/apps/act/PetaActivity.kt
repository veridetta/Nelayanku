package com.nelayanku.apps.act

// Import Google Maps

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.nelayanku.apps.R
import java.util.Locale


class PetaActivity : AppCompatActivity() {
    private lateinit var googleMap: GoogleMap
    private val firestore = FirebaseFirestore.getInstance()
    private val collectionName = "peta" // Ganti dengan nama koleksi Firestore Anda
    private lateinit var customMarker: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_peta)

        // Inisialisasi Google Maps
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { map ->
            googleMap = map
            googleMap.uiSettings.isZoomControlsEnabled = true
            // Ambil data dari Firestore dan tampilkan di peta
            firestore.collection(collectionName)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val markers = ArrayList<Marker>()

                        for (document in task.result!!) {
                            val lat = document.getDouble("lat")
                            val lng = document.getDouble("long")
                            val gmaps = document.getString("gmaps")
                            val jenis = document.getString("jenis")
                            //custom marker berdasarkan jenis
                            var snippet = ""
                            var ket = ""
                            if(jenis == "Potensi"){
                                val height = 40
                                val width = 40
                                val bitmapdraw = resources.getDrawable(R.drawable.fish) as BitmapDrawable
                                val b = bitmapdraw.bitmap
                                 customMarker = Bitmap.createScaledBitmap(b, width, height, false)
                                 ket = "Daerah Penangkapan Ikan"
                                //tambahkan baris baru dan gabungkan dengan gmaps
                                 snippet =  "Url Google Maps : "+gmaps
                            }else if(jenis == "Tangkap"){
                                //daerah potensi ikan
                                 ket = "Daerah Penangkapan Ikan"
                                //tambahkan baris baru dan gabungkan dengan gmaps
                                snippet = "Url Google Maps : "+gmaps
                                val height = 40
                                val width = 40
                                val bitmapdraw = resources.getDrawable(com.nelayanku.apps.R.drawable.area) as BitmapDrawable
                                val b = bitmapdraw.bitmap
                                 customMarker = Bitmap.createScaledBitmap(b, width, height, false)
                            }
                            val latLng = LatLng(lat!!, lng!!)
                            //perkecil ukuran marker
                            //ambil nama tempat dari lat dan long
                            val marker = googleMap.addMarker(
                                MarkerOptions()
                                    .position(latLng)
                                    .title(ket)
                                    .snippet(snippet)
                                    .icon(BitmapDescriptorFactory.fromBitmap(customMarker))
                                    .anchor(0.5f, 0.5f)
                            )
                            marker?.let { markers.add(it) }
                        }

                        // Zoom peta agar semua marker terlihat
                        if (markers.isNotEmpty()) {
                            val builder = LatLngBounds.Builder()
                            for (marker in markers) {
                                builder.include(marker.position)
                            }
                            val bounds = builder.build()
                            val padding = 100 // margin dari tepi peta (opsional)
                            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                            googleMap.animateCamera(cameraUpdate)
                        }
                    } else {
                        // Handle kesalahan saat mengambil data dari Firestore
                        Log.w("PetaActivity", "Error getting documents.", task.exception)
                    }
                }
        }
    }
}

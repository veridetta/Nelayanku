package com.nelayanku.apps.tools

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.nelayanku.apps.R
import com.nelayanku.apps.account.RegisterActivity
import com.nelayanku.apps.mapsApi.Item
import com.nelayanku.apps.mapsApi.MapInstance
import com.nelayanku.apps.mapsApi.SearcMapInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class PickerActivity : AppCompatActivity() {

    private var malangLatLng = LatLng(-7.977447, 112.634796)
    private val retrofitInstance = MapInstance.create()
    private val searcInstance = SearcMapInstance.create()
    var address =""

    private var hasFetch = false
    private var animateMarker = true
    var userType = "user"
    var lat = 0.0
    var lng = 0.0
    lateinit var progresscircular : ProgressBar
    lateinit var mapview : SupportMapFragment
    lateinit var iconmarker : ImageView
    lateinit var iconmarkershadow : ImageView
    lateinit var texttitle : TextView
    lateinit var textaddress : TextView
    lateinit var searchView : androidx.appcompat.widget.SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picker)
        userType = intent.getStringExtra("userType") ?: "user"
        val lastLat = intent.getDoubleExtra("lat",0.0) ?: 0.0
        val lastLng = intent.getDoubleExtra("lng",0.0) ?: 0.0
        if(lastLat!==0.0){
            malangLatLng = LatLng(lastLat, lastLng)
        }
        progresscircular = findViewById(R.id.progress_circular)
        iconmarker = findViewById(R.id.icon_marker)
        iconmarkershadow = findViewById(R.id.icon_marker_shadow)
        texttitle = findViewById(R.id.text_title)
        textaddress = findViewById(R.id.text_address)
        mapview = supportFragmentManager.findFragmentById(R.id.map_view) as SupportMapFragment
        searchView = findViewById<androidx.appcompat.widget.SearchView>(R.id.search_view)
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    GlobalScope.launch(Dispatchers.Main) {
                        val item = withContext(Dispatchers.IO) {
                            searchLocation(it)
                        }
                        item?.let {
                            val latLng = LatLng(it.position.lat, it.position.lng)
                            setCamera(latLng)
                        }
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Tambahkan logika untuk menangani perubahan teks saat pengguna mengetik di sini
                return true
            }
        })

        val pickLocationButton = findViewById<Button>(R.id.button_pick_location)

        // Menambahkan listener onClick ke tombol "Pilih Lokasi"
        pickLocationButton.setOnClickListener {
            onButtonPickLocationClick()
        }
        progresscircular.visibility = View.GONE
        setCamera(malangLatLng)
    }

    fun setCamera(latLng: LatLng){
        val bottomSheet = BottomSheetBehavior.from(findViewById<View>(R.id.bottom_sheet))
        bottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
        (mapview as SupportMapFragment).getMapAsync { map ->
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))

            val oldPosition = map.cameraPosition.target

            map.setOnCameraMoveStartedListener {
                // drag started
                if (animateMarker) {
                    bottomSheet.state = BottomSheetBehavior.STATE_HIDDEN

                    iconmarker.animate().translationY(-50f).start()
                    iconmarkershadow.animate().withStartAction {
                        iconmarkershadow.setPadding(10)
                    }.start()
                }

                hasFetch = false
            }

            map.setOnCameraIdleListener {
                val newPosition = map.cameraPosition.target
                if (newPosition != oldPosition) {
                    // drag ended
                    iconmarker.animate().translationY(0f).start()
                    iconmarkershadow.animate().withStartAction {
                        iconmarkershadow.setPadding(0)
                    }.start()

                    getLocation(newPosition) { item ->
                        bottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
                        val position = item.position
                        val findLocation = LatLng(position.lat, position.lng)

                        map.animateCamera(CameraUpdateFactory.newLatLng(findLocation), 200,
                            object : GoogleMap.CancelableCallback {
                                override fun onFinish() {
                                    hasFetch = true
                                    animateMarker = true
                                }

                                override fun onCancel() {
                                    animateMarker = true
                                }

                            })

                        val titlePlace = item.title
                        address = item.address.label
                        lat = item.position.lat
                        lng = item.position.lng
                        texttitle.text = titlePlace
                        textaddress.text = address
                    }
                }
            }
        }
    }
    private fun getLocation(latLng: LatLng, done: (Item) -> Unit) {
        val at = "${latLng.latitude},${latLng.longitude}"
        if (!hasFetch) {
            animateMarker = false
            progresscircular.visibility = View.VISIBLE
            GlobalScope.launch {
                try {
                    val places = retrofitInstance.getLocation(at).items
                    runOnUiThread {
                        if (places.isNotEmpty()) {
                            progresscircular.visibility = View.GONE
                            done.invoke(places.first())
                            hasFetch = false
                            animateMarker = true
                        }
                    }
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
    }
    private suspend fun searchLocation(query: String): Item? {
        return try {
            val response = searcInstance.getSearch(query)
            response.items.firstOrNull()
        } catch (e: HttpException) {
            e.printStackTrace()
            null
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }
    // Function to handle the button click event
    fun onButtonPickLocationClick() {
        val intent = Intent()
        if (userType == "user") {
            intent.putExtra("pickedAddress", address)
            intent.putExtra("pickedLat", lat)
            intent.putExtra("pickedLng", lng)
            setResult(Activity.RESULT_OK, intent)
            finish()
        } else {
            intent.putExtra("pickedAddress", address)
            intent.putExtra("pickedLat", lat)
            intent.putExtra("pickedLng", lng)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

}
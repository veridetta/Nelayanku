package com.nelayanku.apps.act.admin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import com.nelayanku.apps.R

class BuktiActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bukti)
        //ambil url dari intent
        val url = intent.getStringExtra("url")
        //tampilkan gambar dari url
        val photoView: PhotoView = findViewById(R.id.photoView)

        // Load image using your preferred method
        Glide.with(this)
            .load(url)
            .into(photoView)
    }
}
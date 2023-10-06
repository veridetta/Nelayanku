package com.nelayanku.apps.tools

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity

fun showConfirmationDialog(context: Context) {
    val builder = AlertDialog.Builder(context)
    builder.setTitle("Konfirmasi Keluar")
    builder.setMessage("Apakah Anda ingin keluar dari aplikasi?")
    builder.setPositiveButton("Ya") { _: DialogInterface, _: Int ->
        // Menutup aktivitas utama (activity) saat pengguna memilih "Ya"
        (context as AppCompatActivity).finish()
    }
    builder.setNegativeButton("Tidak") { dialog: DialogInterface, _: Int ->
        dialog.dismiss()
    }
    builder.setOnCancelListener {
        // Aksi yang diambil jika pengguna menekan tombol kembali perangkat
        // Misalnya, jika Anda ingin tetap di dalam aplikasi
    }
    builder.show()
}
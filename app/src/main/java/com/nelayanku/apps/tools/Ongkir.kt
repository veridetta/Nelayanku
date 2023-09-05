package com.nelayanku.apps.tools

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

//jadikan fungsi if, yang return double
fun ongkir(jarak : Double) : Double{
    //aturan <= 50km ongkir 100rb, <= 60km ongkir 110rb <= 100km 150rb
    if (jarak <= 50000){
        return 100000.0
    }else if (jarak <= 60000){
        return 110000.0
    }else if (jarak <= 100000){
        return 150000.0
    }else{
        return 0.0
    }
}
//fungsi hitung jarak
fun hitungJarak(lat1 : Double, lon1 : Double, lat2 : Double, lon2 : Double) : Double{
    val R = 6371 // Radius of the earth in km
    val dLat = deg2rad(lat2-lat1)  // deg2rad below
    val dLon = deg2rad(lon2-lon1)
    val a =
        Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
                Math.sin(dLon/2) * Math.sin(dLon/2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))
    val d = R * c // Distance in km
    return d
}
//fungsi deg2rad
fun deg2rad(deg: Double): Double {
    return deg * (Math.PI/180)
}
fun formatCurrency(price: Double): String {
    val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return numberFormat.format(price)
}

fun formatDate(tanggal : String) : String{
    val inputDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
    val date = inputDateFormat.parse(tanggal)
    // Buat objek SimpleDateFormat untuk mengubah tanggal ke format baru "dd MMMM yyyy"
    val outputDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    // Konversi tanggal
    val outputDateString = outputDateFormat.format(date)
    return outputDateString
}
package com.nelayanku.apps.adapter.chat
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nelayanku.apps.R
import com.nelayanku.apps.model.ChatHeaderModel
import com.nelayanku.apps.model.Informasi
import com.nelayanku.apps.model.Product
import com.nelayanku.apps.tools.readChatHeader
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ChatHeaderAdapter(
    private var productList: MutableList<ChatHeaderModel>,
    val context: Context,
    private val listener: (ChatHeaderModel) -> Unit
) : RecyclerView.Adapter<ChatHeaderAdapter.InfoViewHolder>() {
    public var filteredProductList: MutableList<ChatHeaderModel> = mutableListOf()
    init {
        filteredProductList.addAll(productList)
    }
    override fun getItemViewType(position: Int): Int {
        return if (position == 0 && filteredProductList.isEmpty()) {
            1 // Return 1 for empty state view
        } else {
            0 // Return 0 for regular product view
        }
    }
    fun filter(query: String) {
        filteredProductList.clear()
        if (query !== null || query !=="") {
            val lowerCaseQuery = query.toLowerCase(Locale.getDefault())
            for (product in productList) {
                val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                val role = sharedPreferences.getString("userRole", "")
                if (role == "user") {
                    val nam = product.nameSeller?.toLowerCase(Locale.getDefault())?.contains(lowerCaseQuery)
                    Log.d("Kunci ", lowerCaseQuery)
                    if (nam == true) {
                        filteredProductList.add(product)
                        Log.d("Ada ", product.nameSeller.toString())
                    }
                } else {
                    val nam = product.nameUser?.toLowerCase(Locale.getDefault())?.contains(lowerCaseQuery)
                    Log.d("Kunci ", lowerCaseQuery)
                    if (nam == true) {
                        filteredProductList.add(product)
                        Log.d("Ada ", product.nameUser.toString())
                    }
                }
            }
        } else {
            filteredProductList.addAll(productList)
        }
        notifyDataSetChanged()
        Log.d("Data f",filteredProductList.size.toString())
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_list, parent, false)
        return InfoViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return filteredProductList.size
    }
    private fun formatCurrency(price: Double): String {
        val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return numberFormat.format(price)
    }
    override fun onBindViewHolder(holder: InfoViewHolder, position: Int) {
        val currentProduct = filteredProductList[position]
        holder.tvId.text = currentProduct.uid
        //ambil role dari sharedpreferences
        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val role = sharedPreferences.getString("userRole", "")
        if (role == "user") {
            holder.tvName.text = currentProduct.nameSeller
        } else {
            holder.tvName.text = currentProduct.nameUser
        }

        val uidUser = sharedPreferences.getString("userUid", "")
        if(currentProduct.lastSender==uidUser){
            holder.tvUnread.visibility = View.GONE
        }else{
            if (currentProduct.unread!! > 0) {
                holder.tvUnread.visibility = View.VISIBLE
                holder.tvUnread.text = currentProduct.unread.toString() + " pesan baru"
            } else {
                holder.tvUnread.visibility = View.GONE
            }
        }
        //check type
        if (currentProduct.type == "text") {
            //atur agar lastchat maksimal 50 karakter saja
            if (currentProduct.lastChat!!.length > 50) {
                holder.tvLastChat.text = currentProduct.lastChat!!.substring(0, 50) + "..."
            } else {
                holder.tvLastChat.text = currentProduct.lastChat
            }
        } else if(currentProduct.type == "image"){
            holder.tvLastChat.text = "Gambar"
        }else{
            holder.tvLastChat.text = "Product"
        }

        //atur agar tanggal format seperti ini Sabtu, 20-03-2021
        val tanggal = currentProduct.lastTanggal!!.split("/")
        val bulan = when (tanggal[1]) {
            "01" -> "Januari"
            "02" -> "Februari"
            "03" -> "Maret"
            "04" -> "April"
            "05" -> "Mei"
            "06" -> "Juni"
            "07" -> "Juli"
            "08" -> "Agustus"
            "09" -> "September"
            "10" -> "Oktober"
            "11" -> "November"
            "12" -> "Desember"
            else -> "Januari"
        }
        //tambahkan hari ke tanggal
        val hari = currentProduct.lastTanggal!!.split("/")
        val tanggalLengkap = hari[0] + "-" + hari[1] + "-" + hari[2]
        val hariLengkap = getDayName(tanggalLengkap)
        holder.tvTanggal.text = hariLengkap + ", " + tanggal[0] + " " + bulan + " " + tanggal[2]
        //jam pakai getTimeAgo
        val currentTime = System.currentTimeMillis()
        holder.tvJam.text = getTimeAgo(convertToMillis(currentProduct.lastTanggal!!, currentProduct.lastJam!!))
        holder.cardView.setOnClickListener {
            listener(currentProduct)
        }
        //cek unread

    }
    fun getTimeAgo(targetTime: Long): String {
        val currentTime = System.currentTimeMillis()
        val elapsedTimeInSeconds = (currentTime - targetTime) / 1000

        return when {
            elapsedTimeInSeconds < 60 -> "$elapsedTimeInSeconds detik yang lalu"
            elapsedTimeInSeconds < 3600 -> "${elapsedTimeInSeconds / 60} menit yang lalu"
            elapsedTimeInSeconds < 86400 -> "${elapsedTimeInSeconds / 3600} jam yang lalu"
            else -> {
                // Jika lebih dari 1 hari, tampilkan jam biasa
                val sdf = SimpleDateFormat("HH:mm")
                sdf.format(Date(targetTime))
            }
        }
    }
    fun getDayName(date: String): String {
        val inputDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale("id", "ID"))
        val dateObject = inputDateFormat.parse(date)
        val outputDateFormat = SimpleDateFormat("EEEE", Locale("id", "ID"))

        return outputDateFormat.format(dateObject)
    }
    fun convertToMillis(tanggal:String, jam:String): Long {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        val date = sdf.parse(tanggal+" "+jam)
        return date.time
    }
    inner class InfoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvId: TextView = itemView.findViewById(R.id.tvChatId)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvLastChat: TextView = itemView.findViewById(R.id.tvChat)
        val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggal)
        val tvJam: TextView = itemView.findViewById(R.id.tvJam)
        val tvUnread: TextView = itemView.findViewById(R.id.tvUnread)
        val cardView: CardView = itemView.findViewById(R.id.cardView)
    }
}

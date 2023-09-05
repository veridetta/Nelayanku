package com.nelayanku.apps.adapter.seller
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.nelayanku.apps.R
import com.nelayanku.apps.model.Wallet
import com.nelayanku.apps.tools.formatDate
import java.text.NumberFormat
import java.util.Locale


class WalletAdapter(
    private var productList: MutableList<Wallet>,
    val context: Context
) : RecyclerView.Adapter<WalletAdapter.WalletViewHolder>() {
    public var filteredProductList: MutableList<Wallet> = mutableListOf()
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
        filteredProductList.addAll(productList)
        notifyDataSetChanged()
        Log.d("Data f",filteredProductList.size.toString())
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wallet, parent, false)
        return WalletViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return filteredProductList.size
    }
    private fun formatCurrency(price: Double): String {
        val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return numberFormat.format(price)
    }
    override fun onBindViewHolder(holder: WalletViewHolder, position: Int) {
        val currentProduct = filteredProductList[position]
        val tgl = formatDate(currentProduct.tanggal.toString())
        holder.textViewIdPesanan.text = tgl
        val rp = currentProduct.nominal?.toDouble() ?: 0.0
        val formattedPrice = formatCurrency(rp)
        holder.textViewNominal.text = formattedPrice
        holder.textViewNama.text = currentProduct.nama
        holder.textViewStatus.text = currentProduct.status
        holder.textViewNoRek.text = currentProduct.bank+" - "+currentProduct.norek
        //jika currentproduk.jenis == "pemasukan" maka tampilkan warna hijau
        //jika currentproduk.jenis == "pengeluaran" maka tampilkan warna merah dan tambahkan tanda minus
        if (currentProduct.jenis == "pemasukan") {
            holder.textViewNominal.setTextColor(ContextCompat.getColor(context, R.color.green))
        } else {
            holder.textViewNominal.setTextColor(ContextCompat.getColor(context, R.color.red))
            holder.textViewNominal.text = "-"+holder.textViewNominal.text
        }
        // Set background color of statusIndicator based on product status
        if (currentProduct.status == "published") {
            holder.textViewStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.publishedStatusColor))
        } else {
            holder.textViewStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.pendingStatusColor))
        }
    }

    inner class WalletViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewIdPesanan: TextView = itemView.findViewById(R.id.textViewIdPesanan)
        val textViewNominal: TextView = itemView.findViewById(R.id.textViewNominal)
        val textViewNama: TextView = itemView.findViewById(R.id.textViewNama)
        val textViewNoRek: TextView = itemView.findViewById(R.id.textViewNoRek)
        val textViewStatus: TextView = itemView.findViewById(R.id.textViewStatus)
        // Add logic for delete button view here
    }
}

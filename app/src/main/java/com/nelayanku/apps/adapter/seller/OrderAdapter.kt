package com.nelayanku.apps.adapter.seller
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nelayanku.apps.R
import com.nelayanku.apps.model.Order
import com.nelayanku.apps.model.Wallet
import java.text.NumberFormat
import java.util.Locale


class OrderAdapter(
    private var productList: MutableList<Order>,
    val context: Context,
    private val onAccClickListener: (Order) -> Unit,
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {
    public var filteredProductList: MutableList<Order> = mutableListOf()
    public lateinit var statusSelected : String
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
                val nam = product.namaBarang?.toLowerCase(Locale.getDefault())?.contains(lowerCaseQuery)
                Log.d("Kunci ", lowerCaseQuery)
                if (nam == true) {
                    filteredProductList.add(product)
                    Log.d("Ada ", product.namaBarang.toString())
                }
            }
        } else {
            filteredProductList.addAll(productList)
        }
        notifyDataSetChanged()
        Log.d("Data f",filteredProductList.size.toString())
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order, parent, false)
        return OrderViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return filteredProductList.size
    }
    private fun formatCurrency(price: Double): String {
        val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return numberFormat.format(price)
    }
    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val currentProduct = filteredProductList[position]

        holder.textViewTanggal.text = currentProduct.tanggal
        holder.textViewIdPesanan.text = currentProduct.idTransaksi
        holder.textViewStatus.text = currentProduct.status
        holder.textViewNama.text = "1. "+currentProduct.namaBarang
        val rpNoimnal = currentProduct.hargaBarang?.toDouble() ?: 0.0
        val rpTotal = currentProduct.total?.toDouble() ?: 0.0
        val rpBiaya = currentProduct.biaya?.toDouble() ?: 0.0
        val rpGrandTotal = rpTotal - rpBiaya
        holder.textViewQuantity.text = currentProduct.jumlahBarang+"@"+formatCurrency(rpNoimnal)
        holder.textViewTotal.text = formatCurrency(rpTotal)
        holder.textViewTLayanan.text = "-"+formatCurrency(rpBiaya)
        holder.textViewTotalHargaValue.text = formatCurrency(rpGrandTotal)
        val statusAdapter = ArrayAdapter.createFromResource(
            context,
            R.array.pesan_status_seller,
            android.R.layout.simple_spinner_item
        )
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        holder.spStatus.adapter = statusAdapter
        //set selected item
        val spinnerPosition = statusAdapter.getPosition(currentProduct.status)
        holder.spStatus.setSelection(spinnerPosition)
        holder.spStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                pos: Int,
                id: Long
            ) {
                statusSelected = parent.getItemAtPosition(pos).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        //jika status selain Dikirim dan Selesai, maka btnUbah tampil
        if (currentProduct.status == "Dikirim" || currentProduct.status == "Selesai") {
            holder.btnUbah.visibility = View.GONE
            holder.spStatus.visibility = View.GONE
        } else {
            holder.btnUbah.visibility = View.VISIBLE
            holder.spStatus.visibility = View.VISIBLE
        }
        holder.btnUbah.setOnClickListener { onAccClickListener(currentProduct) }
    }

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewTanggal: TextView = itemView.findViewById(R.id.textViewTanggal)
        val textViewIdPesanan: TextView = itemView.findViewById(R.id.textViewIdPesanan)
        val textViewStatus: TextView = itemView.findViewById(R.id.textViewStatus)
        val textViewNama: TextView = itemView.findViewById(R.id.textViewNama)
        val textViewQuantity: TextView = itemView.findViewById(R.id.textViewQuantity)
        val textViewTotal: TextView = itemView.findViewById(R.id.textViewTotal)
        val textViewLayanan: TextView = itemView.findViewById(R.id.textViewLayanan)
        val textViewTLayanan: TextView = itemView.findViewById(R.id.textViewTLayanan)
        val textViewTotalHargaValue: TextView = itemView.findViewById(R.id.textViewTotalHargaValue)
        val btnUbah: Button = itemView.findViewById(R.id.btnUbah)
        val spStatus: Spinner = itemView.findViewById(R.id.spStatus)
    }

}

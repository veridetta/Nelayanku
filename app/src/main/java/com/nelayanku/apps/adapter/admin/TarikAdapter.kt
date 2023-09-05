package com.nelayanku.apps.adapter.admin
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
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.nelayanku.apps.R
import com.nelayanku.apps.model.Product
import com.nelayanku.apps.model.Wallet
import java.text.NumberFormat
import java.util.Locale


class TarikAdapter(
    private var productList: MutableList<Wallet>,
    val context: Context,
    private val onAccClickListener: (Wallet) -> Unit,
) : RecyclerView.Adapter<TarikAdapter.WalletViewHolder>() {
    public var filteredProductList: MutableList<Wallet> = mutableListOf()
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
        filteredProductList.addAll(productList)
        notifyDataSetChanged()
        Log.d("Data f",filteredProductList.size.toString())
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalletViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tarik, parent, false)
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

        holder.textViewTanggal.text = currentProduct.tanggal
        val rp = currentProduct.nominal?.toDouble() ?: 0.0
        val formattedPrice = formatCurrency(rp)
        holder.textViewNominal.text = formattedPrice
        holder.textViewNama.text = currentProduct.nama
        holder.textViewNoRek.text = currentProduct.bank+" - "+currentProduct.norek
        val statusAdapter = ArrayAdapter.createFromResource(
            context,
            R.array.tarik_status,
            android.R.layout.simple_spinner_item
        )
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        holder.spStatus.adapter = statusAdapter
        //set status sesuai dengan value currentProduct.status
        val spinnerPosition = statusAdapter.getPosition(currentProduct.status)
        holder.spStatus.setSelection(spinnerPosition)
        statusSelected = currentProduct.status.toString()
        //jika status Selesai, btnUbah hide
        if (statusSelected == "Selesai" || statusSelected == "Ditolak") {
            holder.btnUbah.visibility = View.GONE
            holder.spStatus.isEnabled = false
        } else {
            holder.btnUbah.visibility = View.VISIBLE
            holder.spStatus.isEnabled = true
        }
        holder.btnUbah.setOnClickListener { onAccClickListener(currentProduct) }
        //spStatus listener dan set ke statusSelected
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
    }

    inner class WalletViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewTanggal: TextView = itemView.findViewById(R.id.textViewTanggal)
        val textViewNominal: TextView = itemView.findViewById(R.id.textViewNominal)
        val textViewNama: TextView = itemView.findViewById(R.id.textViewNama)
        val textViewNoRek: TextView = itemView.findViewById(R.id.textViewNoRek)
        val spStatus: Spinner = itemView.findViewById(R.id.spStatus)
        val btnUbah: Button = itemView.findViewById(R.id.btnUbah)
        // Add logic for delete button view here
    }
}

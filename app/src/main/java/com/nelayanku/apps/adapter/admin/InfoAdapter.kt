package com.nelayanku.apps.adapter.admin
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nelayanku.apps.R
import com.nelayanku.apps.model.Informasi
import com.nelayanku.apps.model.Product
import java.text.NumberFormat
import java.util.Locale


class InfoAdapter(
    private var productList: MutableList<Informasi>,
    val context: Context,
    private val onEditClickListener: (Informasi) -> Unit,
) : RecyclerView.Adapter<InfoAdapter.InfoViewHolder>() {
    public var filteredProductList: MutableList<Informasi> = mutableListOf()
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
                val nam = product.title?.toLowerCase(Locale.getDefault())?.contains(lowerCaseQuery)
                Log.d("Kunci ", lowerCaseQuery)
                if (nam == true) {
                    filteredProductList.add(product)
                    Log.d("Ada ", product.title.toString())
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
            .inflate(R.layout.item_info, parent, false)
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

        holder.textProductName.text = currentProduct.title
        holder.textProductPrice.text = currentProduct.tanggal
        holder.textProductDescription.text = currentProduct.description

        Glide.with(context)
            .load(currentProduct.coverImage)
            .override(270,270).centerCrop()
            .placeholder(R.drawable.no_image)
            .into(holder.imageProductCover)

        holder.lyiconEdit.setOnClickListener { onEditClickListener(currentProduct) }
    }

    inner class InfoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textProductName: TextView = itemView.findViewById(R.id.textProductName)
        val textProductPrice: TextView = itemView.findViewById(R.id.textProductPrice)
        val textProductDescription: TextView = itemView.findViewById(R.id.textProductDescription)
        val imageProductCover: ImageView = itemView.findViewById(R.id.imageProductCover)
        val lyiconEdit: LinearLayout = itemView.findViewById(R.id.btnEdit)
    }
}

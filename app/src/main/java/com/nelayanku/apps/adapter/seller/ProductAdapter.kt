package com.nelayanku.apps.adapter.seller
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
import com.nelayanku.apps.model.Product
import java.text.NumberFormat
import java.util.Locale


class ProductAdapter(
    private var productList: MutableList<Product>,
    val context: Context,
    private val onEditClickListener: (Product) -> Unit,
    private val onStatusEdit: (Product) -> Unit,
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {
    public var filteredProductList: MutableList<Product> = mutableListOf()
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
                val nam = product.name?.toLowerCase(Locale.getDefault())?.contains(lowerCaseQuery)
                Log.d("Kunci ", lowerCaseQuery)
                if (nam == true) {
                    filteredProductList.add(product)
                    Log.d("Ada ", product.name.toString())
                }
            }
        } else {
            filteredProductList.addAll(productList)
        }
        notifyDataSetChanged()
        Log.d("Data f",filteredProductList.size.toString())
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return filteredProductList.size
    }
    private fun formatCurrency(price: Double): String {
        val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return numberFormat.format(price)
    }
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val currentProduct = filteredProductList[position]

        holder.textProductName.text = currentProduct.name
        val rp = currentProduct.price?.toDouble() ?: 0.0
        val formattedPrice = formatCurrency(rp) + " /kg"
        holder.textProductPrice.text = formattedPrice
        holder.textProductDescription.text = currentProduct.description
        holder.statusIndicator.text = currentProduct.status

        Glide.with(context)
            .load(currentProduct.coverImage)
            .override(270,270).centerCrop()
            .placeholder(R.drawable.no_image)
            .into(holder.imageProductCover)

        // Set background color of statusIndicator based on product status
        if (currentProduct.status == "published") {
            holder.statusIndicator.setBackgroundColor(ContextCompat.getColor(context, R.color.publishedStatusColor))
            holder.btnTvStatus.text="Nonaktifkan"
        } else {
            holder.btnTvStatus.text="Aktifkan"
            holder.statusIndicator.setBackgroundColor(ContextCompat.getColor(context, R.color.pendingStatusColor))
        }
        holder.lyiconEdit.setOnClickListener { onEditClickListener(currentProduct) }
        holder.btnStatus.setOnClickListener { onStatusEdit(currentProduct) }
        var isExpanded = false
        holder.textProductDescription.setOnClickListener {
            isExpanded = !isExpanded
            if (isExpanded) {
                holder.textProductDescription.maxLines = Int.MAX_VALUE // Tampilkan semua baris
            } else {
                holder.textProductDescription.maxLines = 3 // Kembali ke maksimum 3 baris
            }
        }
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textProductName: TextView = itemView.findViewById(R.id.textProductName)
        val textProductPrice: TextView = itemView.findViewById(R.id.textProductPrice)
        val textProductDescription: TextView = itemView.findViewById(R.id.textProductDescription)
        val imageProductCover: ImageView = itemView.findViewById(R.id.imageProductCover)
        val statusIndicator: Button = itemView.findViewById(R.id.statusIndicator)
        val lyiconEdit: LinearLayout = itemView.findViewById(R.id.iconEdit)
        val btnTvStatus: TextView = itemView.findViewById(R.id.btnTvStatus)
        val btnStatus: LinearLayout = itemView.findViewById(R.id.btnStatus)
        // Add logic for delete button view here
    }
}

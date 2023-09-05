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
import com.bumptech.glide.Glide.init
import com.facebook.shimmer.ShimmerFrameLayout
import com.nelayanku.apps.R
import com.nelayanku.apps.model.Product
import java.text.NumberFormat
import java.util.Locale


class ProductAdapter(
    private var productList: MutableList<Product>,
    val context: Context,
    private val onAccClickListener: (Product) -> Unit,
    private val onTolakClickListener: (Product) -> Unit,
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
            .inflate(R.layout.item_product_admin, parent, false)
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

        Glide.with(context)
            .load(currentProduct.coverImage)
            .override(270,270).centerCrop()
            .placeholder(R.drawable.no_image)
            .into(holder.imageProductCover)

        holder.btnAcc.setOnClickListener { onAccClickListener(currentProduct) }
        holder.btnTolak.setOnClickListener { onTolakClickListener(currentProduct) }
        //if status = published lyButton.hide
        if (currentProduct.status == "published"){
            holder.lyButton.visibility = View.GONE
        }else{
            holder.lyButton.visibility = View.VISIBLE
        }
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textProductName: TextView = itemView.findViewById(R.id.textProductName)
        val textProductPrice: TextView = itemView.findViewById(R.id.textProductPrice)
        val textProductDescription: TextView = itemView.findViewById(R.id.textProductDescription)
        val imageProductCover: ImageView = itemView.findViewById(R.id.imageProductCover)
        val lyButton: LinearLayout = itemView.findViewById(R.id.lyButton)
        val btnAcc: LinearLayout = itemView.findViewById(R.id.btnSetujui)
        val btnTolak: LinearLayout = itemView.findViewById(R.id.btnTolak)
    }
}

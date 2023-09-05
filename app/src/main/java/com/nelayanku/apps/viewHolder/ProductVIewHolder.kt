package com.nelayanku.apps.viewHolder

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nelayanku.apps.R
import com.nelayanku.apps.model.Product


class ProductVIewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    private val textProductName: TextView = itemView.findViewById(R.id.textProductName)
    private val textProductPrice: TextView = itemView.findViewById(R.id.textProductPrice)
    private val textProductDescription: TextView = itemView.findViewById(R.id.textProductDescription)
    private val imageProductCover: ImageView = itemView.findViewById(R.id.imageProductCover)
    private val statusIndicator: View = itemView.findViewById(R.id.statusIndicator)
    val iconEdit: ImageView = itemView.findViewById(R.id.iconEdit)
    fun bindItem(product: Product) {
        view.apply {
            val context: Context = itemView.context
            //set view
            textProductName.text = product.name
            textProductPrice.text = product.price
            textProductDescription.text = product.description

            // Load cover image using Glide library
            Glide.with(itemView.context)
                .load(product.coverImage)
                .placeholder(R.drawable.no_image)
                .into(imageProductCover)

            // Set background color of statusIndicator based on product status
            if (product.status == "published") {
                statusIndicator.setBackgroundColor(ContextCompat.getColor(context, R.color.publishedStatusColor))
            } else {
                statusIndicator.setBackgroundColor(ContextCompat.getColor(context, R.color.pendingStatusColor))
            }
        }
    }
}
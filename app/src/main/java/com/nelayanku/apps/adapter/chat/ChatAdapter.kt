package com.nelayanku.apps.adapter.chat

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.google.firebase.firestore.FirebaseFirestore
import com.nelayanku.apps.R
import com.nelayanku.apps.model.ChatModel
import com.nelayanku.apps.model.Product
import com.nelayanku.apps.tools.formatCurrency


class ChatAdapter(
    private val chatList: List<ChatModel>,
    private val context: Context,
    private val currentUserUid: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_TEXT = 1
        private const val VIEW_TYPE_PRODUCT = 2
        private const val VIEW_TYPE_TEXT_RIGHT = 3
        private const val VIEW_TYPE_PRODUCT_RIGHT = 4
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_TEXT -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
                TextChatViewHolder(view)
            }
            VIEW_TYPE_PRODUCT -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product_chat, parent, false)
                ProductChatViewHolder(view)
            }
            VIEW_TYPE_TEXT_RIGHT -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_right, parent, false)
                TextChatViewHolder(view)
            }
            VIEW_TYPE_PRODUCT_RIGHT -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product_right, parent, false)
                ProductChatViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chatItem = chatList[position]
        when (getItemViewType(position)) {
            VIEW_TYPE_TEXT -> {
                val textHolder = holder as TextChatViewHolder
                textHolder.bind(chatItem)
                Log.d("ChatAdapter", "onBindViewHolder TEXT: ${chatItem.message}")
            }
            VIEW_TYPE_PRODUCT -> {
                val productHolder = holder as ProductChatViewHolder
                productHolder.bind(chatItem, context, onChatClickListener = { product ->
                    // Handle product chat item click
                })
                Log.d("ChatAdapter", "onBindViewHolder PRODUCT: ${chatItem.message}")
            }
            VIEW_TYPE_TEXT_RIGHT -> {
                val textHolder = holder as TextChatViewHolder
                textHolder.bind(chatItem)
                Log.d("ChatAdapter", "onBindViewHolder TEXT: ${chatItem.message}")
            }
            VIEW_TYPE_PRODUCT_RIGHT -> {
                val productHolder = holder as ProductChatViewHolder
                productHolder.bind(chatItem, context, onChatClickListener = { product ->
                    // Handle product chat item click
                })
                Log.d("ChatAdapter", "onBindViewHolder PRODUCT: ${chatItem.message}")
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val chatItem = chatList[position]

        // Check if the message is from the current user
        val isCurrentUserMessage = chatItem.senderId == currentUserUid
        return when {
            isCurrentUserMessage -> {
                when (chatItem.type) {
                    "text" -> VIEW_TYPE_TEXT_RIGHT // Right view type for current user's text message
                    "product" -> VIEW_TYPE_PRODUCT_RIGHT // Right view type for current user's product message
                    else -> VIEW_TYPE_TEXT_RIGHT // Default to right view type for text message
                }
            }
            else -> {
                when (chatItem.type) {
                    "text" -> VIEW_TYPE_TEXT // Left view type for other user's text message
                    "product" -> VIEW_TYPE_PRODUCT // Left view type for other user's product message
                    else -> VIEW_TYPE_TEXT // Default to left view type for text message
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    inner class TextChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textMessageTextView: TextView = itemView.findViewById(R.id.textViewMessage)
        private val imageMessageImageView: ImageView = itemView.findViewById(R.id.imageViewAttached)

        fun bind(chatItem: ChatModel) {
            // Bind text chat data to views
            textMessageTextView.text = chatItem.message
            // Cek jika ada URL gambar dalam pesan
            if (chatItem.type == "image" && chatItem.message != null) {
                //sembunyikan text message jika ada gambar
                textMessageTextView.visibility = View.GONE
                imageMessageImageView.visibility = View.VISIBLE
                // Tampilkan gambar pesan jika ada
                try {
                    Glide.with(itemView.context)
                        .load(chatItem.message)
                        .placeholder(R.drawable.no_image)
                        .into(imageMessageImageView)
                } catch (e: GlideException) {
                    e.logRootCauses("Glide Load Failed")
                    e.printStackTrace()
                }
            } else {
                // Sembunyikan ImageView jika tidak ada gambar
                imageMessageImageView.visibility = View.GONE
                //tampilkan text message jika tidak ada gambar
                textMessageTextView.visibility = View.VISIBLE
            }
        }
    }


    inner class ProductChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Implement binding and other logic for product chat item
        fun bind(chatItem: ChatModel, context: Context, onChatClickListener: (Product) -> Unit) {
            // Bind product chat data to views
            val productNameTextView: TextView = itemView.findViewById(R.id.textProductName)
            val productPriceTextView: TextView = itemView.findViewById(R.id.textProductPrice)
            val productDescriptionTextView: TextView = itemView.findViewById(R.id.textProductDescription)
            val productImageView: ImageView = itemView.findViewById(R.id.imageProductCover)

            //ambil data dari firebase
            val db = FirebaseFirestore.getInstance()
            val TAG = "LoadData"
            //ambil data dari collection Product
            db.collection("products").document(chatItem.message.toString()).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                        //buat objek product
                        productNameTextView.text = document.data?.get("name").toString()
                        //format harga pakai format rupiah
                        val price = document.data?.get("price").toString().toDouble()
                        val formattedPrice = formatCurrency(price) + " /kg"
                        productPriceTextView.text = formattedPrice
                        productDescriptionTextView.text = document.data?.get("description").toString()
                        Glide.with(context)
                            .load(document.data?.get("coverImage").toString())
                            .placeholder(R.drawable.no_image)
                            .into(productImageView)

                    } else {
                        Log.d(TAG, "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    //log
                    Log.d(TAG, "get failed with ", exception)
                }
        }
    }
}

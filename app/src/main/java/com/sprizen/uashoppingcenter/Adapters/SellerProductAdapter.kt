package com.sprizen.uashoppingcenter.Adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sprizen.uashoppingcenter.Model.SellerProduct
import com.sprizen.uashoppingcenter.R
import com.sprizen.uashoppingcenter.databinding.SellerProductAdapterBinding

class SellerProductAdapter(
    private var productList: List<SellerProduct>,
    private val onDeleteClick: (SellerProduct) -> Unit
) : RecyclerView.Adapter<SellerProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(val binding: SellerProductAdapterBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = SellerProductAdapterBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        val context = holder.itemView.context

        // Product Name & Price
        holder.binding.tvProductName.text = product.productName
        holder.binding.tvProductPrice.text = "₹${product.sellingPrice}"

        // Meta Text (Stock & Sold - yahan sold ki jagah filhal 0 ya sample rakha hai, agar model mein ho toh wo pass kar dein)
        holder.binding.tvProductMeta.text = "Stock: ${product.stock}   •   Sold: 0"

        // Status Chip Logic (Active / Out of Stock)
        if (product.stock > 0) {
            holder.binding.chipStatus.text = "Active"
            holder.binding.chipStatus.setTextColor(Color.parseColor("#00A669"))
            holder.binding.chipStatus.background = ContextCompat.getDrawable(context, R.drawable.bg_status_active)
        } else {
            holder.binding.chipStatus.text = "Out of Stock"
            holder.binding.chipStatus.setTextColor(Color.parseColor("#EF4444"))
            // Agar out of stock ka background alag ho toh wo bhi laga sakte hain
        }

        // Product Image Load via Glide
        if (product.imageUrls.isNotEmpty()) {
            Glide.with(context)
                .load(product.imageUrls[0])
                .placeholder(R.drawable.images_3)
                .error(R.drawable.images_3)
                .into(holder.binding.ivProductImage)
        } else {
            holder.binding.ivProductImage.setImageResource(R.drawable.images_3)
        }

        // Optional: Agar aap card par click ya long press par delete ya edit rakhna chahein
        holder.itemView.setOnLongClickListener {
            onDeleteClick(product)
            true
        }
    }

    override fun getItemCount(): Int = productList.size

    fun updateList(newList: List<SellerProduct>) {
        productList = newList
        notifyDataSetChanged()
    }
}
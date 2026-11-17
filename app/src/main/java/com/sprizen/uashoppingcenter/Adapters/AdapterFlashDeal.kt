package com.sprizen.uashoppingcenter.Adapters

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.button.MaterialButton
import com.sprizen.uashoppingcenter.Activities.ProductDetailsActivity
import com.sprizen.uashoppingcenter.DATA_CLASS.PRODUCT
import com.sprizen.uashoppingcenter.Fragments.HomeFragment
import com.sprizen.uashoppingcenter.R

class AdapterFlashDeal(
    val context: Context,
    val itemList: MutableList<PRODUCT>,
    var listener: HomeFragment
) : RecyclerView.Adapter<AdapterFlashDeal.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.flash_deal_adapter, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentPosition = holder.bindingAdapterPosition
        if (currentPosition == RecyclerView.NO_POSITION) return
        val product = itemList[currentPosition]

        // Cloudinary image optimization
        val imageUrl = product.imagesUrls.firstOrNull()
        val optimizedImageUrl = imageUrl?.replace(
            "/upload/",
            "/upload/w_600,h_600,c_fill,q_auto,f_auto/"
        )

        Glide.with(context)
            .load(optimizedImageUrl)
            .skipMemoryCache(true)
            .override(600, 600)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .centerCrop()
            .placeholder(R.drawable.placeholder)
            .into(holder.ivProductImage)

        // Product Name
        holder.tvProductTitle.text = product.productName

        // Selling / Deal Price
        holder.tvDealPrice.text = "Rs:${product.productPriceSelling}"

        // Original Price with Strike-Through
        holder.tvOriginalPrice.text = "Rs:${product.productPriceActual}"
        holder.tvOriginalPrice.paintFlags = holder.tvOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

        // Discount Percentage Calculation (Fixed)
        if (product.productPriceActual > 0 && product.productPriceActual > product.productPriceSelling) {
            val discountPs = ((product.productPriceActual - product.productPriceSelling).toDouble() / product.productPriceActual) * 100
            holder.tvDiscountChip.text = "-${discountPs.toInt()}%"
            holder.tvDiscountChip.visibility = View.VISIBLE
        } else {
            holder.tvDiscountChip.visibility = View.GONE
        }

        // Stock Progress & Status Text
        val stockCount = product.stockAvailable
        holder.pbStockProgress.progress = stockCount
        holder.tvStockStatus.text = "Available: $stockCount left"

        // Open Product Details Activity
        holder.itemView.setOnClickListener {
            context.startActivity(
                Intent(context, ProductDetailsActivity::class.java)
                    .putExtra("productId", product.productId)
            )
        }

        // Buy Now Button Click Listener (Triggers HomeFragment Flying Animation)
        holder.btnBuyNow.setOnClickListener {
            listener.onButtonClick(holder.ivProductImage)
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        val tvFlashBadge: TextView = itemView.findViewById(R.id.tvFlashBadge)
        val tvDiscountChip: TextView = itemView.findViewById(R.id.tvDiscountChip)
        val tvProductTitle: TextView = itemView.findViewById(R.id.tvProductTitle)
        val tvDealPrice: TextView = itemView.findViewById(R.id.tvDealPrice)
        val tvOriginalPrice: TextView = itemView.findViewById(R.id.tvOriginalPrice)
        val pbStockProgress: ProgressBar = itemView.findViewById(R.id.pbStockProgress)
        val tvStockStatus: TextView = itemView.findViewById(R.id.tvStockStatus)
        val btnBuyNow: Button = itemView.findViewById(R.id.btnBuyNow)
    }
}
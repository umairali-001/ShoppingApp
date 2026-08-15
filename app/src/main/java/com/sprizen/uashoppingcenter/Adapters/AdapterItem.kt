package com.sprizen.uashoppingcenter.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sprizen.uashoppingcenter.Activities.ProductDetailsActivity
import com.sprizen.uashoppingcenter.DATA_CLASS.PRODUCT
import com.sprizen.uashoppingcenter.R

class AdapterItem(var  context: Context, var itemList: MutableList<PRODUCT>) : RecyclerView.Adapter<AdapterItem.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):ViewHolder {
        
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_adapter, parent, false))
    }

    override fun onBindViewHolder(binding: ViewHolder, position: Int) {

        val product = itemList[position]

        // First Cloudinary image
        val imageUrl = product.imagesUrls.firstOrNull()

        Glide.with(context)
            .load(imageUrl)

            .centerCrop()
            .into(binding.imageUrl)

        // Product name
        binding.itemName.text = product.productName

        // Selling price
        binding.itemPrice.text = "${product.productPriceSelling}"

        // Listing Price
        binding.listingPrice.text = "₹${product.productPriceActual}"

        // Rating
        binding.itemRatting.text = "★ ${product.rating}"

        // Description
        binding.itemDes.text = product.productDescription

        // Off Percentage
        var discountps = (product.productPriceSelling-product.productPriceActual)/product
            .productPriceActual*100
        binding.itemOffPercentage.text = "${discountps.toInt()}%"

        // Open Product Details
        binding.itemView.setOnClickListener {

            context.startActivity(Intent(context, ProductDetailsActivity::class.java)
                .putExtra("productId",product.productId)
            )

        }
    }


    override fun getItemCount(): Int {
        return itemList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val imageUrl: ImageView = itemView.findViewById(R.id.item_image_adapter)

        val itemName: TextView = itemView.findViewById(R.id.item_name_adapter)

        val itemPrice: TextView = itemView.findViewById(R.id.item_price_adapter)

        val itemRatting: TextView = itemView.findViewById(R.id.item_ratting_adapter)
        val listingPrice: TextView = itemView.findViewById(R.id.item_Listing_adapter)
        val itemDes: TextView = itemView.findViewById(R.id.item_description_adapter)
        val itemOffPercentage: TextView = itemView.findViewById(R.id.item_percentage_adapter)


    }
}
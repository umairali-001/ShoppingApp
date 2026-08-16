package com.sprizen.uashoppingcenter.Adapters

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sprizen.uashoppingcenter.Activities.ProductDetailsActivity
import com.sprizen.uashoppingcenter.DATA_CLASS.PRODUCT
import com.sprizen.uashoppingcenter.DataBase
import com.sprizen.uashoppingcenter.Fragments.HomeFragment
import com.sprizen.uashoppingcenter.R

class AdapterItem(context: Context, itemList: MutableList<PRODUCT>, var listener: HomeFragment) : RecyclerView.Adapter<AdapterItem.ViewHolder>() {

    var context : Context
    var itemList : MutableList<PRODUCT>
    lateinit var dataBase: DataBase


    init {
        this.context = context
        this.itemList = itemList
        dataBase = DataBase(context)

    }


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
        var discountps = (product.productPriceSelling - product.productPriceActual) / product
            .productPriceActual * 100
        binding.itemOffPercentage.text = "${discountps.toInt()}%"

        // Open Product Details
        binding.itemView.setOnClickListener {

            context.startActivity(
                Intent(context, ProductDetailsActivity::class.java)
                    .putExtra("productId", product.productId)
            )
        }

        // Favorite Button Logic
        var isColorChange = false
        binding.favBtn.setOnClickListener {
            if (isColorChange) {
                binding.favBg.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.bg_profile_body_curve
                    )
                )
                binding.favBtn.imageTintList = ColorStateList.valueOf(Color.GRAY)
                isColorChange = false
            } else {
                binding.favBg.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.dot_active
                    )
                )
                binding.favBtn.imageTintList = ColorStateList.valueOf(Color.WHITE)
                isColorChange = true
            }
        }

        //Cart Button Logic
        binding.addToCatBtn.setOnClickListener{




            var result = dataBase.insertCartProduct(product.productId)
            binding.addToCatBtn.setBackgroundDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.button_click_background
                )
            )
            Toast.makeText(context,"${product.productId}", Toast.LENGTH_SHORT).show()
            Toast.makeText(context,"$result",Toast.LENGTH_SHORT).show()

            binding.addToCatBtn.isEnabled = false

        }
    }


    override fun getItemCount(): Int {
        return itemList.size
    }


    interface OnItemClickListener{
        fun onItemClick(productId : String)
    }




    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val imageUrl: ImageView = itemView.findViewById(R.id.item_image_adapter)

        val itemName: TextView = itemView.findViewById(R.id.item_name_adapter)

        val itemPrice: TextView = itemView.findViewById(R.id.item_price_adapter)

        val itemRatting: TextView = itemView.findViewById(R.id.item_ratting_adapter)
        val listingPrice: TextView = itemView.findViewById(R.id.item_Listing_adapter)
        val itemDes: TextView = itemView.findViewById(R.id.item_description_adapter)
        val itemOffPercentage: TextView = itemView.findViewById(R.id.item_percentage_adapter)
        val favBtn: ImageView = itemView.findViewById(R.id.item_favBtn_adapter)
        val favBg: LinearLayout = itemView.findViewById(R.id.fav_background)
        val addToCatBtn: LinearLayout = itemView.findViewById(R.id.item_addToCart_adapter)


    }
}
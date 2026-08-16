package com.sprizen.uashoppingcenter.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sprizen.uashoppingcenter.DATA_CLASS.PRODUCT
import com.sprizen.uashoppingcenter.Fragments.CartFragment
import com.sprizen.uashoppingcenter.R

class ItemCartAdapter(context: Context, itemList: MutableList<PRODUCT>) : RecyclerView.Adapter<ItemCartAdapter.ViewHolder>() {


    var context : Context
    var itemList : MutableList<PRODUCT>

    init {
        this.context = context
        this.itemList = itemList

    }



    override fun onCreateViewHolder(p0: ViewGroup, position: Int, ): ItemCartAdapter.ViewHolder {

        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.cart_adapter,null))
    }

    override fun onBindViewHolder(binding: ItemCartAdapter.ViewHolder, position: Int) {

        binding.productName.text = itemList[position].productName
        binding.productPrice.text = itemList[position].productPriceSelling.toString()
        binding.productOldPrice.text = itemList[position].productPriceActual.toString()
        binding.productDesc.text = itemList[position].discountOfProduct.toString()

        Glide.with(context)
            .load(itemList[position].imagesUrls[1])

            .centerCrop()
            .into(binding.productImage)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        var productImage = itemView.findViewById<ImageView>(R.id.iv_cart_product_image)
        var productName = itemView.findViewById<TextView>(R.id.tv_product_title)
        var productPrice = itemView.findViewById<TextView>(R.id.tv_product_price)
        var productOldPrice = itemView.findViewById<TextView>(R.id.tv_old_price)
        var productDesc = itemView.findViewById<TextView>(R.id.tv_discount_tag)

    }
}
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

class CategoryProductAdapter(
    private val context: Context,
    private var productList: List<PRODUCT>
) : RecyclerView.Adapter<CategoryProductAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_category_product_compact, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = productList[position]
        holder.tvTitle.text = product.productName
        holder.tvPrice.text = "Rs: ${product.productPriceSelling.toInt()}"

        val imgUrl = product.imagesUrls.firstOrNull()
        if (!imgUrl.isNullOrEmpty()) {
            Glide.with(context)
                .load(imgUrl)
                .centerCrop()
                .placeholder(R.drawable.image_1)
                .into(holder.ivProduct)
        } else {
            holder.ivProduct.setImageResource(R.drawable.image_1)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ProductDetailsActivity::class.java)
            intent.putExtra("productId", product.productId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = productList.size

    fun updateProducts(newProducts: List<PRODUCT>) {
        productList = newProducts
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProduct: ImageView = itemView.findViewById(R.id.ivCompactProduct)
        val tvTitle: TextView = itemView.findViewById(R.id.tvCompactTitle)
        val tvPrice: TextView = itemView.findViewById(R.id.tvCompactPrice)
    }
}

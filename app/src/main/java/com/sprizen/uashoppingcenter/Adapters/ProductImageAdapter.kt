package com.sprizen.uashoppingcenter.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sprizen.uashoppingcenter.R

class ProductImageAdapter(
    private val context: Context,
    private val imagesList: MutableList<String>
) : RecyclerView.Adapter<ProductImageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view = LayoutInflater.from(context).inflate(
            R.layout.adapter_products_images,
            parent,
            false
        )

        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        Glide.with(context)
            .load(imagesList[position])

            .centerCrop()
            .into(holder.productImage)


    }

    override fun getItemCount(): Int {
        return imagesList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val productImage: ImageView =
            itemView.findViewById(R.id.productImage)
    }
}
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
import com.sprizen.uashoppingcenter.Activities.ExploreActivity
import com.sprizen.uashoppingcenter.DATA_CLASS.PHOTO
import com.sprizen.uashoppingcenter.R

class SubCategoryAdapter(
    private val context: Context,
    private var subCategories: List<PHOTO>
) : RecyclerView.Adapter<SubCategoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_subcategory_grid, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = subCategories[position]
        holder.tvName.text = item.photoName

        if (item.photoLink.isNotEmpty()) {
            Glide.with(context)
                .load(item.photoLink)
                .centerCrop()
                .placeholder(R.drawable.image_1)
                .into(holder.ivImage)
        } else {
            holder.ivImage.setImageResource(R.drawable.image_1)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ExploreActivity::class.java)
            intent.putExtra("category_query", item.photoName)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = subCategories.size

    fun updateData(newList: List<PHOTO>) {
        subCategories = newList
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivImage: ImageView = itemView.findViewById(R.id.ivSubCategoryImage)
        val tvName: TextView = itemView.findViewById(R.id.tvSubCategoryName)
    }
}

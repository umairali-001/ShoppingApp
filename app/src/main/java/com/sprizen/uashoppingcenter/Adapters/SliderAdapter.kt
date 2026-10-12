package com.sprizen.uashoppingcenter.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.play.core.integrity.v
import com.google.api.Context
import com.sprizen.uashoppingcenter.R

class SliderAdapter(private val images: List<Int>) : RecyclerView.Adapter<SliderAdapter
    .SliderViewHolder>() {


    class SliderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgShoe: ImageView = view.findViewById(R.id.imgShoe)
        var button : Button = view.findViewById(R.id.btnDiscover)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.slider_item, parent, false)
        return SliderViewHolder(view)
    }

    override fun onViewDetachedFromWindow(holder: SliderViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.imgShoe.setImageDrawable(null) // امیج ریسورس اور ڈراؤبل کو ختم کرتا ہے
    }

    // جب آئٹم دوبارہ سکرین پر نظر آئے گا (Attach ہو گا) تو امیج دوبارہ سیٹ ہو جائے گی
    override fun onViewAttachedToWindow(holder: SliderViewHolder) {
        super.onViewAttachedToWindow(holder)
        val position = holder.bindingAdapterPosition
        if (position != RecyclerView.NO_POSITION && images.isNotEmpty()) {
            val actualPosition = position % images.size
            holder.imgShoe.setImageResource(images[actualPosition])
        }
        holder.button.setOnClickListener {

        }

    }

    override fun onBindViewHolder(holder: SliderViewHolder, position: Int) {

    }

    // Infinite loop ke liye bada number return karte hain
    override fun getItemCount(): Int = Integer.MAX_VALUE
}
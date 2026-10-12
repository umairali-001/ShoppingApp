package com.sprizen.uashoppingcenter.Adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.sprizen.uashoppingcenter.R

data class CategoryNavItem(
    val name: String,
    val iconRes: Int,
    val dbCategoryKey: String
)

class CategoryNavAdapter(
    private val context: Context,
    private val categories: List<CategoryNavItem>,
    private val onCategorySelected: (CategoryNavItem) -> Unit
) : RecyclerView.Adapter<CategoryNavAdapter.ViewHolder>() {

    var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_category_nav, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = categories[position]
        val isSelected = position == selectedPosition

        holder.tvLabel.text = item.name
        holder.ivIcon.setImageResource(item.iconRes)

        if (isSelected) {
            holder.cardIcon.setCardBackgroundColor(Color.parseColor("#55AD8E"))
            holder.tvLabel.setTextColor(Color.parseColor("#00A669"))
            holder.tvLabel.setTypeface(null, android.graphics.Typeface.BOLD)
            holder.activeIndicator.visibility = View.VISIBLE
        } else {
            holder.cardIcon.setCardBackgroundColor(Color.parseColor("#E1E2E6"))
            holder.tvLabel.setTextColor(Color.parseColor("#64748B"))
            holder.tvLabel.setTypeface(null, android.graphics.Typeface.NORMAL)
            holder.activeIndicator.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            val prevPos = selectedPosition
            selectedPosition = holder.bindingAdapterPosition
            notifyItemChanged(prevPos)
            notifyItemChanged(selectedPosition)
            onCategorySelected(item)
        }
    }

    override fun getItemCount(): Int = categories.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val activeIndicator: View = itemView.findViewById(R.id.viewActiveIndicator)
        val cardIcon: CardView = itemView.findViewById(R.id.cardCategoryIcon)
        val ivIcon: ImageView = itemView.findViewById(R.id.ivCategoryIcon)
        val tvLabel: TextView = itemView.findViewById(R.id.tvCategoryLabel)
    }
}

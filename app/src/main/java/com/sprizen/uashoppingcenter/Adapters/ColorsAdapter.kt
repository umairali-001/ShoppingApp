package com.sprizen.uashoppingcenter.Adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.sprizen.uashoppingcenter.R

class ColorsAdapter(
    private val colors: List<String>,
    private val onColorSelected: (String) -> Unit
) : RecyclerView.Adapter<ColorsAdapter.ColorViewHolder>() {

    private var selectedPosition = 0

    class ColorViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val colorOuterCircle: CardView =
            view.findViewById(R.id.colorOuterCircle)

        val colorCircle: View =
            view.findViewById(R.id.colorCircle)

        val tvColorName: TextView =
            view.findViewById(R.id.tvColorName)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ColorViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.adapter_colors,
                parent,
                false
            )

        return ColorViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ColorViewHolder,
        position: Int
    ) {

        val colorName = colors[position]

        holder.tvColorName.text = colorName

        // Color apply
        holder.colorCircle.backgroundTintList =
            android.content.res.ColorStateList.valueOf(
                getColor(colorName)
            )

        // Selected color
        if (position == selectedPosition) {

            holder.colorOuterCircle.setCardBackgroundColor(
                holder.itemView.context.getColor(
                    R.color.primaryGreenColor
                )
            )

            holder.tvColorName.setTextColor(
                holder.itemView.context.getColor(
                    R.color.black
                )
            )

        } else {

            holder.colorOuterCircle.setCardBackgroundColor(
                Color.TRANSPARENT
            )

            holder.tvColorName.setTextColor(
                Color.GRAY
            )
        }

        holder.itemView.setOnClickListener {

            val oldPosition = selectedPosition

            selectedPosition = position

            notifyItemChanged(oldPosition)
            notifyItemChanged(selectedPosition)

            onColorSelected(colorName)
        }
    }

    override fun getItemCount(): Int {
        return colors.size
    }


    private fun getColor(colorName: String): Int {

        return when (colorName.lowercase().trim()) {

            "black" -> Color.BLACK

            "white" -> Color.WHITE

            "red" -> Color.RED

            "blue" -> Color.rgb(0, 102, 255)

            "green" -> Color.GREEN

            "yellow" -> Color.YELLOW

            "orange" -> Color.rgb(255, 165, 0)

            "pink" -> Color.rgb(255, 141, 161)

            "purple" -> Color.rgb(128, 0, 128)

            "grey",
            "gray" -> Color.rgb(176, 176, 176)

            "brown" -> Color.rgb(121, 85, 72)

            "silver" -> Color.rgb(192, 192, 192)

            "gold" -> Color.rgb(255, 215, 0)

            "navy blue" -> Color.rgb(0, 0, 128)

            "sky blue" -> Color.rgb(135, 206, 235)

            else -> Color.LTGRAY
        }
    }
}
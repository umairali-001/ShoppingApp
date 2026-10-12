package com.sprizen.uashoppingcenter.Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sprizen.uashoppingcenter.DATA_CLASS.OrderItem
import com.sprizen.uashoppingcenter.DATA_CLASS.PRODUCT
import com.sprizen.uashoppingcenter.DataBase
import com.sprizen.uashoppingcenter.Fragments.CartFragment
import com.sprizen.uashoppingcenter.R
import com.sprizen.uashoppingcenter.databinding.FragmentCartBinding

class ItemCartAdapter(
    private val context: Context,
    private val itemList: MutableList<PRODUCT>,
    private val bindingCartF: FragmentCartBinding,
    private var listener: OnProductCheckedListener
) : RecyclerView.Adapter<ItemCartAdapter.ViewHolder>() {

    private val dataBase: DataBase = DataBase(context)

    // Sirf wahi product IDs store hongi jo user check/tick karega
    private val selectedProducts = mutableSetOf<String>()
    private val productQuantities = mutableMapOf<String, Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.cart_adapter, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentPosition = holder.bindingAdapterPosition
        if (currentPosition == RecyclerView.NO_POSITION) return

        val product = itemList[currentPosition]
        val productId = product.productId

        // Default Quantity = 1
        if (!productQuantities.containsKey(productId)) {
            productQuantities[productId] = 1
        }
        val currentQty = productQuantities[productId] ?: 1

        holder.productName.text = product.productName
        holder.productPrice.text = "Rs:${product.productPriceSelling}"
        holder.productOldPrice.text = "Rs:${product.productPriceActual}"
        holder.productDesc.text = "${product.discountOfProduct}% OFF"
        holder.quantity.text = "$currentQty"

        if (product.imagesUrls.isNotEmpty()) {
            Glide.with(context)
                .load(product.imagesUrls[0])
                .centerCrop()
                .into(holder.productImage)
        }

        // Checkbox recycling bug fix karne ke liye listener pehle clear karein
        holder.chackBox.setOnCheckedChangeListener(null)
        holder.chackBox.isChecked = selectedProducts.contains(productId)

        // Checkbox toggle handler
        holder.chackBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedProducts.add(productId)
            } else {
                selectedProducts.remove(productId)
            }
            recalculateTotal()
        }

        holder.btn_minus.setOnClickListener {
            val qty = productQuantities[productId] ?: 1
            if (qty > 1) {
                val newQty = qty - 1
                productQuantities[productId] = newQty
                holder.quantity.text = "$newQty"
                recalculateTotal()
            }
        }

        holder.btn_pluse.setOnClickListener {
            val qty = productQuantities[productId] ?: 1
            val maxStock = if (product.stockAvailable > 0) product.stockAvailable else 100
            if (qty < maxStock) {
                val newQty = qty + 1
                productQuantities[productId] = newQty
                holder.quantity.text = "$newQty"
                recalculateTotal()
            } else {
                Toast.makeText(context, "Stock limit reached", Toast.LENGTH_SHORT).show()
            }
        }

        holder.deleteBtn.setOnClickListener {
            deleteDialog(currentPosition)
        }
    }

    private fun recalculateTotal() {
        var total = 0
        var selectedCount = 0

        for (product in itemList) {
            if (selectedProducts.contains(product.productId)) {
                val qty = productQuantities[product.productId] ?: 1
                val price = product.productPriceSelling.toDouble()?.toInt() ?: 0
                total += (price * qty)
                selectedCount++
            }
        }

        listener.onProductChecked(selectedCount, total)
    }

    // Ye function sirf un items ko return karega jo user ne tick (select) kiye hain
    fun getSelectedOrderItems(): ArrayList<OrderItem> {
        val selectedList = ArrayList<OrderItem>()

        for (product in itemList) {
            if (selectedProducts.contains(product.productId)) {
                val qty = productQuantities[product.productId] ?: 1
                val price = product.productPriceSelling.toDouble() ?: 0.0

                selectedList.add(
                    OrderItem(
                        productId = product.productId,
                        productName = product.productName,
                        price = price,
                        quantity = qty,
                        imageUrl = product.imagesUrls.firstOrNull() ?: ""
                    )
                )
            }
        }
        return selectedList
    }

    override fun getItemCount(): Int = itemList.size

    interface OnProductCheckedListener {
        fun onProductChecked(itemCount: Int, price: Int)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.iv_cart_product_image)
        val productName: TextView = itemView.findViewById(R.id.tv_product_title)
        val productPrice: TextView = itemView.findViewById(R.id.tv_product_price)
        val productOldPrice: TextView = itemView.findViewById(R.id.tv_old_price)
        val productDesc: TextView = itemView.findViewById(R.id.tv_discount_tag)
        val deleteBtn: ImageView = itemView.findViewById(R.id.btn_delete)
        val chackBox: CheckBox = itemView.findViewById(R.id.cb_select_product)
        val btn_minus: ImageView = itemView.findViewById(R.id.btn_minus)
        val btn_pluse: ImageView = itemView.findViewById(R.id.btn_plus)
        val quantity: TextView = itemView.findViewById(R.id.tv_qty_count)
    }

    @SuppressLint("MissingInflatedId")
    private fun deleteDialog(position: Int) {
        val builder = AlertDialog.Builder(context)
        val layout = LayoutInflater.from(context).inflate(R.layout.dialog_delete_product, null)
        builder.setView(layout)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        val cancelBtn = layout.findViewById<Button>(R.id.dialog_Cancel_btn)
        val deleteBtn = layout.findViewById<Button>(R.id.dialog_Delete_btn)

        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        deleteBtn.setOnClickListener {
            try {
                val product = itemList[position]
                selectedProducts.remove(product.productId)
                productQuantities.remove(product.productId)

                dataBase.deleteProduct(product.productId)
                itemList.removeAt(position)

                notifyItemRemoved(position)
                notifyItemRangeChanged(position, itemList.size)
                recalculateTotal()

                dialog.dismiss()
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
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
import com.sprizen.uashoppingcenter.DATA_CLASS.PRODUCT
import com.sprizen.uashoppingcenter.DataBase
import com.sprizen.uashoppingcenter.Fragments.CartFragment
import com.sprizen.uashoppingcenter.R
import com.sprizen.uashoppingcenter.databinding.FragmentCartBinding

class ItemCartAdapter(
    context: Context, itemList: MutableList<PRODUCT>,
    bindingCartF:
    FragmentCartBinding,
    var listener: CartFragment,
) :
    RecyclerView.Adapter<ItemCartAdapter.ViewHolder>() {


    var context: Context
    var itemList: MutableList<PRODUCT>

    var isItemChecked = false
    var bindingCartF: FragmentCartBinding

    var totalPrice = 0
    var dataBase: DataBase

    var itemQuantity = 1

    var selectedList = mutableListOf<PRODUCT>()

    var itemCoutList = MutableList(itemList.size) { 1 }

    init {
        this.context = context
        this.itemList = itemList
        dataBase = DataBase(context)
        this.bindingCartF = bindingCartF

    }


    override fun onCreateViewHolder(p0: ViewGroup, position: Int): ItemCartAdapter.ViewHolder {

        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.cart_adapter, null))
    }


    override fun onBindViewHolder(binding: ItemCartAdapter.ViewHolder, position: Int) {

        try {

            itemCoutList.add(1)


            var position = binding.bindingAdapterPosition


            binding.productName.text = itemList[position].productName
            binding.productPrice.text = "Rs:${itemList[position].productPriceSelling}"
            binding.productOldPrice.text = "Rs:${itemList[position].productPriceActual}"
            binding.productDesc.text = "${itemList[position].discountOfProduct} OFF"




            Glide.with(context)
                .load(itemList[position].imagesUrls[0])

                .centerCrop()
                .into(binding.productImage)


            var product = itemList[position]
            var price = itemList[position].productPriceSelling.toInt()

            binding.chackBox.setOnCheckedChangeListener(null)
            binding.chackBox.setOnCheckedChangeListener { _, isChacked ->


                if (isChacked) {
                    selectedList.add(product)
                    totalPrice += price * itemCoutList[position]
                    isItemChecked=true

                }else{

                    totalPrice -= price * itemCoutList[position]
                    selectedList.remove(product)

                }

                    binding.quantity.text = "${itemCoutList[position]}"
                    listener.onProductChecked(selectedList.size, totalPrice)

            }


            binding.btn_minus.setOnClickListener {

                if (itemCoutList[position] > 1) {

                    if (binding.chackBox.isChecked) {
                        totalPrice -= price
                    }

                    itemCoutList[position] = itemCoutList[position] - 1
                }

                binding.quantity.text = "${itemCoutList[position]}"
                listener.onProductChecked(selectedList.size, totalPrice)
            }

            binding.btn_pluse.setOnClickListener {

                if (itemCoutList[position] < itemList[position].stockAvailable) {

                    if (binding.chackBox.isChecked) {
                        totalPrice += price
                    }

                    itemCoutList[position] = itemCoutList[position] + 1
                }

                binding.quantity.text = "${itemCoutList[position]}"
                listener.onProductChecked(selectedList.size, totalPrice)
            }

            binding.deleteBtn.setOnClickListener {
                if (binding.chackBox.isChecked){
                    binding.chackBox.isChecked = false
                }
                deleteDilog(position)
            }
        } catch (e: Exception) {

            e.stackTrace

        }

    }

    interface OnProductCheckedListener {
        fun onProductChecked(itemCount: Int, price: Int)

    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var productImage = itemView.findViewById<ImageView>(R.id.iv_cart_product_image)
        var productName = itemView.findViewById<TextView>(R.id.tv_product_title)
        var productPrice = itemView.findViewById<TextView>(R.id.tv_product_price)
        var productOldPrice = itemView.findViewById<TextView>(R.id.tv_old_price)
        var productDesc = itemView.findViewById<TextView>(R.id.tv_discount_tag)


        var deleteBtn = itemView.findViewById<ImageView>(R.id.btn_delete)


        var chackBox = itemView.findViewById<CheckBox>(R.id.cb_select_product)

        var btn_minus = itemView.findViewById<ImageView>(R.id.btn_minus)

        var btn_pluse = itemView.findViewById<ImageView>(R.id.btn_plus)

        var quantity = itemView.findViewById<TextView>(R.id.tv_qty_count)


    }


    @SuppressLint("MissingInflatedId")
    fun deleteDilog(position: Int) {
        var builder = AlertDialog.Builder(context)
        var layout = LayoutInflater.from(context).inflate(R.layout.dialog_delete_product, null)
        builder.setView(layout)
        var dailog = builder.create()
        dailog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dailog.show()

        var cnacelBtn = layout.findViewById<Button>(R.id.dialog_Cancel_btn)
        var deleteBtn = layout.findViewById<Button>(R.id.dialog_Delete_btn)
        cnacelBtn.setOnClickListener {
            dailog.dismiss()
        }

        deleteBtn.setOnClickListener {
            try {
                dataBase.deleteProduct(itemList[position].productId)
                itemList.removeAt(position)
                itemCoutList.removeAt(position)


                notifyItemRemoved(position)
                notifyItemRangeChanged(position, itemList.size)
                dailog.dismiss()

            }
            catch (e: Exception){
                Toast.makeText(context, "${e}", Toast.LENGTH_SHORT).show()
                e.stackTrace
            }

        }
    }
}
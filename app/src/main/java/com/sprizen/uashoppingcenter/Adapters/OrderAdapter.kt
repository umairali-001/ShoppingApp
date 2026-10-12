package com.sprizen.uashoppingcenter.Adapters

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.sprizen.uashoppingcenter.DATA_CLASS.Order
import com.sprizen.uashoppingcenter.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderAdapter(
    private val context: Context,
    private val ordersList: MutableList<Order>,
    private val isSellerMode: Boolean = false,
    private val onStatusChanged: (() -> Unit)? = null
) : RecyclerView.Adapter<OrderAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_order_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = ordersList[position]

        holder.tvOrderId.text = order.orderId
        holder.tvOrderStatus.text = order.orderStatus
        holder.tvOrderDate.text = "Placed: " + dateFormat.format(Date(order.timestamp))

        // Status badge color styling
        when (order.orderStatus.lowercase()) {
            "delivered" -> {
                holder.tvOrderStatus.setTextColor(Color.parseColor("#16A34A"))
                holder.tvOrderStatus.setBackgroundResource(R.drawable.bg_status_active)
            }
            "shipped" -> {
                holder.tvOrderStatus.setTextColor(Color.parseColor("#0284C7"))
                holder.tvOrderStatus.setBackgroundResource(R.drawable.bg_status_active)
            }
            "cancelled" -> {
                holder.tvOrderStatus.setTextColor(Color.parseColor("#DC2626"))
                holder.tvOrderStatus.setBackgroundResource(R.drawable.bg_status_active)
            }
            else -> {
                holder.tvOrderStatus.setTextColor(Color.parseColor("#EAB308"))
                holder.tvOrderStatus.setBackgroundResource(R.drawable.bg_status_active)
            }
        }

        holder.tvCustomerName.text = "Customer: ${order.customerName} (${order.customerPhone})"
        holder.tvAddress.text = "Deliver to: ${order.shippingAddress}, ${order.city}"

        val itemsSummary = if (order.items.isNotEmpty()) {
            val names = order.items.joinToString(", ") { "${it.quantity}x ${it.productName}" }
            "${order.items.size} Item(s): $names"
        } else {
            "Order items processed"
        }
        holder.tvItemsSummary.text = itemsSummary

        holder.tvPaymentMethod.text = "${order.paymentMethod} • ${order.paymentStatus}"
        holder.tvTotalAmount.text = "Total: Rs. ${order.totalAmount.toInt()}"

        if (isSellerMode) {
            holder.btnUpdateStatus.visibility = View.VISIBLE
            holder.btnUpdateStatus.setOnClickListener {
                showUpdateStatusDialog(order)
            }
        } else {
            holder.btnUpdateStatus.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = ordersList.size

    private fun showUpdateStatusDialog(order: Order) {
        val statuses = arrayOf("Processing", "Shipped", "Delivered", "Cancelled")
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Update Order Status (${order.orderId})")
        builder.setItems(statuses) { dialog, which ->
            val newStatus = statuses[which]
            FirebaseDatabase.getInstance().getReference("orders")
                .child(order.orderId)
                .child("orderStatus")
                .setValue(newStatus)
                .addOnSuccessListener {
                    order.orderStatus = newStatus
                    notifyDataSetChanged()
                    onStatusChanged?.invoke()
                    Toast.makeText(context, "Status updated to $newStatus", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to update: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOrderId: TextView = itemView.findViewById(R.id.tvCardOrderId)
        val tvOrderStatus: TextView = itemView.findViewById(R.id.tvCardOrderStatus)
        val tvOrderDate: TextView = itemView.findViewById(R.id.tvCardOrderDate)
        val tvCustomerName: TextView = itemView.findViewById(R.id.tvCardCustomerName)
        val tvAddress: TextView = itemView.findViewById(R.id.tvCardAddress)
        val tvItemsSummary: TextView = itemView.findViewById(R.id.tvCardItemsSummary)
        val tvPaymentMethod: TextView = itemView.findViewById(R.id.tvCardPaymentMethod)
        val tvTotalAmount: TextView = itemView.findViewById(R.id.tvCardTotalAmount)
        val btnUpdateStatus: Button = itemView.findViewById(R.id.btnUpdateOrderStatus)
    }
}

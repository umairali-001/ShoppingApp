package com.sprizen.uashoppingcenter.Activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sprizen.uashoppingcenter.Adapters.OrderAdapter
import com.sprizen.uashoppingcenter.DATA_CLASS.Order
import com.sprizen.uashoppingcenter.R
import com.sprizen.uashoppingcenter.databinding.ActivityMyOrdersBinding

class MyOrdersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyOrdersBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private val orderList = mutableListOf<Order>()
    private lateinit var orderAdapter: OrderAdapter

    private var filterStatus = "ALL"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyOrdersBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // 1. ProfileFragment se bheja gaya FILTER_STATUS receive karein
        filterStatus = intent.getStringExtra("FILTER_STATUS") ?: "ALL"

        // 2. Toolbar title update karein filter ke hisaab se
        binding.tvToolbarTitle.text = when (filterStatus) {
            "Placed" -> "Placed Orders"
            "Processing" -> "Processing Orders"
            "Cancelled" -> "Cancelled Orders"
            else -> "All Orders"
        }

        setupRecyclerView()
        setupListeners()
        loadFilteredOrders()
    }

    private fun setupListeners() {
        binding.btnBackOrders.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter(this, orderList)

        binding.rvOrders.layoutManager = LinearLayoutManager(this)
        binding.rvOrders.adapter = orderAdapter
    }

    private fun loadFilteredOrders() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val userId = currentUser.uid
        binding.progressBar?.visibility = View.VISIBLE

        database.getReference("orders")
            .orderByChild("userId")
            .equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    binding.progressBar?.visibility = View.GONE
                    orderList.clear()

                    for (orderSnap in snapshot.children) {
                        val order = orderSnap.getValue(Order::class.java)
                        if (order != null) {
                            val status = order.orderStatus ?: ""

                            // 3. Status Filtering Logic
                            when (filterStatus) {
                                "ALL" -> orderList.add(order)
                                "Placed" -> {
                                    if (status.equals("Placed", ignoreCase = true) || status.equals("PENDING_COD", ignoreCase = true)) {
                                        orderList.add(order)
                                    }
                                }
                                "Processing" -> {
                                    if (status.equals("Processing", ignoreCase = true) || status.equals("Shipped", ignoreCase = true)) {
                                        orderList.add(order)
                                    }
                                }
                                "Cancelled" -> {
                                    if (status.equals("Cancelled", ignoreCase = true)) {
                                        orderList.add(order)
                                    }
                                }
                            }
                        }
                    }

                    // Sabse naye orders sabse upar dikhane ke liye reverse karein
                    orderList.reverse()
                    orderAdapter.notifyDataSetChanged()

                    // Agar koi order na mile toh empty message show karein
                    if (orderList.isEmpty()) {
                        binding.tvEmptyOrders?.visibility = View.VISIBLE
                    } else {
                        binding.tvEmptyOrders?.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressBar?.visibility = View.GONE
                    Toast.makeText(this@MyOrdersActivity, "Failed to load orders: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
    private fun showOrderDetailsDialog(order: Order) {
        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_order_details, null)
        dialog.setContentView(view)

        // Bind Views
        val tvId = view.findViewById<TextView>(R.id.tvDialogOrderId)
        val tvDate = view.findViewById<TextView>(R.id.tvDialogOrderDate)
        val tvStatus = view.findViewById<TextView>(R.id.tvDialogOrderStatus)
        val tvName = view.findViewById<TextView>(R.id.tvDialogCustomerName)
        val tvPhone = view.findViewById<TextView>(R.id.tvDialogCustomerPhone)
        val tvAddress = view.findViewById<TextView>(R.id.tvDialogShippingAddress)
        val tvPayment = view.findViewById<TextView>(R.id.tvDialogPaymentMethod)
        val tvTotal = view.findViewById<TextView>(R.id.tvDialogTotalAmount)
        val btnClose = view.findViewById<Button>(R.id.btnCloseDialog)
        val itemsContainer = view.findViewById<LinearLayout>(R.id.layoutDialogOrderItemsContainer)

        // Set Data
        tvId.text = "Order ID: ${order.orderId}"

        val dateString = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(order.timestamp))
        tvDate.text = "Date: $dateString"

        tvStatus.text = order.orderStatus
        tvName.text = "Name: ${order.customerName}"
        tvPhone.text = "Phone: ${order.customerPhone}"
        tvAddress.text = "Address: ${order.shippingAddress}, ${order.city} (${order.postalCode})"
        tvPayment.text = "${order.paymentMethod} (${order.paymentStatus})"
        tvTotal.text = "Rs. ${order.totalAmount.toInt()}"

        // Populate Dynamic Items
        itemsContainer.removeAllViews()
        for (item in order.items) {
            val itemView = LayoutInflater.from(this).inflate(R.layout.cart_adapter, itemsContainer, false)

            itemView.findViewById<TextView>(R.id.tv_product_title).text = item.productName
            itemView.findViewById<TextView>(R.id.tv_product_price).text = "Rs: ${(item.price * item.quantity).toInt()}"
            itemView.findViewById<TextView>(R.id.tv_qty_count).text = "Qty: ${item.quantity}"

            // Hide unused buttons in dialog view
            itemView.findViewById<TextView>(R.id.tv_old_price).visibility = View.GONE
            itemView.findViewById<TextView>(R.id.tv_discount_tag).visibility = View.GONE
            itemView.findViewById<ImageView>(R.id.btn_minus).visibility = View.GONE
            itemView.findViewById<ImageView>(R.id.btn_plus).visibility = View.GONE
            itemView.findViewById<ImageView>(R.id.btn_delete).visibility = View.GONE
            itemView.findViewById<View>(R.id.cb_select_product).visibility = View.GONE
            itemView.findViewById<View>(R.id.line1)?.visibility = View.GONE
            itemView.findViewById<View>(R.id.line0)?.visibility = View.GONE

            val img = itemView.findViewById<ImageView>(R.id.iv_cart_product_image)
            if (item.imageUrl.isNotEmpty()) {
                com.bumptech.glide.Glide.with(this).load(item.imageUrl).centerCrop().into(img)
            }

            itemsContainer.addView(itemView)
        }

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
package com.sprizen.uashoppingcenter.Activities

import android.os.Bundle
import android.view.View
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
import com.sprizen.uashoppingcenter.databinding.ActivityMyOrdersBinding

class MyOrdersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyOrdersBinding
    private val ordersList = mutableListOf<Order>()
    private lateinit var orderAdapter: OrderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMyOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBackOrders.setOnClickListener {
            finish()
        }

        setupRecyclerView()
        loadOrders()
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter(this, ordersList, isSellerMode = false)
        binding.rvCustomerOrders.layoutManager = LinearLayoutManager(this)
        binding.rvCustomerOrders.adapter = orderAdapter
    }

    private fun loadOrders() {
        binding.pbLoadingOrders.visibility = View.VISIBLE
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        val ordersRef = FirebaseDatabase.getInstance().getReference("orders")
        ordersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                binding.pbLoadingOrders.visibility = View.GONE
                ordersList.clear()

                for (child in snapshot.children) {
                    val order = child.getValue(Order::class.java)
                    if (order != null) {
                        // Filter by user ID if authenticated, or show all if guest/test
                        if (userId == null || order.userId == userId || order.userId.startsWith("guest_")) {
                            ordersList.add(order)
                        }
                    }
                }

                // Sort newest first
                ordersList.sortByDescending { it.timestamp }
                orderAdapter.notifyDataSetChanged()

                if (ordersList.isEmpty()) {
                    binding.layoutEmptyOrders.visibility = View.VISIBLE
                    binding.rvCustomerOrders.visibility = View.GONE
                } else {
                    binding.layoutEmptyOrders.visibility = View.GONE
                    binding.rvCustomerOrders.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.pbLoadingOrders.visibility = View.GONE
            }
        })
    }
}

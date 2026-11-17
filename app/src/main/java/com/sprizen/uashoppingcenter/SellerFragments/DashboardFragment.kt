package com.sprizen.uashoppingcenter.SellerFragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sprizen.uashoppingcenter.Activities.AddProductActivity
import com.sprizen.uashoppingcenter.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private lateinit var binding: FragmentDashboardBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var userId = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        userId = auth.currentUser?.uid ?: ""

        // Load seller store name and product stats
        loadSellerData()

        // Add New Product Card Click
        binding.cardAddProduct.setOnClickListener {
            startActivity(Intent(requireContext(), AddProductActivity::class.java))
        }

        // Refresh stats click
        binding.tvRefreshStats.setOnClickListener {
            loadSellerData()
        }

        return binding.root
    }

    private fun loadSellerData() {
        if (userId.isEmpty()) return

        // 1. Load Seller Name from Realtime Database (`Users/{userId}`)
        database.getReference("Users").child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.child("name").getValue(String::class.java)
                    if (!name.isNullOrEmpty() && isAdded) {
                        binding.tvStoreName.text = "$name's Store"
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        // 2. Count Total Products listed by this specific seller from `products` node
        database.getReference("products")
            .orderByChild("userId")
            .equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (isAdded) {
                        val productCount = snapshot.childrenCount
                        binding.tvTotalProducts.text = productCount.toString()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}
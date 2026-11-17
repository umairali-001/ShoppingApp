package com.sprizen.uashoppingcenter.SellerFragments

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
import com.sprizen.uashoppingcenter.databinding.FragmentOrderBinding

class OrderFragment : Fragment() {

    private lateinit var binding: FragmentOrderBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var userId = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentOrderBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        userId = auth.currentUser?.uid ?: ""

        // Fetch Seller Orders
        loadSellerOrders()

        // Refresh click
        binding.ivRefreshOrders.setOnClickListener {
            loadSellerOrders()
        }

        return binding.root
    }

    private fun loadSellerOrders() {
        if (userId.isEmpty()) return

        // Yahan aap apne Firebase 'Orders' node se data fetch karenge
        // Misal ke taur par, agar orders mein seller ki id match karni ho ya saare orders load karne hon:
        database.getReference("Orders")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return

                    if (snapshot.exists()) {
                        binding.layoutEmptyOrders.visibility = View.GONE
                        binding.rvSellerOrders.visibility = View.VISIBLE
                        // Yahan aap apna Order Adapter set karenge jo aap orders list ke liye banayenge
                    } else {
                        binding.layoutEmptyOrders.visibility = View.VISIBLE
                        binding.rvSellerOrders.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    if (isAdded) {
                        binding.layoutEmptyOrders.visibility = View.VISIBLE
                    }
                }
            })
    }
}
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
import com.sprizen.uashoppingcenter.databinding.FragmentEarningBinding

class EarningFragment : Fragment() {

    private lateinit var binding: FragmentEarningBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var userId = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentEarningBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        userId = auth.currentUser?.uid ?: ""

        loadSellerEarnings()

        binding.ivRefreshEarnings.setOnClickListener {
            loadSellerEarnings()
        }

        return binding.root
    }

    private fun loadSellerEarnings() {
        if (userId.isEmpty()) return

        // Yahan aap Firebase se seller ki sales ya orders ka data fetch kar ke total revenue calculate kar sakte hain
        database.getReference("products")
            .orderByChild("userId")
            .equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return

                    var totalValue = 0.0
                    for (productSnap in snapshot.children) {
                        val price = productSnap.child("sellingPrice").getValue(Double::class.java) ?: 0.0
                        val stock = productSnap.child("stock").getValue(Int::class.java) ?: 0
                        // Misal ke taur par inventory value / estimated revenue calculate kar sakte hain
                        totalValue += (price * stock)
                    }

                    binding.tvTotalEarnings.text = "₹$totalValue"
                    binding.tvMonthlyEarnings.text = "₹$totalValue"
                    binding.tvPendingPayout.text = "₹0.00"
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}
package com.sprizen.uashoppingcenter.SellerFragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.sprizen.uashoppingcenter.Activities.SettingsActivity
import com.sprizen.uashoppingcenter.R
import com.sprizen.uashoppingcenter.databinding.FragmentMoreBinding

class MoreFragment : Fragment() {

    private lateinit var binding: FragmentMoreBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var firestore: FirebaseFirestore
    private var userId = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentMoreBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        firestore = FirebaseFirestore.getInstance()
        userId = auth.currentUser?.uid ?: ""

        loadSellerProfile()
        setupListeners()

        return binding.root
    }

    private fun loadSellerProfile() {
        if (userId.isEmpty()) return

        // 1. Load Name & Profile Image URL from Realtime Database
        database.getReference("Users").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isAdded) return

                    val name = snapshot.child("name").getValue(String::class.java) ?: "Seller Store"
                    val imageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)

                    binding.tvSellerName.text = name

                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(requireContext())
                            .load(imageUrl)
                            .placeholder(R.drawable.images_3)
                            .error(R.drawable.images_3)
                            .into(binding.ivSellerProfile)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        // 2. Load Email from Firestore
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (isAdded && document != null && document.exists()) {
                    val email = document.getString("email") ?: auth.currentUser?.email ?: ""
                    binding.tvSellerEmail.text = email
                } else if (isAdded) {
                    binding.tvSellerEmail.text = auth.currentUser?.email ?: ""
                }
            }
    }

    private fun setupListeners() {
        // Edit Settings Click -> Open SettingsActivity
        binding.btnEditStoreProfile.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        // Switch to Buyer Mode (Closes Seller Activity)
        binding.btnSwitchToBuyer.setOnClickListener {
            activity?.finish()
        }

        // Help Support Click
        binding.btnHelpSupport.setOnClickListener {
            Toast.makeText(requireContext(), "Seller Support Center", Toast.LENGTH_SHORT).show()
        }

        // Logout
        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to log out from seller account?")
            .setPositiveButton("Logout") { _, _ ->
                auth.signOut()
                activity?.finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
package com.sprizen.uashoppingcenter.Fragments

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.sprizen.uashoppingcenter.Activities.MyOrdersActivity
import com.sprizen.uashoppingcenter.Activities.SellerActivity
import com.sprizen.uashoppingcenter.Activities.SettingsActivity
import com.sprizen.uashoppingcenter.R
import com.sprizen.uashoppingcenter.databinding.FragmentProfileBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var database: FirebaseDatabase

    private val client = OkHttpClient()
    private var userId = ""

    companion object {
        private const val TAG = "ProfileFragmentDebug"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        database = FirebaseDatabase.getInstance()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            userId = currentUser.uid
            Log.d(TAG, "Logged-in User ID: $userId")
        } else {
            Log.e(TAG, "No user logged in!")
            Toast.makeText(requireContext(), "Please login again", Toast.LENGTH_SHORT).show()
        }

        // Load data from Firebase
        loadUserData()

        // Profile Image Click

        // Name Click -> Open Edit Dialog
        binding.tvProfileName.setOnClickListener {
            if (userId.isNotEmpty()) {

            }
        }

        // Seller Button Click
        binding.sellerBtn.setOnClickListener {
            startActivity(Intent(requireContext(), SellerActivity::class.java))
        }

        binding.btnSettingsTop.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        binding.allOrders.setOnClickListener {
            startActivity(Intent(requireContext(), MyOrdersActivity::class.java))

        }

        return binding.root
    }

    // ============================================================
    // LOAD USER DATA
    // ============================================================
    private fun loadUserData() {
        if (userId.isEmpty()) return

        // 1. Load Name & Image from Realtime Database (`Users/{userId}`)
        // Agar node mojood nahi hoga toh error nahi dega, balki khali snapshot dega
        val userRef = database.getReference("Users").child(userId)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return

                val name = snapshot.child("name").getValue(String::class.java)
                val imageUrl = snapshot.child("profileImageUrl").getValue(String::class.java)

                if (!name.isNullOrEmpty()) {
                    binding.tvProfileName.text = name
                } else {
                    binding.tvProfileName.text = "Tap to Set Name"
                }

                if (!imageUrl.isNullOrEmpty()) {
                    Glide.with(requireContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .into(binding.ivProfile)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (isAdded) {
                    Log.e(TAG, "Database read failed: ${error.message}")
                }
            }
        })

        // 2. Load Email from Firestore
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (isAdded && document != null && document.exists()) {
                    val email = document.getString("email") ?: auth.currentUser?.email ?: ""
                    binding.tvProfileEmail.text = email
                } else if (isAdded) {
                    binding.tvProfileEmail.text = auth.currentUser?.email ?: ""
                }
            }
            .addOnFailureListener {
                if (isAdded) {
                    binding.tvProfileEmail.text = auth.currentUser?.email ?: ""
                }
            }
    }
}
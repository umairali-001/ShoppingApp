package com.sprizen.uashoppingcenter.SellerFragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sprizen.uashoppingcenter.Activities.AddProductActivity
import com.sprizen.uashoppingcenter.Adapter.SellerProductAdapter
import com.sprizen.uashoppingcenter.Model.SellerProduct
import com.sprizen.uashoppingcenter.databinding.FragmentProductsBinding

class ProductsFragment : Fragment() {

    private lateinit var binding: FragmentProductsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private val allProductsList = ArrayList<SellerProduct>()
    private lateinit var adapter: SellerProductAdapter
    private var userId = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProductsBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        userId = auth.currentUser?.uid ?: ""

        setupRecyclerView()
        loadSellerProducts()
        setupListeners()
        setupSearch()

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = SellerProductAdapter(ArrayList()) { product ->
            showDeleteDialog(product)
        }
        binding.recyclerViewProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewProducts.adapter = adapter
    }

    private fun loadSellerProducts() {
        if (userId.isEmpty()) return

        database.getReference("products")
            .orderByChild("userId")
            .equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    allProductsList.clear()
                    var inStockCount = 0
                    var outOfStockCount = 0

                    for (productSnap in snapshot.children) {
                        val product = productSnap.getValue(SellerProduct::class.java)
                        if (product != null) {
                            allProductsList.add(product)
                            if (product.stock > 0) inStockCount++ else outOfStockCount++
                        }
                    }

                    if (isAdded) {
                        binding.tvTabAll.text = "All (${allProductsList.size})"
                        binding.tvTabActive.text = "In Stock ($inStockCount)"
                        binding.tvTabOutOfStock.text = "Out of Stock ($outOfStockCount)"

                        filterAndDisplayList("")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }

    private fun setupSearch() {
        binding.etSearchProducts.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterAndDisplayList(s.toString().trim())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterAndDisplayList(query: String) {
        val filtered = allProductsList.filter {
            it.productName.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true)
        }

        adapter.updateList(filtered)

        if (filtered.isEmpty()) {
            binding.layoutEmptyProducts.visibility = View.VISIBLE
            binding.recyclerViewProducts.visibility = View.GONE
        } else {
            binding.layoutEmptyProducts.visibility = View.GONE
            binding.recyclerViewProducts.visibility = View.VISIBLE
        }
    }

    private fun setupListeners() {
        val openAddProduct = View.OnClickListener {
            startActivity(Intent(requireContext(), AddProductActivity::class.java))
        }

        binding.btnAddNewProduct.setOnClickListener(openAddProduct)
        binding.btnQuickAdd.setOnClickListener(openAddProduct)

        binding.btnBack.setOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }

        binding.tvTabAll.setOnClickListener { filterAndDisplayList("") }
        binding.tvTabActive.setOnClickListener {
            val inStockList = allProductsList.filter { it.stock > 0 }
            adapter.updateList(inStockList)
        }
        binding.tvTabOutOfStock.setOnClickListener {
            val outOfStockList = allProductsList.filter { it.stock <= 0 }
            adapter.updateList(outOfStockList)
        }
    }

    private fun showDeleteDialog(product: SellerProduct) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Product")
            .setMessage("Are you sure you want to delete '${product.productName}'?")
            .setPositiveButton("Delete") { _, _ ->
                deleteProductFromFirebase(product.productId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteProductFromFirebase(productId: String) {
        database.getReference("products").child(productId).removeValue()
            .addOnSuccessListener {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Product deleted", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                if (isAdded) {
                    Toast.makeText(requireContext(), "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
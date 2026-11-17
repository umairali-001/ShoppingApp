package com.sprizen.uashoppingcenter.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.FirebaseDatabase
import com.sprizen.uashoppingcenter.Activities.CheckoutActivity
import com.sprizen.uashoppingcenter.Adapters.ItemCartAdapter
import com.sprizen.uashoppingcenter.DATA_CLASS.PRODUCT
import com.sprizen.uashoppingcenter.DataBase
import com.sprizen.uashoppingcenter.databinding.FragmentCartBinding

class CartFragment : Fragment(), ItemCartAdapter.OnProductCheckedListener {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private lateinit var itemCartAdapter: ItemCartAdapter
    private lateinit var dataBase: DataBase

    private val itemList = mutableListOf<PRODUCT>()
    private val productIds = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeEveryThing()
    }

    override fun onResume() {
        super.onResume()
        // Cart refresh karenge jab bhi user wapas aaye
        loadCartData()
    }

    fun initializeEveryThing() {
        dataBase = DataBase(requireContext())
        setupRecyclerView()
        setupClickListeners()
    }

    private fun setupRecyclerView() {

        itemCartAdapter = ItemCartAdapter(
            requireContext(),
            itemList,
            binding,
            this@CartFragment
        )

        binding.rvCartRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = itemCartAdapter
        }

    }

    private fun setupClickListeners() {


        binding.btnBuyPruductCart.setOnClickListener {
            if (itemList.isEmpty()) {
                Toast.makeText(requireContext(), "Your cart is empty!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Sirf wahi items milenge jo user ne Checkbox par tick kiye hain,
            // unki user-selected exact quantity (pieces) ke sath!
            val selectedOrderItems = itemCartAdapter.getSelectedOrderItems()

            if (selectedOrderItems.isEmpty()) {
                Toast.makeText(requireContext(), "Please select at least one item to proceed!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Selected items aur unki quantities ko Checkout Activity mein pass kar rahe hain
            val intent = Intent(requireContext(), CheckoutActivity::class.java).apply {
                putExtra("order_items", selectedOrderItems)
            }
            startActivity(intent)
        }

    }

    private fun loadCartData() {
        val cartProductList = dataBase.getCatProduct()

        if (cartProductList.isEmpty()) {
            itemList.clear()
            productIds.clear()
            itemCartAdapter.notifyDataSetChanged()
            updateCartSummary(0, 0)
            return
        }

        productIds.clear()
        // Unique IDs filter kar rahe hain taake cart mein duplicates na hon


        val uniqueCartIds = cartProductList.map { it.productId }.distinct()
        productIds.addAll(uniqueCartIds)

        itemList.clear()
        itemCartAdapter.notifyDataSetChanged()

        loadProductsFromFirebase()
    }

    private fun loadProductsFromFirebase() {
        val productRef = FirebaseDatabase
            .getInstance()
            .getReference("products")

        productIds.forEach { productId ->
            productRef
                .child(productId)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (_binding != null && snapshot.exists()) {
                        val product = snapshot.getValue(PRODUCT::class.java)
                        if (product != null) {
                            if (product.productId.isEmpty()) {
                                product.productId = snapshot.key ?: ""
                            }

                            // Ensure duplicate product object list mein add na ho
                            val isAlreadyAdded = itemList.any { it.productId == product.productId }

                            if (!isAlreadyAdded) {
                                itemList.add(product)
                                itemCartAdapter.notifyItemInserted(itemList.size - 1)
                            }
                        }
                    }
                }
                .addOnFailureListener { error ->
                    if (context != null) {
                        Toast.makeText(
                            requireContext(),
                            "Failed: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }

    override fun onProductChecked(
        itemCount: Int,
        price: Int
    ) {
        updateCartSummary(itemCount, price)
    }

    private fun updateCartSummary(itemCount: Int, price: Int) {
        binding.totalPriceAndItemCoutText.text = "Total Price ($itemCount Items)"
        binding.totalPriceTv.text = "Rs: $price.0"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
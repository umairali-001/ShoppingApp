package com.sprizen.uashoppingcenter.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.FirebaseDatabase
import com.sprizen.uashoppingcenter.Adapters.ItemCartAdapter
import com.sprizen.uashoppingcenter.DATA_CLASS.CartSQ
import com.sprizen.uashoppingcenter.DATA_CLASS.PRODUCT
import com.sprizen.uashoppingcenter.DataBase
import com.sprizen.uashoppingcenter.databinding.FragmentCartBinding

class CartFragment : Fragment(), ItemCartAdapter.OnProductCheckedListener {

    private lateinit var binding: FragmentCartBinding
    private lateinit var itemCartAdapter: ItemCartAdapter
    private lateinit var dataBase: DataBase

    private val itemList = mutableListOf<PRODUCT>()
    private val productIds = mutableListOf<String>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onResume() {
        super.onResume()

        if (::binding.isInitialized) {
            loadCartData()
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeEveryThing()
    }


    fun initializeEveryThing() {

        dataBase = DataBase(requireContext())

        setupRecyclerView()

        loadCartData()
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


    private fun loadCartData() {

        val cartProductList = dataBase.getCatProduct()

        if (cartProductList.isEmpty()) {

            itemList.clear()
            productIds.clear()

            itemCartAdapter.notifyDataSetChanged()

            return
        }


        productIds.clear()
        itemList.clear()


        cartProductList.forEach { cartProduct ->

            productIds.add(cartProduct.productId)
        }


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

                    if (snapshot.exists()) {

                        val product = snapshot.getValue(PRODUCT::class.java)

                        if (product != null) {

                            itemList.add(product)

                            itemCartAdapter.notifyItemInserted(
                                itemList.size - 1
                            )
                        }
                    }
                }
                .addOnFailureListener { error ->

                    Toast.makeText(
                        requireContext(),
                        "Failed: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }


    override fun onProductChecked(
        itemCount: Int,
        price: Int
    ) {

        binding.totalPriceAndItemCoutText.text =
            "Total Price (${itemCount}  Items)"

        binding.totalPriceTv.text =
            "Rs: ${price}.0"
    }
}
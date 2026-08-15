package com.sprizen.uashoppingcenter.Activities

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.google.firebase.database.FirebaseDatabase
import com.sprizen.uashoppingcenter.Adapters.ProductImageAdapter
import com.sprizen.uashoppingcenter.DATA_CLASS.PRODUCT
import com.sprizen.uashoppingcenter.R
import com.sprizen.uashoppingcenter.databinding.ActivityDetailsBuyingBinding

class ProductDetailsActivity : AppCompatActivity() {

     lateinit var binding: ActivityDetailsBuyingBinding

     lateinit var imagesList : MutableList<String>
     var productId: String = ""
     var product: PRODUCT? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailsBuyingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeEveryThing()

    }


    fun initializeEveryThing(){

        // Adapter se productId receive
        productId = intent.getStringExtra("productId") ?: ""

        if (productId.isEmpty()) {
            Toast.makeText(
                this,
                "Product ID not found",
                Toast.LENGTH_SHORT
            ).show()

            finish()
            return
        }

        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        loadProduct()


//
//        val adapter = ProductImageAdapter(
//            this@ProductDetailsActivity,
//            imagesList
//        )
//
//
//        binding.productImageRecyclerView.layoutManager =
//            LinearLayoutManager(
//                this@ProductDetailsActivity,
//                LinearLayoutManager.HORIZONTAL,
//                false
//            )
//
//        binding.productImageRecyclerView.adapter = adapter

// Snap effect
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(
            binding.productImageRecyclerView
        )

    }

    fun loadProduct() {

        val productReference = FirebaseDatabase.getInstance().getReference("products").child(productId)

        productReference.get().addOnSuccessListener { snapshot ->

                if (snapshot.exists()) {
                    product = snapshot.getValue(PRODUCT::class.java)
                    if (product != null) {
                        displayProduct(product!!)
                    }

                } else {
                    Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show()

                    finish()
                }
            }
            .addOnFailureListener { error -> Toast.makeText(this, "Failed: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }

    fun displayProduct(product: PRODUCT) {

        // Product Name
        binding.tvTitle.text = product.productName

        // Selling Price
        binding.tvSellingPrice.text =
            "₹${product.productPriceSelling}"

        // Actual Price
        binding.tvActualPrice.text =
            "₹${product.productPriceActual}"

        // Discount
        binding.tvDiscount.text =
            "${product.discountOfProduct}% OFF"

        // Description
        binding.tvDescription.text =
            product.productDescription

        // Rating
        binding.tvRating.text =
            "★ ${product.rating}"

        // Images
        setupProductImages(product.imagesUrls)

        // Colors
        // setupColors(product.colorsAvailable)
    }

    private fun setupProductImages(images: List<String>) {

        val adapter = ProductImageAdapter(
            this@ProductDetailsActivity,
            images as MutableList<String>
        )

        binding.productImageRecyclerView.layoutManager = LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL,
                false
            )

        binding.productImageRecyclerView.adapter = adapter
    }

}
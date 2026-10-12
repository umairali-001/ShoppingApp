package com.sprizen.uashoppingcenter.Activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.google.firebase.database.FirebaseDatabase
import com.sprizen.uashoppingcenter.Adapters.ProductImageAdapter
import com.sprizen.uashoppingcenter.DATA_CLASS.OrderItem
import com.sprizen.uashoppingcenter.DATA_CLASS.PRODUCT
import com.sprizen.uashoppingcenter.DataBase
import com.sprizen.uashoppingcenter.R
import com.sprizen.uashoppingcenter.databinding.ActivityDetailsBuyingBinding

class ProductDetailsActivity : AppCompatActivity() {

    lateinit var binding: ActivityDetailsBuyingBinding

    lateinit var imagesList: MutableList<String>
    var productId: String = ""
    var product: PRODUCT? = null
    lateinit var dataBase: DataBase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailsBuyingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeEveryThing()
    }

    fun initializeEveryThing() {
        dataBase = DataBase(this@ProductDetailsActivity)
        themes()

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
        binding.backIcon.setOnClickListener {
            binding.backIcon.setBackgroundResource(R.drawable.button_click_background)
            binding.backIcon.postDelayed({ binding.backIcon.background = null }, 50)
            finish()
        }

        loadProduct()

        // Snap effect for ViewPager-like experience in RecyclerView
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
        }.addOnFailureListener { error ->
            Toast.makeText(this, "Failed: ${error.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun displayProduct(product: PRODUCT) {
        // Product Name
        binding.tvTitle.text = product.productName

        // Selling Price
        binding.tvSellingPrice.text = "₹${product.productPriceSelling}"

        // Actual Price
        binding.tvActualPrice.text = "₹${product.productPriceActual}"

        // Discount
        binding.tvDiscount.text = "${product.discountOfProduct}% OFF"

        // Description
        binding.tvDescription.text = product.productDescription

        // Rating
        binding.tvRating.text = "★ ${product.rating}"

        // Images
        setupProductImages(product.imagesUrls)
    }

    private fun setupProductImages(images: List<String>) {
        val adapter = ProductImageAdapter(
            this@ProductDetailsActivity,
            images as MutableList<String>
        )

        binding.productImageRecyclerView.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        binding.productImageRecyclerView.adapter = adapter
    }

    fun themes() {
        window.statusBarColor = Color.TRANSPARENT

        WindowInsetsControllerCompat(
            window,
            window.decorView
        ).isAppearanceLightStatusBars = true

        // Window insets handled properly without duplication
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomBar) { _, insets ->
            val navigationInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val params = binding.bottomBar.layoutParams as ViewGroup.MarginLayoutParams
            params.bottomMargin = navigationInsets.bottom
            binding.bottomBar.layoutParams = params
            insets
        }

        // Buy/Checkout Button Click
        binding.btnBuyPruduct.setOnClickListener {
            product?.let { currentProduct ->
                // Convert single product to OrderItem
                val singleOrderItem = ArrayList<OrderItem>().apply {
                    add(
                        OrderItem(
                            productId = currentProduct.productId,
                            productName = currentProduct.productName,
                            price = currentProduct.productPriceSelling.toDouble()
                                ?: 0.0, // Convert to Double
                            quantity = 1,
                            imageUrl = currentProduct.imagesUrls.firstOrNull() ?: ""
                        )
                    )
                }

                val intent = Intent(this@ProductDetailsActivity, CheckoutActivity::class.java).apply {
                    putExtra("order_items", singleOrderItem) // Passing explicit single product
                }
                startActivity(intent)
            } ?: run {
                Toast.makeText(this, "Product details not loaded yet", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
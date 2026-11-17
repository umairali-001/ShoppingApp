package com.sprizen.uashoppingcenter.Activities

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.sprizen.uashoppingcenter.DATA_CLASS.Order
import com.sprizen.uashoppingcenter.DATA_CLASS.OrderItem
import com.sprizen.uashoppingcenter.DATA_CLASS.PRODUCT
import com.sprizen.uashoppingcenter.DataBase
import com.sprizen.uashoppingcenter.R
import com.sprizen.uashoppingcenter.databinding.ActivityCheckoutBinding
import java.util.UUID

class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding
    private lateinit var dataBase: DataBase
    private val orderItems = mutableListOf<OrderItem>()

    private var subtotal = 0.0
    private var discount = 0.0
    private var shippingFee = 0.0
    private var totalPayable = 0.0
    private var selectedPaymentMethod = "Credit / Debit Card"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dataBase = DataBase(this)

        setupListeners()
        loadOrderData()
        setupCardFormatters()
    }

    private fun setupListeners() {
        binding.btnBackCheckout.setOnClickListener {
            finish()
        }

        binding.rgPaymentMethod.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbCard -> {
                    selectedPaymentMethod = "Credit / Debit Card"
                    binding.layoutCardDetails.visibility = View.VISIBLE
                    binding.layoutWalletDetails.visibility = View.GONE
                    binding.layoutCodDetails.visibility = View.GONE
                    binding.btnPlaceOrder.text = "Pay & Confirm Order"
                }
                R.id.rbWallet -> {
                    selectedPaymentMethod = "Mobile Wallet / UPI"
                    binding.layoutCardDetails.visibility = View.GONE
                    binding.layoutWalletDetails.visibility = View.VISIBLE
                    binding.layoutCodDetails.visibility = View.GONE
                    binding.btnPlaceOrder.text = "Pay with Wallet"
                }
                R.id.rbCod -> {
                    selectedPaymentMethod = "Cash on Delivery"
                    binding.layoutCardDetails.visibility = View.GONE
                    binding.layoutWalletDetails.visibility = View.GONE
                    binding.layoutCodDetails.visibility = View.VISIBLE
                    binding.btnPlaceOrder.text = "Place COD Order"
                }
            }
        }

        binding.btnPlaceOrder.setOnClickListener {
            processCheckout()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun loadOrderData() {
        // 1. Check if items were passed from Intent (e.g. "Buy Now" or "Cart Selected Items")
        val passedItems = intent.getSerializableExtra("order_items") as? ArrayList<OrderItem>

        if (passedItems != null && passedItems.isNotEmpty()) {
            orderItems.clear()
            orderItems.addAll(passedItems.distinctBy { it.productId })
            calculateAndDisplaySummary()
        } else {
            // 2. Fallback: Load all carted items from SQLite Database if nothing passed
            val cartProducts = dataBase.getCatProduct()
            if (cartProducts.isEmpty()) {
                Toast.makeText(this, "No items to checkout", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            val uniqueCartProductIds = cartProducts.map { it.productId }.distinct()

            val productsRef = FirebaseDatabase.getInstance().getReference("products")
            var pendingLoads = uniqueCartProductIds.size
            orderItems.clear()

            for (cartItemId in uniqueCartProductIds) {
                productsRef.child(cartItemId).get().addOnSuccessListener { snap ->
                    val prod = snap.getValue(PRODUCT::class.java)
                    if (prod != null) {
                        val sellingPrice = prod.productPriceSelling.toDouble() ?: 0.0

                        val isAlreadyAdded = orderItems.any { it.productId == prod.productId }
                        if (!isAlreadyAdded) {
                            orderItems.add(
                                OrderItem(
                                    productId = prod.productId,
                                    productName = prod.productName,
                                    price = sellingPrice,
                                    quantity = 1,
                                    imageUrl = prod.imagesUrls.firstOrNull() ?: ""
                                )
                            )
                        }
                    }
                    pendingLoads--
                    if (pendingLoads <= 0) {
                        calculateAndDisplaySummary()
                    }
                }.addOnFailureListener {
                    pendingLoads--
                    if (pendingLoads <= 0) {
                        calculateAndDisplaySummary()
                    }
                }
            }
        }

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null && !user.displayName.isNullOrEmpty()) {
            binding.etFullName.setText(user.displayName)
        }
    }

    private fun calculateAndDisplaySummary() {
        binding.layoutOrderItemsContainer.removeAllViews()

        subtotal = 0.0
        discount = 0.0

        for (item in orderItems) {
            // Fixed multiplication operator (*)
            subtotal += (item.price * item.quantity)

            val itemView = LayoutInflater.from(this).inflate(R.layout.cart_adapter, binding.layoutOrderItemsContainer, false)
            itemView.findViewById<TextView>(R.id.tv_product_title).text = item.productName
            itemView.findViewById<TextView>(R.id.tv_product_price).text = "Rs: ${(item.price * item.quantity).toInt()}"
            itemView.findViewById<TextView>(R.id.tv_old_price).visibility = View.GONE
            itemView.findViewById<TextView>(R.id.tv_discount_tag).visibility = View.GONE
            itemView.findViewById<TextView>(R.id.tv_qty_count).text = "Qty: ${item.quantity}"
            itemView.findViewById<ImageView>(R.id.btn_minus).visibility = View.GONE
            itemView.findViewById<ImageView>(R.id.btn_plus).visibility = View.GONE
            itemView.findViewById<ImageView>(R.id.btn_delete).visibility = View.GONE
            itemView.findViewById<View>(R.id.cb_select_product).visibility = View.GONE

            val line1 = itemView.findViewById<View>(R.id.line1)
            line1?.visibility = View.GONE
            val line0 = itemView.findViewById<View>(R.id.line0)
            line0?.visibility = View.GONE

            val img = itemView.findViewById<ImageView>(R.id.iv_cart_product_image)
            if (item.imageUrl.isNotEmpty()) {
                Glide.with(this).load(item.imageUrl).centerCrop().into(img)
            }

            binding.layoutOrderItemsContainer.addView(itemView)
        }

        if (subtotal > 2000) {
            // Fixed multiplication operator (*)
            discount = subtotal * 0.1
        }

        shippingFee = if (subtotal > 1500 || subtotal == 0.0) 0.0 else 150.0
        // Fixed operators (+ and -)
        totalPayable = (subtotal - discount + shippingFee).coerceAtLeast(0.0)

        binding.tvCheckoutSubtotal.text = "Rs: ${subtotal.toInt()}"
        binding.tvCheckoutDiscount.text = "- Rs: ${discount.toInt()}"
        binding.tvCheckoutShipping.text = if (shippingFee == 0.0) "FREE" else "Rs: ${shippingFee.toInt()}"
        binding.tvCheckoutTotal.text = "Rs: ${totalPayable.toInt()}"
        binding.tvBottomTotal.text = "Rs: ${totalPayable.toInt()}"
    }

    private fun setupCardFormatters() {
        binding.etCardNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val raw = s.toString().replace(" ", "")
                val formatted = raw.chunked(4).joinToString(" ")
                if (formatted != s.toString()) {
                    binding.etCardNumber.setText(formatted)
                    binding.etCardNumber.setSelection(formatted.length)
                }
                if (raw.isNotEmpty()) {
                    val masked = if (raw.length > 4) {
                        "•••• •••• •••• " + raw.takeLast(4)
                    } else {
                        raw
                    }
                    binding.tvCardPreviewNumber.text = masked
                } else {
                    binding.tvCardPreviewNumber.text = "•••• •••• •••• 1234"
                }
            }
        })

        binding.etCardHolder.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val name = s.toString().trim()
                binding.tvCardPreviewHolder.text = if (name.isNotEmpty()) name.uppercase() else "CARD HOLDER"
            }
        })

        binding.etCardExpiry.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val exp = s.toString().trim()
                binding.tvCardPreviewExpiry.text = if (exp.isNotEmpty()) exp else "12/28"
            }
        })
    }

    private fun processCheckout() {
        val fullName = binding.etFullName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val city = binding.etCity.text.toString().trim()
        val postal = binding.etPostalCode.text.toString().trim()

        if (fullName.isEmpty()) {
            binding.etFullName.error = "Full Name is required"
            binding.etFullName.requestFocus()
            return
        }
        if (phone.isEmpty()) {
            binding.etPhone.error = "Phone number is required"
            binding.etPhone.requestFocus()
            return
        }
        if (address.isEmpty()) {
            binding.etAddress.error = "Delivery address is required"
            binding.etAddress.requestFocus()
            return
        }
        if (city.isEmpty()) {
            binding.etCity.error = "City is required"
            binding.etCity.requestFocus()
            return
        }

        binding.loadingOverlay.visibility = View.VISIBLE

        val orderId = "ORD-" + UUID.randomUUID().toString().take(8).uppercase()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: "guest_${System.currentTimeMillis()}"
        val userEmail = currentUser?.email ?: "guest@uashopping.com"

        // اگر Cash on Delivery ہو تو براہ راست فائر بیس میں سیو کریں
        if (selectedPaymentMethod == "Cash on Delivery") {
            binding.tvLoadingStatus.text = "Placing COD Order..."
            saveOrderToFirebase(orderId, userId, userEmail, fullName, phone, address, city, postal, "PENDING_COD")
        } else {
            // اگر JazzCash, Easypaisa, یا Card ہو
            binding.tvLoadingStatus.text = "Opening Payment Gateway..."

            // یہاں آپ کی جاز کیش / ایزی پیسہ / پے فاسٹ کا آن لائن پیمنٹ لنک آئے گا
            val paymentGatewayUrl = "https://your-backend-api.com/pay?orderId=$orderId&amount=$totalPayable"

            binding.loadingOverlay.visibility = View.GONE
            openSecurePaymentBrowser(paymentGatewayUrl)
        }
    }

    private fun openSecurePaymentBrowser(gatewayUrl: String) {
        try {
            val builder = androidx.browser.customtabs.CustomTabsIntent.Builder()
            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(this, android.net.Uri.parse(gatewayUrl))
        } catch (e: Exception) {
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(gatewayUrl))
            startActivity(intent)
        }
    }

    private fun saveOrderToFirebase(
        orderId: String, userId: String, userEmail: String,
        fullName: String, phone: String, address: String,
        city: String, postal: String, paymentStatus: String
    ) {
        val order = Order(
            orderId = orderId,
            userId = userId,
            userEmail = userEmail,
            customerName = fullName,
            customerPhone = phone,
            shippingAddress = address,
            city = city,
            postalCode = postal,
            items = orderItems,
            totalAmount = totalPayable,
            paymentMethod = selectedPaymentMethod,
            paymentStatus = paymentStatus,
            orderStatus = "Processing",
            timestamp = System.currentTimeMillis()
        )

        val ordersRef = FirebaseDatabase.getInstance().getReference("orders")
        ordersRef.child(orderId).setValue(order).addOnCompleteListener { task ->
            binding.loadingOverlay.visibility = View.GONE
            if (task.isSuccessful) {
                showSuccessDialog(order)
            } else {
                Toast.makeText(this, "Failed to save order: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showSuccessDialog(order: Order) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        val view = LayoutInflater.from(this).inflate(R.layout.dialog_order_success, null)
        dialog.setContentView(view)

        val title = view.findViewById<TextView>(R.id.tvSuccessTitle)
        val msg = view.findViewById<TextView>(R.id.tvSuccessMessage)
        val btnTrack = view.findViewById<Button>(R.id.btnTrackOrder)
        val btnContinue = view.findViewById<Button>(R.id.btnContinueShopping)

        title.text = "Order Placed Successfully! 🎉"
        msg.text = "Order ID: ${order.orderId}\nTotal: Rs. ${order.totalAmount.toInt()} (${order.paymentMethod})\nStatus: ${order.orderStatus}\n\nDelivery scheduled to ${order.shippingAddress}, ${order.city}."

        btnContinue.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        btnTrack.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, MyOrdersActivity::class.java)
            startActivity(intent)
            finish()
        }

        dialog.show()
    }
}
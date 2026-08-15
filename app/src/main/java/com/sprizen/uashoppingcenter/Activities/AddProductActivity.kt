package com.sprizen.uashoppingcenter.Activities

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.sprizen.uashoppingcenter.R
import com.sprizen.uashoppingcenter.databinding.ActivityAddProductBinding
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


class AddProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddProductBinding

    private lateinit var database: DatabaseReference

    private val client = OkHttpClient()

    // ============================================================
    // CURRENT STEP
    // ============================================================

    private var currentStep = 1

    // ============================================================
    // SELECTED IMAGES
    // ============================================================

    private val selectedImageUris = ArrayList<Uri>()

    private val selectedImageFiles = ArrayList<File>()


    // ============================================================
    // IMAGE PICKER
    // ============================================================

    private val pickMultipleMedia =
        registerForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia(5)
        ) { uris ->

            if (uris.isEmpty()) {

                Toast.makeText(
                    this,
                    "No image selected",
                    Toast.LENGTH_SHORT
                ).show()

                return@registerForActivityResult
            }

            selectedImageUris.clear()

            selectedImageFiles.clear()

            selectedImageUris.addAll(
                uris.take(5)
            )

            showSelectedImages()

            updatePreview()

            Toast.makeText(
                this,
                "${selectedImageUris.size} image(s) selected",
                Toast.LENGTH_SHORT
            ).show()
        }


    // ============================================================
    // ON CREATE
    // ============================================================

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        binding =
            ActivityAddProductBinding.inflate(
                layoutInflater
            )

        setContentView(binding.root)

        initializeEverything()
    }


    // ============================================================
    // INITIALIZATION
    // ============================================================

    private fun initializeEverything() {

        database =
            FirebaseDatabase
                .getInstance()
                .getReference("products")

        setupCategorySpinner()

        setupImagePicker()

        setupNavigation()

        setupDescriptionCounters()

        updateStepUI()

        updatePreview()
    }


    // ============================================================
    // IMAGE PICKER
    // ============================================================

    private fun setupImagePicker() {

        binding.boxAddImage.setOnClickListener {

            pickMultipleMedia.launch(

                PickVisualMediaRequest(
                    ActivityResultContracts
                        .PickVisualMedia
                        .ImageOnly
                )
            )
        }
    }


    // ============================================================
    // SHOW SELECTED IMAGES
    // ============================================================

    private fun showSelectedImages() {

        val imageViews = listOf(

            binding.productImage1,
            binding.productImage2,
            binding.productImage3,
            binding.productImage4,
            binding.productImage5
        )

        // Hide all images first

        imageViews.forEach { imageView ->

            imageView.visibility =
                View.INVISIBLE

            imageView.setImageDrawable(null)
        }


        // Show selected images

        selectedImageUris.forEachIndexed { index, uri ->

            if (index < imageViews.size) {

                imageViews[index].visibility =
                    View.VISIBLE

                imageViews[index].setImageURI(uri)
            }
        }


        // Main preview image

        if (selectedImageUris.isNotEmpty()) {

            binding.previewProductImage
                .setImageURI(
                    selectedImageUris[0]
                )
        }
    }


    // ============================================================
    // CATEGORY SPINNER
    // ============================================================

    private fun setupCategorySpinner() {

        val spinnerCategory =
            binding.categorySpinner

        val spinnerSubCategory =
            binding.subCategorySpinner


        val categoryAdapter =
            ArrayAdapter.createFromResource(

                this,

                R.array.category,

                R.layout.spinner_selected_item
            )


        categoryAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )


        spinnerCategory.adapter =
            categoryAdapter


        spinnerCategory.onItemSelectedListener =

            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(

                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long

                ) {

                    val selectedCategory =
                        parent
                            ?.getItemAtPosition(position)
                            ?.toString()
                            ?: ""


                    val subCategoryArrayResId =

                        when (selectedCategory) {

                            "Fashion" ->
                                R.array.sub_category_fashion

                            "Electronic" ->
                                R.array.sub_category_electronic

                            "Home" ->
                                R.array.sub_category_home

                            "Beauty" ->
                                R.array.sub_category_beauty

                            "Sport" ->
                                R.array.sub_category_sport

                            "Book" ->
                                R.array.sub_category_book

                            "Toys" ->
                                R.array.sub_category_toys

                            "Automotive" ->
                                R.array.sub_category_automotive

                            "Pet Food" ->
                                R.array.sub_category_pet_food

                            else ->
                                R.array.sub_category_electronic
                        }


                    val subCategoryAdapter =

                        ArrayAdapter.createFromResource(

                            this@AddProductActivity,

                            subCategoryArrayResId,

                            R.layout.spinner_selected_item
                        )


                    subCategoryAdapter.setDropDownViewResource(
                        android.R.layout.simple_spinner_dropdown_item
                    )


                    spinnerSubCategory.adapter =
                        subCategoryAdapter


                    updatePreview()
                }


                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) {
                }
            }
    }


    // ============================================================
    // NAVIGATION
    // ============================================================

    private fun setupNavigation() {

        // ========================================================
        // TOP BACK BUTTON
        // ========================================================

        binding.backBtn.setOnClickListener {

            if (currentStep > 1) {

                currentStep--

                updateStepUI()

                updatePreview()

            } else {

                finish()
            }
        }


        // ========================================================
        // PREVIEW BACK BUTTON
        // ========================================================

        binding.btnPreviewBack.setOnClickListener {

            if (currentStep == 7) {

                currentStep = 6

                updateStepUI()

                updatePreview()
            }
        }


        // ========================================================
        // NEXT / UPLOAD BUTTON
        // ========================================================

        binding.btnNext.setOnClickListener {

            // ----------------------------------------------------
            // STEP 1 - 6
            // ----------------------------------------------------

            if (currentStep < 7) {

                if (!validateCurrentStep()) {

                    return@setOnClickListener
                }


                currentStep++


                updatePreview()

                updateStepUI()
            }

            // ----------------------------------------------------
            // STEP 7
            // ----------------------------------------------------

            else {

                uploadProduct()
            }
        }
    }


    // ============================================================
    // UPDATE STEP UI
    // ============================================================

    private fun updateStepUI() {

        hideAllSteps()


        when (currentStep) {

            1 -> {

                binding
                    .firstLayoutStep1ProductImages
                    .visibility = View.VISIBLE
            }


            2 -> {

                binding
                    .secondLayoutStep2PriceInfo
                    .visibility = View.VISIBLE
            }


            3 -> {

                binding
                    .thirdLayoutStep3StockInfo
                    .visibility = View.VISIBLE
            }


            4 -> {

                binding
                    .fourthLayoutStep4Description
                    .visibility = View.VISIBLE
            }


            5 -> {

                binding
                    .fifthLayoutStep5Variants
                    .visibility = View.VISIBLE
            }


            6 -> {

                binding
                    .sixthLayoutStep6ShippingInfo
                    .visibility = View.VISIBLE
            }


            7 -> {

                binding
                    .seventhLayoutStep7Preview
                    .visibility = View.VISIBLE
            }
        }


        // ========================================================
        // STEP COUNT
        // ========================================================

        binding.tvStepCount1.text =
            "$currentStep/7"


        // ========================================================
        // BUTTON
        // ========================================================

        if (currentStep == 7) {

            binding.btnNext.text =
                "Upload Product"

            binding.btnPreviewBack.visibility =
                View.VISIBLE

        } else {

            binding.btnNext.text =
                "Next"

            binding.btnPreviewBack.visibility =
                View.GONE
        }


        // ========================================================
        // SCROLL TOP
        // ========================================================

        binding.mainScrollView.post {

            binding.mainScrollView.scrollTo(
                0,
                0
            )
        }
    }


    // ============================================================
    // HIDE ALL STEPS
    // ============================================================

    private fun hideAllSteps() {

        binding.firstLayoutStep1ProductImages.visibility =
            View.GONE

        binding.secondLayoutStep2PriceInfo.visibility =
            View.GONE

        binding.thirdLayoutStep3StockInfo.visibility =
            View.GONE

        binding.fourthLayoutStep4Description.visibility =
            View.GONE

        binding.fifthLayoutStep5Variants.visibility =
            View.GONE

        binding.sixthLayoutStep6ShippingInfo.visibility =
            View.GONE

        binding.seventhLayoutStep7Preview.visibility =
            View.GONE
    }


    // ============================================================
    // VALIDATION
    // ============================================================

    private fun validateCurrentStep(): Boolean {

        when (currentStep) {

            // ====================================================
            // STEP 1
            // ====================================================

            1 -> {

                if (selectedImageUris.isEmpty()) {

                    Toast.makeText(
                        this,
                        "Please select at least one product image",
                        Toast.LENGTH_SHORT
                    ).show()

                    return false
                }


                val productName =
                    binding.etProductName
                        .text
                        .toString()
                        .trim()


                if (productName.isEmpty()) {

                    binding.etProductName.error =
                        "Product name required"

                    binding.etProductName.requestFocus()

                    return false
                }
            }


            // ====================================================
            // STEP 2
            // ====================================================

            2 -> {

                val sellingPrice =
                    binding.etSellingPrice
                        .text
                        .toString()
                        .trim()


                val mrp =
                    binding.etMrp
                        .text
                        .toString()
                        .trim()


                if (sellingPrice.isEmpty()) {

                    binding.etSellingPrice.error =
                        "Selling price required"

                    return false
                }


                if (sellingPrice.toDoubleOrNull() == null) {

                    binding.etSellingPrice.error =
                        "Enter valid price"

                    return false
                }


                if (mrp.isEmpty()) {

                    binding.etMrp.error =
                        "MRP required"

                    return false
                }


                if (mrp.toDoubleOrNull() == null) {

                    binding.etMrp.error =
                        "Enter valid MRP"

                    return false
                }
            }


            // ====================================================
            // STEP 3
            // ====================================================

            3 -> {

                val stock =
                    binding.etStockQuantity
                        .text
                        .toString()
                        .trim()


                if (stock.isEmpty()) {

                    binding.etStockQuantity.error =
                        "Stock quantity required"

                    return false
                }


                if (stock.toIntOrNull() == null) {

                    binding.etStockQuantity.error =
                        "Enter valid stock"

                    return false
                }
            }


            // ====================================================
            // STEP 4
            // ====================================================

            4 -> {

                val shortDescription =
                    binding.etShortDescription
                        .text
                        .toString()
                        .trim()


                val longDescription =
                    binding.etLongDescription
                        .text
                        .toString()
                        .trim()


                if (shortDescription.isEmpty()) {

                    binding.etShortDescription.error =
                        "Short description required"

                    return false
                }


                if (longDescription.isEmpty()) {

                    binding.etLongDescription.error =
                        "Long description required"

                    return false
                }
            }


            // ====================================================
            // STEP 5
            // ====================================================

            5 -> {

                // Optional
            }


            // ====================================================
            // STEP 6
            // ====================================================

            6 -> {

                val weight =
                    binding.etWeight
                        .text
                        .toString()
                        .trim()


                if (weight.isEmpty()) {

                    binding.etWeight.error =
                        "Weight required"

                    return false
                }


                if (weight.toDoubleOrNull() == null) {

                    binding.etWeight.error =
                        "Enter valid weight"

                    return false
                }
            }
        }


        return true
    }


    // ============================================================
    // DESCRIPTION COUNTERS
    // ============================================================

    private fun setupDescriptionCounters() {

        binding.etShortDescription
            .addTextChangedListener(

                object :
                    android.text.TextWatcher {

                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }


                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {

                        binding.tvShortDescCounter.text =
                            "${s?.length ?: 0}/150"
                    }


                    override fun afterTextChanged(
                        s: android.text.Editable?
                    ) {
                    }
                }
            )


        binding.etLongDescription
            .addTextChangedListener(

                object :
                    android.text.TextWatcher {

                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }


                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {

                        binding.tvLongDescCounter.text =
                            "${s?.length ?: 0}/500"
                    }


                    override fun afterTextChanged(
                        s: android.text.Editable?
                    ) {
                    }
                }
            )
    }


    // ============================================================
    // UPDATE PREVIEW
    // ============================================================

    private fun updatePreview() {

        val title =
            binding.etProductName
                .text
                .toString()
                .trim()


        val sellingPrice =
            binding.etSellingPrice
                .text
                .toString()
                .trim()


        val mrp =
            binding.etMrp
                .text
                .toString()
                .trim()


        val discount =
            binding.etDiscount
                .text
                .toString()
                .trim()


        val stock =
            binding.etStockQuantity
                .text
                .toString()
                .trim()


        val category =
            binding.categorySpinner
                .selectedItem
                ?.toString()
                ?: ""


        val subCategory =
            binding.subCategorySpinner
                .selectedItem
                ?.toString()
                ?: ""


        if (title.isNotEmpty()) {

            binding.tvPreviewTitle.text =
                title
        }


        if (sellingPrice.isNotEmpty()) {

            binding.tvPreviewSellingPrice.text =
                "₹$sellingPrice"
        }


        if (mrp.isNotEmpty()) {

            binding.tvPreviewMrp.text =
                "₹$mrp"
        }


        if (discount.isNotEmpty()) {

            binding.tvPreviewDiscount.text =
                "$discount% OFF"
        }


        if (stock.isNotEmpty()) {

            binding.tvPreviewStock.text =
                "Stock: $stock"
        }


        binding.tvPreviewCategoryPath.text =
            "Category: $category > $subCategory"


        // IMPORTANT:
        // Aapke original code mein selectedImageUris[0,]
        // error tha. Correct syntax [0] hai.

        if (selectedImageUris.isNotEmpty()) {

            binding.previewProductImage
                .setImageURI(
                    selectedImageUris[0]
                )
        }
    }


    // ============================================================
    // UPLOAD PRODUCT
    // ============================================================

    private fun uploadProduct() {

        val productName =
            binding.etProductName
                .text
                .toString()
                .trim()


        val actualPrice =
            binding.etSellingPrice
                .text
                .toString()
                .trim()
                .toDoubleOrNull()


        val sellingPrice =
            binding.etMrp
                .text
                .toString()
                .trim()
                .toDoubleOrNull()


        val discount =
            binding.etDiscount
                .text
                .toString()
                .trim()
                .toDoubleOrNull()
                ?: 0.0


        val stock =
            binding.etStockQuantity
                .text
                .toString()
                .trim()
                .toIntOrNull()
                ?: 0


        val shortDescription =
            binding.etShortDescription
                .text
                .toString()
                .trim()


        val longDescription =
            binding.etLongDescription
                .text
                .toString()
                .trim()


        val category =
            binding.categorySpinner
                .selectedItem
                ?.toString()
                ?: ""


        val subCategory =
            binding.subCategorySpinner
                .selectedItem
                ?.toString()
                ?: ""


        val weight =
            binding.etWeight
                .text
                .toString()
                .trim()
                .toDoubleOrNull()
                ?: 0.0


        val shippingCharges =
            binding.etShippingCharges
                .text
                .toString()
                .trim()
                .toDoubleOrNull()
                ?: 0.0


        // ========================================================
        // VALIDATION
        // ========================================================

        if (productName.isEmpty()) {

            Toast.makeText(
                this,
                "Product name is required",
                Toast.LENGTH_SHORT
            ).show()

            return
        }


        if (sellingPrice == null) {

            Toast.makeText(
                this,
                "Invalid selling price",
                Toast.LENGTH_SHORT
            ).show()

            return
        }


        if (actualPrice == null) {

            Toast.makeText(
                this,
                "Invalid actual price",
                Toast.LENGTH_SHORT
            ).show()

            return
        }


        if (selectedImageUris.isEmpty()) {

            Toast.makeText(
                this,
                "Please select at least one image",
                Toast.LENGTH_SHORT
            ).show()

            return
        }


        // ========================================================
        // URI -> FILE
        // ========================================================

        selectedImageFiles.clear()


        selectedImageUris.forEachIndexed { index, uri ->

            try {

                val file =
                    convertUriToFile(
                        uri,
                        "product_image_$index.jpg"
                    )

                selectedImageFiles.add(file)

            } catch (e: Exception) {

                Toast.makeText(
                    this,
                    "Image conversion failed",
                    Toast.LENGTH_LONG
                ).show()

                return
            }
        }


        // ========================================================
        // DISABLE UPLOAD BUTTON
        // ========================================================

        binding.btnNext.isEnabled =
            false

        binding.btnNext.text =
            "Uploading..."


        Toast.makeText(
            this,
            "Product uploading...",
            Toast.LENGTH_SHORT
        ).show()


        // ========================================================
        // UPLOAD IMAGES
        // ========================================================

        uploadImagesSequentially(

            index = 0,

            imageUrls = ArrayList(),

            productName = productName,

            sellingPrice = sellingPrice,

            actualPrice = actualPrice,

            discount = discount,

            stock = stock,

            description =

                if (longDescription.isNotEmpty())
                    longDescription
                else
                    shortDescription,

            weight = weight,

            shippingCharges = shippingCharges,

            category = category,

            subCategory = subCategory
        )
    }


    // ============================================================
    // UPLOAD IMAGES SEQUENTIALLY
    // ============================================================

    private fun uploadImagesSequentially(

        index: Int,

        imageUrls: ArrayList<String>,

        productName: String,

        sellingPrice: Double,

        actualPrice: Double,

        discount: Double,

        stock: Int,

        description: String,

        weight: Double,

        shippingCharges: Double,

        category: String,

        subCategory: String

    ) {

        // ========================================================
        // ALL IMAGES UPLOADED
        // ========================================================

        if (index >= selectedImageFiles.size) {

            saveProductToFirebase(

                imageUrls = imageUrls,

                productName = productName,

                sellingPrice = sellingPrice,

                actualPrice = actualPrice,

                discount = discount,

                stock = stock,

                description = description,

                weight = weight,

                shippingCharges = shippingCharges,

                category = category,

                subCategory = subCategory
            )

            return
        }


        // ========================================================
        // UPLOAD CURRENT IMAGE
        // ========================================================

        uploadSingleImage(
            selectedImageFiles[index]
        ) { url ->

            if (url != null) {

                imageUrls.add(url)


                // Next image

                uploadImagesSequentially(

                    index = index + 1,

                    imageUrls = imageUrls,

                    productName = productName,

                    sellingPrice = sellingPrice,

                    actualPrice = actualPrice,

                    discount = discount,

                    stock = stock,

                    description = description,

                    weight = weight,

                    shippingCharges = shippingCharges,

                    category = category,

                    subCategory = subCategory
                )

            } else {

                runOnUiThread {

                    binding.btnNext.isEnabled =
                        true

                    binding.btnNext.text =
                        "Upload Product"


                    Toast.makeText(
                        this,
                        "Image ${index + 1} upload failed",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }


    // ============================================================
    // CLOUDINARY UPLOAD
    // ============================================================

    private fun uploadSingleImage(

        imageFile: File,

        callback: (String?) -> Unit

    ) {

        val cloudName =
            "q3pn4aap"

        val uploadPreset =
            "shopping_app"


        val cloudinaryUrl =
            "https://api.cloudinary.com/v1_1/$cloudName/image/upload"


        val requestBody =

            MultipartBody.Builder()

                .setType(
                    MultipartBody.FORM
                )

                .addFormDataPart(
                    "upload_preset",
                    uploadPreset
                )

                .addFormDataPart(

                    "file",

                    imageFile.name,

                    RequestBody.create(

                        "image/*"
                            .toMediaTypeOrNull(),

                        imageFile
                    )
                )

                .build()


        val request =

            Request.Builder()

                .url(cloudinaryUrl)

                .post(requestBody)

                .build()


        client.newCall(request)
            .enqueue(

                object : Callback {

                    override fun onFailure(

                        call: Call,

                        e: IOException

                    ) {

                        callback(null)
                    }


                    override fun onResponse(

                        call: Call,

                        response: Response

                    ) {

                        response.use {

                            if (!response.isSuccessful) {

                                callback(null)

                                return
                            }


                            val responseBody =
                                response.body
                                    ?.string()
                                    ?: ""


                            val imageUrl =
                                extractCloudinaryUrl(
                                    responseBody
                                )


                            callback(imageUrl)
                        }
                    }
                }
            )
    }


    // ============================================================
    // EXTRACT CLOUDINARY URL
    // ============================================================

    private fun extractCloudinaryUrl(
        response: String
    ): String? {

        return try {

            val url =

                response
                    .substringAfter(
                        "\"secure_url\":\""
                    )
                    .substringBefore("\"")


            if (
                url.isNotEmpty() &&
                url != response
            ) {

                url

            } else {

                null
            }

        } catch (e: Exception) {

            null
        }
    }


    // ============================================================
    // SAVE PRODUCT TO FIREBASE
    // ============================================================

    private fun saveProductToFirebase(

        imageUrls: ArrayList<String>,

        productName: String,

        sellingPrice: Double,

        actualPrice: Double,

        discount: Double,

        stock: Int,

        description: String,

        weight: Double,

        shippingCharges: Double,

        category: String,

        subCategory: String

    ) {

        val productId =
            database.push().key


        if (productId == null) {

            resetUploadButton()

            Toast.makeText(
                this,
                "Firebase ID generation failed",
                Toast.LENGTH_LONG
            ).show()

            return
        }


        // ========================================================
        // IMPORTANT
        //
        // Yeh field names aapke PRODUCT data class ke EXACT
        // field names ke according hain.
        // ========================================================

        val productMap =
            hashMapOf<String, Any>(

                "productId" to productId,

                "imagesUrls" to imageUrls,

                "productName" to productName,

                "productPriceSelling" to sellingPrice,

                "productDescription" to description,

                "productPriceActual" to actualPrice,

                "discountOfProduct" to discount,

                "stockAvailable" to stock,

                "lowStockAlert" to 0,

                "colorsAvailable" to emptyList<String>(),

                "productWeight" to weight,

                "productVolume" to 0.0,

                "shippingCharges" to shippingCharges,

                "category" to category,

                "subCategory" to subCategory,

                "location" to "",

                "rating" to 0.0
            )


        // ========================================================
        // SAVE
        // ========================================================

        database
            .child(productId)
            .setValue(productMap)

            .addOnSuccessListener {

                resetUploadButton()


                Toast.makeText(
                    this,
                    "Product successfully uploaded!",
                    Toast.LENGTH_LONG
                ).show()


                // Optional:
                // finish()
            }

            .addOnFailureListener { e ->

                resetUploadButton()


                Toast.makeText(
                    this,
                    e.message
                        ?: "Firebase upload failed",
                    Toast.LENGTH_LONG
                ).show()
            }
    }


    // ============================================================
    // RESET UPLOAD BUTTON
    // ============================================================

    private fun resetUploadButton() {

        binding.btnNext.isEnabled =
            true

        binding.btnNext.text =
            "Upload Product"
    }


    // ============================================================
    // URI -> FILE
    // ============================================================

    private fun convertUriToFile(

        uri: Uri,

        fileName: String

    ): File {

        val tempFile =
            File(
                cacheDir,
                fileName
            )


        val inputStream =
            contentResolver
                .openInputStream(uri)


        if (inputStream == null) {

            throw IOException(
                "Unable to open image"
            )
        }


        inputStream.use { input ->

            FileOutputStream(tempFile)
                .use { output ->

                    input.copyTo(output)
                }
        }


        return tempFile
    }
}
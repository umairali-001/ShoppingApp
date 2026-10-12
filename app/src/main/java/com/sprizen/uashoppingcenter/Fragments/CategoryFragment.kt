package com.sprizen.uashoppingcenter.Fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sprizen.uashoppingcenter.Activities.ExploreActivity
import com.sprizen.uashoppingcenter.Adapters.CategoryNavAdapter
import com.sprizen.uashoppingcenter.Adapters.CategoryNavItem
import com.sprizen.uashoppingcenter.Adapters.CategoryProductAdapter
import com.sprizen.uashoppingcenter.Adapters.SubCategoryAdapter
import com.sprizen.uashoppingcenter.DATA_CLASS.PHOTO
import com.sprizen.uashoppingcenter.DATA_CLASS.PRODUCT
import com.sprizen.uashoppingcenter.DataBase
import com.sprizen.uashoppingcenter.R
import com.sprizen.uashoppingcenter.databinding.FragmentCategoryBinding

class CategoryFragment : Fragment() {

    private lateinit var binding: FragmentCategoryBinding
    private lateinit var dataBase: DataBase
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var navAdapter: CategoryNavAdapter
    private lateinit var subCategoryAdapter: SubCategoryAdapter
    private lateinit var productAdapter: CategoryProductAdapter

    private val categoriesList = listOf(
        CategoryNavItem("Fashion", R.drawable.ic_fashion_category1, "Fashion"),
        CategoryNavItem("Electronic", R.drawable.ic_electronic_category1, "Electronic"),
        CategoryNavItem("Home", R.drawable.ic_home_category1, "Home"),
        CategoryNavItem("Beauty", R.drawable.ic_beauty_category1, "Beauty"),
        CategoryNavItem("Sport", R.drawable.ic_sport_category1, "Sport"),
        CategoryNavItem("Books", R.drawable.ic_books_category1, "Books"),
        CategoryNavItem("Toys", R.drawable.ic_toys_category1, "Toys"),
        CategoryNavItem("Automotive", R.drawable.ic_automotive_category1, "Automotive"),
        CategoryNavItem("Pet Food", R.drawable.ic_pet_food_category1, "PetFood")
    )

    private val currentProducts = mutableListOf<PRODUCT>()
    private var selectedCategory: CategoryNavItem = categoriesList[0]

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeEveryThing()
    }

    private fun initializeEveryThing() {
        dataBase = DataBase(requireContext())

        sharedPreferences = requireContext().getSharedPreferences("isFirstRun", Context.MODE_PRIVATE)
        val isFirstRun = sharedPreferences.getBoolean("isFirstRun", true)
        if (isFirstRun) {
            addImagesToDataBase()
            sharedPreferences.edit().putBoolean("isFirstRun", false).apply()
        }

        setupNavigationRail()
        setupSubCategoriesGrid()
        setupPopularProducts()
        setupSearchActions()

        // Display initial category
        displayCategory(categoriesList[0])
    }

    private fun setupNavigationRail() {
        navAdapter = CategoryNavAdapter(requireContext(), categoriesList) { categoryItem ->
            selectedCategory = categoryItem
            displayCategory(categoryItem)
        }
        binding.rvCategoryNav.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCategoryNav.adapter = navAdapter
    }

    private fun setupSubCategoriesGrid() {
        subCategoryAdapter = SubCategoryAdapter(requireContext(), emptyList())
        binding.rvSubCategories.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvSubCategories.adapter = subCategoryAdapter
    }

    private fun setupPopularProducts() {
        productAdapter = CategoryProductAdapter(requireContext(), currentProducts)
        binding.rvCategoryPopularProducts.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.rvCategoryPopularProducts.adapter = productAdapter
    }

    private fun setupSearchActions() {
        val openSearch = {
            val intent = Intent(requireContext(), ExploreActivity::class.java)
            startActivity(intent)
        }
        binding.btnSearchCategory.setOnClickListener { openSearch() }

        binding.cardCategoryHeroBanner.setOnClickListener {
            val intent = Intent(requireContext(), ExploreActivity::class.java)
            intent.putExtra("category_query", selectedCategory.name)
            startActivity(intent)
        }

        binding.btnViewAllCategoryProducts.setOnClickListener {
            val intent = Intent(requireContext(), ExploreActivity::class.java)
            intent.putExtra("category_query", selectedCategory.name)
            startActivity(intent)
        }
    }

    private fun displayCategory(category: CategoryNavItem) {
        val photos = dataBase.getData(category.dbCategoryKey)

        if (photos.isNotEmpty()) {
            val heroPhoto = photos[0]
            if (heroPhoto.photoLink.isNotEmpty()) {
                Glide.with(this)
                    .load(heroPhoto.photoLink)
                    .centerCrop()
                    .placeholder(R.drawable.image_1)
                    .into(binding.ivCategoryHero)
            }

            val subCategories = if (photos.size > 1) photos.subList(1, photos.size) else emptyList()
            subCategoryAdapter.updateData(subCategories)
            binding.tvSubcategoriesCount.text = "${subCategories.size} items"
        } else {
            subCategoryAdapter.updateData(emptyList())
            binding.tvSubcategoriesCount.text = "0 items"
        }

        binding.tvCategoryBannerTitle.text = "${category.name} Collection"
        binding.tvPopularCategoryTitle.text = "Popular in ${category.name}"

        // Query Firebase products for this category
        loadProductsForCategory(category.name)
    }

    private fun loadProductsForCategory(categoryName: String) {
        FirebaseDatabase.getInstance().getReference("products")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    currentProducts.clear()
                    for (child in snapshot.children) {
                        val product = child.getValue(PRODUCT::class.java)
                        if (product != null) {
                            if (product.productId.isEmpty()) product.productId = child.key ?: ""
                            if (product.category.equals(categoryName, ignoreCase = true) ||
                                product.subCategory.contains(categoryName, ignoreCase = true)) {
                                currentProducts.add(product)
                            }
                        }
                    }

                    if (currentProducts.isEmpty()) {
                        binding.layoutPopularHeader.visibility = View.GONE
                        binding.rvCategoryPopularProducts.visibility = View.GONE
                    } else {
                        binding.layoutPopularHeader.visibility = View.VISIBLE
                        binding.rvCategoryPopularProducts.visibility = View.VISIBLE
                        productAdapter.updateProducts(currentProducts)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun addImagesToDataBase() {
        // For Fashion Category
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783333080/Fashion_Category_x5di4n.jpg", "Fashion", "Fashion")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783321765/Men_s_Cloths_o8plaj.jpg", "Men's Clothing", "Fashion")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783321767/Women_s_Cloths_b3x96z.jpg", "Women's Clothing", "Fashion")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783321762/Kid_s_Clothes_mcjll5.jpg", "Kids Clothing", "Fashion")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783321761/Footwear_awiwsb.jpg", "Footwear", "Fashion")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783321766/Witches_and_Accessories_qgjtti.jpg", "Watches & Accessories", "Fashion")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783321763/jewelry_wznj12.jpg", "Jewelry", "Fashion")

        // For Electronic Category
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783333081/Electronic_Category_aq7krw.jpg", "Electronics", "Electronic")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783321764/Mobile_Accessories_fjhmnp.jpg", "Mobiles & Accessories", "Electronic")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783321762/Laptops_and_Computers_kirhyw.jpg", "Laptops & Computers", "Electronic")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783321761/Headphones_atqsqa.jpg", "Headphones & Audio", "Electronic")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783321762/Camera_aznvnk.jpg", "Cameras", "Electronic")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783321764/Smarts_Wearabele_hmt90g.jpg", "Smart Wearables", "Electronic")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783321763/Home_Appliances_oqpry0.jpg", "Home Appliances", "Electronic")

        // For Home Category
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783333081/Home_Category_ncz1ke.jpg", "Home Living", "Home")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783328662/Furniture_wydvpc.jpg", "Furniture", "Home")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783328663/Home_Decor_nk2oe1.jpg", "Home Decor", "Home")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783328663/Kitchen_Products_qnztgu.jpg", "Kitchenware", "Home")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783328663/Budding_and_Towels_phhsdb.jpg", "Bedding & Towels", "Home")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783328663/Lightning_c0vd5j.jpg", "Lighting", "Home")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783328663/Home_Improvements_vizhy4.jpg", "Tools & Improvement", "Home")

        // For Beauty Category
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783332185/Beauty_Category_ejio14.jpg", "Beauty & Care", "Beauty")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783332184/Makeup_vehpnk.jpg", "Makeup", "Beauty")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783332184/Skin_Care_zgckmz.jpg", "Skincare", "Beauty")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783332183/Haircare_k2pain.jpg", "Haircare", "Beauty")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783332185/Perfumes_ckazlf.jpg", "Fragrances & Perfumes", "Beauty")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783332185/Personal_Care_bb4iml.jpg", "Personal Care Appliances", "Beauty")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783332183/Beauty_Cream_s_qhoggb.jpg", "Cosmetic Creams", "Beauty")

        // For Sports Category
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783334566/Sports_Category_f22ekq.jpg", "Sports & Outdoors", "Sport")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783334564/Fitness_Equipment_zqxunc.jpg", "Fitness Equipment", "Sport")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783334565/Sport_s_Clothing_rivrcq.jpg", "Sports Clothing", "Sport")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783334565/Graphics_Shoe_s_yqgfgw.jpg", "Graphic Shoes", "Sport")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783334565/Outdoor_Camping_kyxwf9.jpg", "Outdoor & Camping", "Sport")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783334564/Cycling_hz8h4x.jpg", "Cycling", "Sport")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783334565/Indoor_Game_s_fxswbj.jpg", "Indoor Games", "Sport")

        // For Books Category
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783336008/Books_Category_f71p8c.jpg", "Books & Literature", "Books")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783336006/Literature_cj26wx.jpg", "Fiction & Literature", "Books")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783336006/Non_Fiction_xgx0tj.jpg", "Non-Fiction", "Books")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783336003/Academic_Textbook_qdeuvz.jpg", "Academic & Textbooks", "Books")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783336004/Kid_s_Books_bdgcue.jpg", "Children & Kids Books", "Books")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783336005/Self-Improves_yrx6jn.jpg", "Self-Help & Personal Growth", "Books")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783336004/Biographies_pebmvf.jpg", "Biographies", "Books")

        // For Toys Category
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783337299/Toys_Category_qyljdr.jpg", "Toys & Games", "Toys")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783337299/Action_Figures_ehiilg.jpg", "Action Figures & Dolls", "Toys")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783337299/Puzzle_Board_pljfvy.jpg", "Board Games & Puzzles", "Toys")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783337299/Educational_Toys_uj4aem.jpg", "Educational Toys", "Toys")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783337305/Remote_Toys_tvq9k6.jpg", "Remote Control Toys", "Toys")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783337304/Soft_Toys_tzywl9.jpg", "Soft & Plush Toys", "Toys")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783337302/Reassemble_Toys_jaxh7r.jpg", "Building Blocks & Sets", "Toys")

        // For Automotive Category
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783343681/Automotives_Category_a3jymt.jpg", "Automotive & Tools", "Automotive")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783343679/Car_s_Accessories_x8b82l.jpg", "Car Accessories", "Automotive")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783343679/Bike_Accessories_kqmzlt.jpg", "Bike Accessories", "Automotive")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783343678/Car_Cleaning_cqdxzq.jpg", "Car Care & Cleaning", "Automotive")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783343679/Ridding_and_Helmet_fdxl0q.jpg", "Helmets & Riding Gear", "Automotive")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783343678/Spare_Part_s_r1qqbw.jpg", "Spare Parts", "Automotive")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783334564/Cycling_hz8h4x.jpg", "Cycling Gear", "Automotive")

        // For PetFood
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783344883/Petfood_Category_e24fxo.jpg", "Pet Supplies", "PetFood")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783344879/Dog_Food_j3kvr2.jpg", "Dog Food", "PetFood")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783344877/Cats_Food_gzmj0v.jpg", "Cat Food", "PetFood")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783344877/Birds_Food_m1w7co.jpg", "Bird Food", "PetFood")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783344879/Fish_Food_dif6ot.jpg", "Fish & Aquatic Food", "PetFood")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783344880/Pet_Treatments_oczeh9.jpg", "Pet Treats & Supplements", "PetFood")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1782994289/Make_cove_image_for_app_202607011954_ycru7b.jpg", "More Pet Essentials", "PetFood")
    }
}

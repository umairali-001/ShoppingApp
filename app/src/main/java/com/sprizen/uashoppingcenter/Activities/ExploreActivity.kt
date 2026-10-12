package com.sprizen.uashoppingcenter.Activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sprizen.uashoppingcenter.Adapters.AdapterItem
import com.sprizen.uashoppingcenter.Adapters.ResecntSearhesAdapter
import com.sprizen.uashoppingcenter.DATA_CLASS.PRODUCT
import com.sprizen.uashoppingcenter.DATA_CLASS.SEARCH
import com.sprizen.uashoppingcenter.DataBase
import com.sprizen.uashoppingcenter.Fragments.HomeFragment
import com.sprizen.uashoppingcenter.R
import com.sprizen.uashoppingcenter.databinding.ActivityExploreBinding

class ExploreActivity : AppCompatActivity(),
    ResecntSearhesAdapter.OnSearchItemClickListener {

    lateinit var binding: ActivityExploreBinding

    lateinit var masterProductList: MutableList<PRODUCT>
    lateinit var displayedProductList: MutableList<PRODUCT>

    lateinit var dataBase: DataBase
    lateinit var adapterItem: AdapterItem
    lateinit var rececntSearhesAdapter: ResecntSearhesAdapter
    lateinit var searchesList: MutableList<SEARCH>
    lateinit var optimizeSearchList: MutableList<SEARCH>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityExploreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeEveryThing()
    }

    fun initializeEveryThing() {
        dataBase = DataBase(this@ExploreActivity)

        searchesList = dataBase.getRecentSearches()

        masterProductList = ArrayList()
        displayedProductList = ArrayList()
        optimizeSearchList = ArrayList()

        // Remove duplicate searches
        searchesList = searchesList.distinctBy { it.recentSearch }.toMutableList()

        // Show maximum 10 unique searches
        optimizeSearchList = searchesList.take(10).toMutableList()

        adapterItem = AdapterItem(this@ExploreActivity, displayedProductList, HomeFragment())

        rececntSearhesAdapter = ResecntSearhesAdapter(this@ExploreActivity, optimizeSearchList)

        binding.recentSearchesRecyclerView.adapter = rececntSearhesAdapter

        val flexboxLayoutManager = FlexboxLayoutManager(this).apply {
            flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.FLEX_START
            alignItems = AlignItems.FLEX_START
        }

        binding.recentSearchesRecyclerView.layoutManager = flexboxLayoutManager

        setUpRecyclerView()
        getProductsFromFirebase()
        AllClickListener()
    }

    //=====================================================================================
    //              All ClickListeners
    //=====================================================================================

    fun AllClickListener() {
        binding.backIcon.setOnClickListener {
            handleBackNavigation()
        }

        // Search Bar Focus Change
        binding.searchBar.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.searchLinearLayout.visibility = View.VISIBLE
                binding.backIcon.visibility = View.VISIBLE
                binding.productImageRecyclerView.visibility = View.GONE
            } else {
                binding.searchLinearLayout.visibility = View.GONE
                binding.backIcon.visibility = View.GONE
            }
        }

        // Search Button
        binding.searchBtn.setOnClickListener {
            val recentSearch = binding.searchBar.text.toString().trim()
            if (recentSearch.isEmpty()) return@setOnClickListener

            dataBase.insertResentSearch(recentSearch)

            binding.searchLinearLayout.visibility = View.GONE
            binding.productImageRecyclerView.visibility = View.VISIBLE
            binding.backIcon.visibility = View.VISIBLE
            binding.searchBar.clearFocus()

            searchProducts(recentSearch)
        }

        // Clear Recent Searches (Soft Delete implementation)
        binding.clearSearchBtn.setOnClickListener {
            // Database se sabhi searches ko soft delete (IS_DELETED = 'TRUE') kar denge
            dataBase.deleteAllSearches()

            searchesList.clear()
            optimizeSearchList.clear()

            rececntSearhesAdapter = ResecntSearhesAdapter(
                this@ExploreActivity,
                optimizeSearchList
            )
            binding.recentSearchesRecyclerView.adapter = rececntSearhesAdapter
        }
    }

    private fun handleBackNavigation() {
        if (binding.productImageRecyclerView.visibility == View.VISIBLE &&
            binding.searchLinearLayout.visibility == View.GONE) {

            binding.productImageRecyclerView.visibility = View.GONE
            binding.searchLinearLayout.visibility = View.VISIBLE
            binding.searchBar.requestFocus()
        } else {
            finish()
        }
    }

    override fun onBackPressed() {
        handleBackNavigation()
    }

    //=====================================================================================
    //              Get Products From Firebase
    //=====================================================================================

    fun getProductsFromFirebase() {
        val database = FirebaseDatabase
            .getInstance()
            .getReference("products")

        database.addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    masterProductList.clear()

                    for (child in snapshot.children) {
                        val product = child.getValue(PRODUCT::class.java)
                        if (product != null) {
                            if (product.productId.isEmpty()) {
                                product.productId = child.key ?: ""
                            }
                            masterProductList.add(product)
                        }
                    }

                    val uniqueProducts = masterProductList.distinctBy { it.productId }
                    masterProductList.clear()
                    masterProductList.addAll(uniqueProducts)

                    displayedProductList.clear()
                    displayedProductList.addAll(masterProductList)
                    adapterItem.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@ExploreActivity,
                        "Firebase Error: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
    }

    //=====================================================================================
    //              Search Products
    //=====================================================================================

    fun searchProducts(queryText: String) {
        val query = queryText.trim().lowercase()
        if (query.isEmpty()) return

        val result = masterProductList
            .filter { product ->
                product.productName.lowercase().contains(query)
            }
            .distinctBy { it.productId }
            .sortedByDescending { product ->
                val title = product.productName.lowercase()
                title.windowed(query.length).count { it == query }
            }

        displayedProductList.clear()
        displayedProductList.addAll(result)
        adapterItem.notifyDataSetChanged()

        binding.productImageRecyclerView.visibility = View.VISIBLE
    }

    //=====================================================================================
    //              RecyclerView
    //=====================================================================================

    fun setUpRecyclerView() {
        val recyclerView = binding.productImageRecyclerView
        recyclerView.adapter = adapterItem
        recyclerView.layoutManager = GridLayoutManager(
            this@ExploreActivity,
            2,
            GridLayoutManager.VERTICAL,
            false
        )
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(20)
    }

    //=====================================================================================
    //              Recent Search Click
    //=====================================================================================

    override fun onItemClick(itemText: String) {
        binding.searchBar.setText(itemText)
        binding.searchLinearLayout.visibility = View.GONE
        binding.productImageRecyclerView.visibility = View.VISIBLE
        binding.searchBar.clearFocus()

        searchProducts(itemText)
    }
}
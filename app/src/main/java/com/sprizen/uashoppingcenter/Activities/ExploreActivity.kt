package com.sprizen.uashoppingcenter.Activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.api.Context
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.sprizen.uashoppingcenter.Adapters.AdapterItem
import com.sprizen.uashoppingcenter.Adapters.ResecntSearhesAdapter
import com.sprizen.uashoppingcenter.DATA_CLASS.PRODUCT
import com.sprizen.uashoppingcenter.DataBase
import com.sprizen.uashoppingcenter.Fragments.HomeFragment
import com.sprizen.uashoppingcenter.R
import com.sprizen.uashoppingcenter.databinding.ActivityExploreBinding
import okhttp3.*
import java.lang.reflect.Constructor

class ExploreActivity : AppCompatActivity(), ResecntSearhesAdapter.OnSearchItemClickListener {

    lateinit var binding: ActivityExploreBinding


    lateinit var productList : MutableList<PRODUCT>

    lateinit var dataBase: DataBase

    lateinit var adapterItem : AdapterItem
    lateinit var rececntSearhesAdapter: ResecntSearhesAdapter
    lateinit var searchesList : MutableList<String>
     lateinit var reverseSearchList : MutableList<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        binding = ActivityExploreBinding.inflate(layoutInflater)
        setContentView(binding.root)


        initializeEveryThing()

    }

    fun initializeEveryThing() {

        AllClickListener()
        dataBase= DataBase(this@ExploreActivity)
        searchesList = dataBase.getRecentSearches().reversed() as MutableList
        reverseSearchList = ArrayList()
        productList = ArrayList()

        adapterItem = AdapterItem(this@ExploreActivity,productList, HomeFragment())


        rececntSearhesAdapter= ResecntSearhesAdapter(this@ExploreActivity,searchesList)
        binding.recentSearchesRecyclerView.adapter= rececntSearhesAdapter
        binding.recentSearchesRecyclerView.layoutManager
        val flexboxLayoutManager = FlexboxLayoutManager(this).apply { flexDirection = FlexDirection.ROW
            flexWrap = FlexWrap.WRAP
            justifyContent = JustifyContent.FLEX_START
            alignItems = AlignItems.FLEX_START
        }

        binding.recentSearchesRecyclerView.layoutManager = flexboxLayoutManager
        binding.recentSearchesRecyclerView.adapter = rececntSearhesAdapter

    }

    //=====================================================================================
    //              All ClickListeners
    //=====================================================================================
    fun AllClickListener(){
        binding.backIcon.setOnClickListener {

            binding.backIcon.setBackgroundResource(R.drawable.button_click_background)
            binding.backIcon.postDelayed({ binding.backIcon.background = null }, 50)
            finish()
        }

        binding.searchBtn.setOnClickListener {
            if (binding.searchBar.text.isEmpty()){

                return@setOnClickListener

            }else {

                var recentSearch = binding.searchBar.text.toString()

                var result = dataBase.insertResentSearch(recentSearch)

                Toast.makeText(this@ExploreActivity, recentSearch, Toast.LENGTH_SHORT).show()

                Toast.makeText(this@ExploreActivity, result, Toast.LENGTH_SHORT).show()

                binding.searchLinearLayout.visibility = View.GONE

                getProductsFromFirebase()
                binding.productImageRecyclerView.visibility = View.VISIBLE
                binding.searchBar.clearFocus()
                setUpRecyclerView()



            }
            binding.searchBar.setOnClickListener {
                if (binding.searchLinearLayout.visibility== View.GONE){
                    binding.searchLinearLayout.visibility = View.VISIBLE
                }
                else{
                    return@setOnClickListener
                }
            }



        }
    }

    fun getProductsFromFirebase() {

        var database = FirebaseDatabase.getInstance().getReference("products")

        database.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                productList.clear()

                for (child in snapshot.children) {

                    var product = child.getValue(PRODUCT::class.java)
                    if (product != null) {

                        // Firebase key
                        if (product.productId.isEmpty()) {

                            product.productId =
                                child.key ?: ""
                        }


                        productList.add(product)
                    }
                }


                adapterItem.notifyDataSetChanged()
            }


            override fun onCancelled(error: DatabaseError) {

                Toast.makeText(this@ExploreActivity, "Firebase Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    fun setUpRecyclerView(){


        var recyclerView = binding.productImageRecyclerView


        recyclerView.adapter = adapterItem

        recyclerView.layoutManager = GridLayoutManager(this@ExploreActivity, 2, GridLayoutManager.VERTICAL, false)


        recyclerView.setHasFixedSize(true)

        recyclerView.setItemViewCacheSize(20)

            val query = binding.searchBar.text.toString().trim().lowercase()

            if (query.isEmpty()) return

            FirebaseFirestore.getInstance()
                .collection("products")
                .get()
                .addOnSuccessListener { documents ->

                    val result = documents.documents.filter { document ->

                        val title = document.getString("title")
                            ?.lowercase()
                            ?: ""

                        title.contains(query)
                    }.toMutableList()

                    result.sortWith(Comparator { product1, product2 ->

                        val title1 = product1.getString("title")
                            ?.lowercase()
                            ?: ""

                        val title2 = product2.getString("title")
                            ?.lowercase()
                            ?: ""

                        val count1 = title1
                            .windowed(query.length)
                            .count { it == query }

                        val count2 = title2
                            .windowed(query.length)
                            .count { it == query }

                        count2 - count1
                    })

                    // Ab result already relevance ke according sorted hai
                    // RecyclerView ko result pass karein
                }
        }

    override fun onItemClick(itemText: String) {
        binding.searchBar.setText(itemText)

        binding.searchLinearLayout.visibility = View.GONE

        getProductsFromFirebase()
        binding.productImageRecyclerView.visibility = View.VISIBLE
        binding.searchBar.clearFocus()


    }


}
package com.sprizen.uashoppingcenter.Activities

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.sprizen.uashoppingcenter.Fragments.CartFragment
import com.sprizen.uashoppingcenter.Fragments.CategoryFragment
import com.sprizen.uashoppingcenter.Fragments.HomeFragment
import com.sprizen.uashoppingcenter.Fragments.ProfileFragment
import com.sprizen.uashoppingcenter.R
import com.sprizen.uashoppingcenter.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    lateinit var binding: ActivityHomeBinding

    lateinit var fragmentList: MutableList<Fragment>
    lateinit var imageList : MutableList<ImageView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        initializeEveryThing()

        ViewCompat.setOnApplyWindowInsetsListener(binding.tabLayout1) { _, insets ->

            val navigationInsets =
                insets.getInsets(WindowInsetsCompat.Type.navigationBars())

            val params =
                binding.tabLayout1.layoutParams as ViewGroup.MarginLayoutParams

            params.bottomMargin = navigationInsets.bottom

            binding.tabLayout1.layoutParams = params

            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.tabLayout) { _, insets ->

            val navigationInsets =
                insets.getInsets(WindowInsetsCompat.Type.navigationBars())

            val params =
                binding.tabLayout.layoutParams as ViewGroup.MarginLayoutParams

            params.bottomMargin = navigationInsets.bottom

            binding.tabLayout.layoutParams = params

            insets
        }
    }

    //===================================================================================================================

    fun initializeEveryThing(){
//        window.statusBarColor = ContextCompat.getColor(this, android.R.color.white)
//        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true




        binding.viewPager2.isUserInputEnabled = false






        imageList = ArrayList()

        var home = findViewById<ImageView>(R.id.homeBT)
        var explore = findViewById<ImageView>(R.id.exploreBT)
        var fav = findViewById<ImageView>(R.id.favBT)
        var profile = findViewById<ImageView>(R.id.profileBT)
        imageList.add(home)
        imageList.add(explore)
        imageList.add(fav)
        imageList.add(profile)






        setUpViewPager()
        binding.tabLayout.addTab(binding.tabLayout.newTab())
        binding.tabLayout.addTab(binding.tabLayout.newTab())
        binding.tabLayout.addTab(binding.tabLayout.newTab())
        binding.tabLayout.addTab(binding.tabLayout.newTab())

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab) {
                TabLayout.INDICATOR_ANIMATION_MODE_ELASTIC
                binding.viewPager2.currentItem = tab.position
                tabLayoutManager(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                tabLayoutManager(tab.position)

            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                tabLayoutManager(tab.position)

            }

        })

        binding.viewPager2.registerOnPageChangeCallback(object  : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {

                binding.tabLayout.getTabAt(position)?.select()
            }

        })

    }




    fun tabLayoutManager(position: Int) {

        val iconsList = listOf(
            R.drawable.ic_home,
            R.drawable.ic_category,
            R.drawable.ic_cart,
            R.drawable.ic_profile
        )

        val selectedColor = ContextCompat.getColor(
            this@HomeActivity,
            R.color.primaryGreenColor
        )

        for (i in imageList.indices) {

            imageList[i].setImageResource(iconsList[i])

            if (i == position) {

                // Selected icon → tint
                imageList[i].setColorFilter(selectedColor)

            } else {

                // Unselected icon → tint remove
                imageList[i].colorFilter = null
            }
        }
    }



    fun setUpViewPager() {
        fragmentList = ArrayList()

        var firstF = HomeFragment()
        var secondF = CategoryFragment()
        var thridF = CartFragment()
        var fourthF = ProfileFragment()

        fragmentList.add(firstF)
        fragmentList.add(secondF)
        fragmentList.add(thridF)
        fragmentList.add(fourthF)


        var fragmentAdapter = FragmentAdapter(supportFragmentManager, lifecycle, fragmentList)

        binding.viewPager2.adapter = fragmentAdapter


    }

    class FragmentAdapter(
        fragmentManager: FragmentManager, lifecycle: Lifecycle,
        fragmentList:
        MutableList<Fragment>,
    ) : FragmentStateAdapter(fragmentManager, lifecycle) {

        var fragmentList: MutableList<Fragment>

        init {
            this.fragmentList = fragmentList
        }

        override fun createFragment(position: Int): Fragment {
            return fragmentList[position]

        }

        override fun getItemCount(): Int {
            return fragmentList.size
        }


    }
}
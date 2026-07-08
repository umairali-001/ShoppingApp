package com.sprizen.uashoppingcenter.Fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.sprizen.uashoppingcenter.Activities.ExploreActivity
import com.sprizen.uashoppingcenter.R
import com.sprizen.uashoppingcenter.Adapters.SliderAdapter
import com.sprizen.uashoppingcenter.Adapters.AdapterItem
import com.sprizen.uashoppingcenter.DATA_CLASS.ITEM
import com.sprizen.uashoppingcenter.databinding.FragmentHomeBinding
import kotlin.jvm.java
import kotlin.math.abs

class HomeFragment : Fragment() {


    lateinit var binding: FragmentHomeBinding

    private lateinit var viewPager: ViewPager2
    private lateinit var dotsContainer: LinearLayout

    lateinit var itemList: MutableList<ITEM>
    lateinit var adapterItem: AdapterItem

    private val imagesList = listOf(
        R.drawable.image_1,
        R.drawable.images_2,
        R.drawable.images_3
    )

    private val sliderHandler = Handler(Looper.getMainLooper())

    private val sliderRunnable = Runnable {
        viewPager.currentItem = viewPager.currentItem + 1
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding  = FragmentHomeBinding.inflate(layoutInflater)


        initializeEveryThing()



        return binding.root
    }


    fun initializeEveryThing(){

        binding.btnSearch.setOnClickListener {

            val animation = AnimationUtils.loadAnimation(
                requireContext(),
                R.anim.button_animation
            )

            // کلک ہوتے ہی Background لگ جائے
            binding.btnSearch.setBackgroundResource(R.drawable.button_click_background)

            animation.setAnimationListener(object : Animation.AnimationListener {

                override fun onAnimationStart(animation: Animation?) {}

                override fun onAnimationEnd(animation: Animation?) {

                    // Animation ختم ہوتے ہی Background ہٹا دیں
                    binding.btnSearch.background = null

                    binding.searchLinearLayout.visibility = View.VISIBLE
                }

                override fun onAnimationRepeat(animation: Animation?) {}
            })

            binding.btnSearch.startAnimation(animation)
        }


        binding.backIcon.setOnClickListener {

            val animation = AnimationUtils.loadAnimation(
                requireContext(),
                R.anim.button_animation
            )

            // کلک ہوتے ہی Background لگ جائے
            binding.backIcon.setBackgroundResource(R.drawable.button_click_background)

            animation.setAnimationListener(object : Animation.AnimationListener {

                override fun onAnimationStart(animation: Animation?) {}

                override fun onAnimationEnd(animation: Animation?) {

                    // Animation ختم ہوتے ہی Background ہٹا دیں
                    binding.backIcon.background = null

                    binding.searchLinearLayout.visibility = View.GONE
                }

                override fun onAnimationRepeat(animation: Animation?) {}
            })

            binding.backIcon.startAnimation(animation)

        }


        binding.cartContainer.setOnClickListener {
            val animation = AnimationUtils.loadAnimation(
                requireContext(),
                R.anim.button_animation
            )

            // کلک ہوتے ہی Background لگ جائے
            binding.cartContainer.setBackgroundResource(R.drawable.button_click_background)

            animation.setAnimationListener(object : Animation.AnimationListener {

                override fun onAnimationStart(animation: Animation?) {}

                override fun onAnimationEnd(animation: Animation?) {

                    // Animation ختم ہوتے ہی Background ہٹا دیں
                    binding.cartContainer.background = null

                    val tabLayout = requireActivity().findViewById<TabLayout>(R.id.tabLayout)

                    tabLayout.selectTab(tabLayout.getTabAt(2))

                }

                override fun onAnimationRepeat(animation: Animation?) {}
            })

            binding.cartContainer.startAnimation(animation)

        }

        binding.menuBtn.setOnClickListener {
            val animation = AnimationUtils.loadAnimation(
                requireContext(),
                R.anim.button_animation
            )

            // کلک ہوتے ہی Background لگ جائے
            binding.menuBtn.setBackgroundResource(R.drawable.button_click_background)

            animation.setAnimationListener(object : Animation.AnimationListener {

                override fun onAnimationStart(animation: Animation?) {}

                override fun onAnimationEnd(animation: Animation?) {

                    // Animation ختم ہوتے ہی Background ہٹا دیں
                    binding.menuBtn.background = null

                    val tabLayout = requireActivity().findViewById<TabLayout>(R.id.tabLayout)

                    tabLayout.selectTab(tabLayout.getTabAt(3))
                }

                override fun onAnimationRepeat(animation: Animation?) {}
            })

            binding.menuBtn.startAnimation(animation)

        }







    }


















    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        itemList = mutableListOf<ITEM>()
        val singleItem = ITEM(
            "R.drawable.image_1",
            "Testing Item this is my testing item i im a programmer",
            "12,4445",
            "4.5"
        )

        repeat(20) { itemList.add(singleItem) }

        adapterItem = AdapterItem(requireContext(), itemList)


        var recyclerView = view.findViewById<RecyclerView>(R.id.item_show_home_recyclerView)

        recyclerView.adapter = adapterItem
        recyclerView.layoutManager = GridLayoutManager(
            requireContext(), 2, GridLayoutManager.VERTICAL, false
        )
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(20)


        Toast.makeText(requireContext(), "${itemList.size}", Toast.LENGTH_SHORT).show()

        imageSlider(view)





    }
    override fun onPause() {
        super.onPause()
        sliderHandler.removeCallbacks(sliderRunnable)
    }

    override fun onResume() {
        super.onResume()
        sliderHandler.postDelayed(sliderRunnable, 3500)
    }








    //image Slider Function Created by ChatGpt
    fun imageSlider(view: View){
        // Fragment me views ko dhundne ke liye view.findViewById use kiya jata hai
        viewPager = view.findViewById(R.id.viewPager)
        dotsContainer = view.findViewById(R.id.dotsContainer)

        val adapter = SliderAdapter(imagesList)
        viewPager.adapter = adapter

        // ViewPager2 ke andar ke RecyclerView ki clipping false karna
        val child = viewPager.getChildAt(0)
        if (child is RecyclerView) {
            child.clipChildren = false
            child.clipToPadding = false
            child.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }

        // Infinite position logic
        val middlePosition = (Integer.MAX_VALUE / 2) - ((Integer.MAX_VALUE / 2) % imagesList.size)
        viewPager.setCurrentItem(middlePosition, false)

        setupDots(imagesList.size)
        updateDots(0)

        // Perfect Margin and Scale Transformer
        val pageTransformer = CompositePageTransformer().apply {
            addTransformer(MarginPageTransformer(16))
            addTransformer { page, position ->
                val r = 1 - abs(position)
                page.scaleY = 0.85f + r * 0.15f
                page.scaleX = 0.88f + r * 0.12f
                page.alpha = 0.6f + r * 0.4f
            }
        }
        viewPager.setPageTransformer(pageTransformer)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val realPosition = position % imagesList.size
                updateDots(realPosition)

                sliderHandler.removeCallbacks(sliderRunnable)
                sliderHandler.postDelayed(sliderRunnable, 3500)
            }
        })
    }

    private fun setupDots(size: Int) {
        context?.let { ctx ->
            dotsContainer.removeAllViews()
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(10, 0, 10, 0)
            }

            for (i in 0 until size) {
                val dot = ImageView(ctx)
                dot.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.dot_inactive))
                dotsContainer.addView(dot, params)
            }
        }
    }

    private fun updateDots(currentPosition: Int) {
        context?.let { ctx ->
            for (i in 0 until dotsContainer.childCount) {
                val imageView = dotsContainer.getChildAt(i) as ImageView
                if (i == currentPosition) {
                    imageView.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.dot_active))
                } else {
                    imageView.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.dot_inactive))
                }
            }
        }
    }

}
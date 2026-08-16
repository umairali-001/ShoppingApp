package com.sprizen.uashoppingcenter.Fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sprizen.uashoppingcenter.Activities.ExploreActivity
import com.sprizen.uashoppingcenter.Adapters.AdapterItem
import com.sprizen.uashoppingcenter.Adapters.SliderAdapter
import com.sprizen.uashoppingcenter.DATA_CLASS.PRODUCT
import com.sprizen.uashoppingcenter.R
import com.sprizen.uashoppingcenter.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {
    lateinit var itemList: MutableList<PRODUCT>
    lateinit var adapterItem: AdapterItem

    lateinit var binding: FragmentHomeBinding

    // ==========================================
    // SLIDER
    // ==========================================

    lateinit var sliderAdapter: SliderAdapter

    val sliderImages = listOf(R.drawable.image_1, R.drawable.images_2, R.drawable.images_3)

    private val sliderHandler = Handler(Looper.getMainLooper())

    private var currentSliderPosition = 0

    private val sliderRunnable = object : Runnable {

        override fun run() {

            if (sliderImages.isNotEmpty()) {

                currentSliderPosition++

                binding.viewPager.setCurrentItem(
                    currentSliderPosition,
                    true
                )

                sliderHandler.postDelayed(
                    this,
                    3000
                )
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        binding = FragmentHomeBinding.bind(view)

        initializeEveryThing()

    }




    fun initializeEveryThing(){

        // ==========================================
        // PRODUCT LIST
        // ==========================================

        itemList = mutableListOf()




        // ==========================================
        // ADAPTER
        // ==========================================

        adapterItem = AdapterItem(requireContext(), itemList,this@HomeFragment)


        // ==========================================
        // RECYCLER VIEW
        // ==========================================

        var recyclerView = binding.itemShowHomeRecyclerView


        recyclerView.adapter = adapterItem

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)


        recyclerView.setHasFixedSize(true)

        recyclerView.setItemViewCacheSize(20)


        // ==========================================
        // GET FIREBASE DATA
        // ==========================================

        getProductsFromFirebase()

        setupSlider()
        setupDots()
        clickListeners()

    }



    fun clickListeners(){
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

                    startActivity(Intent(requireContext(), ExploreActivity::class.java))
                }

                override fun onAnimationRepeat(animation: Animation?) {}
            })

            binding.btnSearch.startAnimation(animation)

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

                    binding.cartContainer.background = null

                    // Cart Fragment par jao
                    requireActivity()
                        .findViewById<ViewPager2>(R.id.viewPager2)
                        .currentItem = 2
                }

                override fun onAnimationRepeat(animation: Animation?) {}
            })

            binding.cartContainer.startAnimation(animation)
        }

    }



    fun getProductsFromFirebase() {

        var database = FirebaseDatabase.getInstance().getReference("products")

        database.addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    itemList.clear()

                    for (child in snapshot.children) {

                        var product = child.getValue(PRODUCT::class.java)
                        if (product != null) {

                            // Firebase key
                            if (product.productId.isEmpty()) {

                                product.productId =
                                    child.key ?: ""
                            }


                            itemList.add(product)
                        }
                    }


                    adapterItem.notifyDataSetChanged()
                }


                override fun onCancelled(error: DatabaseError) {

                    Toast.makeText(requireContext(), "Firebase Error: ${error.message}", Toast.LENGTH_LONG).show()
                }
            }
        )
    }


    // ==========================================
    // SLIDER SETUP
    // ==========================================

    private fun setupSlider() {

        sliderAdapter = SliderAdapter(sliderImages)

        binding.viewPager.adapter = sliderAdapter

        // Initial position
        currentSliderPosition = 0

        binding.viewPager.setCurrentItem(
            currentSliderPosition,
            false
        )

        setupDots()

        binding.viewPager.registerOnPageChangeCallback(

            object : ViewPager2.OnPageChangeCallback() {

                override fun onPageSelected(position: Int) {

                    super.onPageSelected(position)

                    currentSliderPosition = position

                    val realPosition =
                        position % sliderImages.size

                    updateDots(realPosition)
                }
            }
        )

        // Auto slide start
        startAutoSlider()
    }


    // ==========================================
    // DOTS SETUP
    // ==========================================

    private fun setupDots() {

        val dotsContainer = binding.dotsContainer

        dotsContainer.removeAllViews()

        for (i in sliderImages.indices) {

            val dot = View(requireContext())

            val params = LinearLayout.LayoutParams(
                if (i == 0) 15 else 15,
                15
            )

            params.setMargins(
                3,
                3,
                3,
                3
            )

            dot.layoutParams = params

            if (i == 0) {

                dot.setBackgroundResource(
                    R.drawable.dot_active
                )

            } else {

                dot.setBackgroundResource(
                    R.drawable.dot_inactive
                )
            }

            dotsContainer.addView(dot)
        }
    }


    // ==========================================
    // UPDATE DOTS
    // ==========================================

    private fun updateDots(activePosition: Int) {

        val dotsContainer = binding.dotsContainer

        for (i in 0 until dotsContainer.childCount) {

            val dot = dotsContainer.getChildAt(i)

            val params = dot.layoutParams
                    as LinearLayout.LayoutParams

            if (i == activePosition) {

                params.width = 15
                params.height = 15

                dot.setBackgroundResource(
                    R.drawable.dot_active
                )

            } else {

                params.width = 15
                params.height = 15

                dot.setBackgroundResource(
                    R.drawable.dot_inactive
                )
            }

            params.setMargins(
                3,
                3,
                3,
                3
            )

            dot.layoutParams = params
        }
    }


    // ==========================================
    // AUTO SLIDER
    // ==========================================

    private fun startAutoSlider() {

        sliderHandler.removeCallbacks(
            sliderRunnable
        )

        sliderHandler.postDelayed(
            sliderRunnable,
            3000
        )
    }


    // ==========================================
    // STOP AUTO SLIDER
    // ==========================================

    private fun stopAutoSlider() {

        sliderHandler.removeCallbacks(
            sliderRunnable
        )
    }



    // ==========================================
    // PAUSE AUTO SLIDER
    // ==========================================

    override fun onPause() {

        super.onPause()

        stopAutoSlider()
    }


    // ==========================================
    // RESUME AUTO SLIDER
    // ==========================================

    override fun onResume() {

        super.onResume()

        if (sliderImages.isNotEmpty()) {

            startAutoSlider()
        }
    }


    // ==========================================
    // DESTROY VIEW
    // ==========================================

    override fun onDestroyView() {

        stopAutoSlider()

        super.onDestroyView()
    }


}
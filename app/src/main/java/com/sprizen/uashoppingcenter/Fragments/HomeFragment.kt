package com.sprizen.uashoppingcenter.Fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
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
import kotlinx.coroutines.delay

class HomeFragment : Fragment(R.layout.fragment_home), AdapterItem.OnButtonClickListener {
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
                binding.viewPager.setCurrentItem(currentSliderPosition, true)
                sliderHandler.postDelayed(this, 5000)
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        binding = FragmentHomeBinding.bind(view)

        initializeEveryThing()

    }





    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        binding = FragmentHomeBinding.inflate(layoutInflater)

        initializeEveryThing()

        return binding.root
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


            binding.btnSearch.setBackgroundResource(R.drawable.button_click_background)
            binding.btnSearch.postDelayed({ binding.btnSearch.background=null },50)
            startActivity(Intent(requireContext(), ExploreActivity::class.java))
        }

        binding.cartContainer.setOnClickListener {

            binding.cartContainer.setBackgroundResource(R.drawable.button_click_background)
            binding.cartContainer.postDelayed({ binding.cartContainer.background=null },100)
            if (binding.cartContainer.background!=null) {
                requireActivity().findViewById<ViewPager2>(R.id.viewPager2).currentItem = 2
            }
            else{

            }
        }
        binding.btnNotification.setOnClickListener {


            binding.btnNotification.setBackgroundResource(R.drawable.button_click_background)
            binding.btnNotification.postDelayed({ binding.btnNotification.background=null },50)
            startActivity(Intent(requireContext(), ExploreActivity::class.java))
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

        adapterItem = AdapterItem(requireContext(), itemList,this@HomeFragment)


        // ==========================================
        // RECYCLER VIEW
        // ==========================================

        var recyclerView = binding.itemShowHomeRecyclerView


        recyclerView.adapter = adapterItem

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)


        recyclerView.setHasFixedSize(true)

        recyclerView.setItemViewCacheSize(20)

    }


    // ==========================================
    // DESTROY VIEW
    // ==========================================

    override fun onDestroyView() {

        stopAutoSlider()

        super.onDestroyView()
    }

    override fun onButtonClick(imageView: ImageView) {

        val iconCart =
            requireActivity().findViewById<ImageView>(R.id.favBT)

        animateAddToCart(
            productImage = imageView,
            cartIcon = iconCart
        )
    }

    fun animateAddToCart(
        productImage: ImageView,
        cartIcon: ImageView
    ) {
        val root =
            requireActivity().findViewById<FrameLayout>(R.id.frameLayout)

        val startLocation = IntArray(2)
        val endLocation = IntArray(2)
        val rootLocation = IntArray(2)

        productImage.getLocationOnScreen(startLocation)
        cartIcon.getLocationOnScreen(endLocation)
        root.getLocationOnScreen(rootLocation)

        val size = productImage.width

        val flyingImage = ImageView(requireContext()).apply {
            setImageDrawable(productImage.drawable)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        val startX = startLocation[0] - rootLocation[0]
        val startY = startLocation[1] - rootLocation[1]

        val targetX =
            endLocation[0] +
                    cartIcon.width / 2 -
                    size / 2 -
                    rootLocation[0]

        val targetY =
            endLocation[1] +
                    cartIcon.height / 2 -
                    size / 2 -
                    rootLocation[1]

        val params = FrameLayout.LayoutParams(size, size).apply {
            leftMargin = startX
            topMargin = startY
        }

        root.addView(flyingImage, params)

        flyingImage.animate()
            .translationX((targetX - startX).toFloat())
            .translationY((targetY - startY).toFloat())
            .scaleX(0.1f)
            .scaleY(0.1f)
            .setDuration(300)
            .setInterpolator(
                AccelerateDecelerateInterpolator()
            )
            .withEndAction {

                root.removeView(flyingImage)

                cartIcon.animate()
                    .scaleX(1.25f)
                    .scaleY(1.25f)
                    .setDuration(120)
                    .withEndAction {

                        cartIcon.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(120)
                            .start()
                    }
                    .start()
            }
            .start()
    }
}
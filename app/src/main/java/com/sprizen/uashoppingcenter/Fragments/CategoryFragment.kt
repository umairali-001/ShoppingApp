package com.sprizen.uashoppingcenter.Fragments
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.database.database
import com.sprizen.uashoppingcenter.DATA_CLASS.PRODUCT
import com.sprizen.uashoppingcenter.DATA_CLASS.PHOTO
import com.sprizen.uashoppingcenter.DataBase
import com.sprizen.uashoppingcenter.R
import com.sprizen.uashoppingcenter.databinding.FragmentCategoryBinding


class CategoryFragment : Fragment() {

    private lateinit var binding: FragmentCategoryBinding
    lateinit var imageViewsList: MutableList<ImageView>
    lateinit var categoryTextViewList : MutableList<TextView>
    lateinit var ImagesUrlList : MutableList<PHOTO>
    lateinit var dataBase: DataBase
    lateinit var sharedPreferences: SharedPreferences
    lateinit var editor : SharedPreferences.Editor


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, ): View {

        binding = FragmentCategoryBinding.inflate(layoutInflater)

        initializeEveryThing()

        return binding.root
    }



    private fun initializeEveryThing() {

        ImagesUrlList = ArrayList()

        sharedPreferences = requireContext().getSharedPreferences("isFirstRun", Context.MODE_PRIVATE)
        val isFirstRun = sharedPreferences.getBoolean("isFirstRun", true)
        if (isFirstRun) {
            addImagesToDataBase()
            editor = sharedPreferences.edit()
            editor.putBoolean("isFirstRun", false)
            editor.apply()
        }



        registerAllimageViews()
        selectedCategory()

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



    }


    fun selectedCategory(){

        setImages("Fashion")

        binding.categoryFashionBtn.setOnClickListener {
            var animation = AnimationUtils.loadAnimation(requireContext(),R.anim
                .button_animation)
            binding.categoryFashionBtn.animation = animation
            animation.start()

            cleaAllBg()
            binding.categoryFashionBtn.setBackgroundResource(R.drawable.bg_category)
            setImages("Fashion")
        }
        binding.categoryElectronicBtn.setOnClickListener {
            var animation = AnimationUtils.loadAnimation(requireContext(),R.anim
                .button_animation)
            binding.categoryElectronicBtn.animation = animation
            animation.start()
            cleaAllBg()

            binding.categoryElectronicBtn.setBackgroundResource(R.drawable.bg_category)
            setImages("Electronic")
        }
        binding.categoryHomeBtn.setOnClickListener {
            var animation = AnimationUtils.loadAnimation(requireContext(),R.anim
                .button_animation)
            binding.categoryHomeBtn.animation = animation
            animation.start()
            cleaAllBg()

            binding.categoryHomeBtn.setBackgroundResource(R.drawable.bg_category)
            setImages("Home")
        }
        binding.categoryBeautyBtn.setOnClickListener {
            var animation = AnimationUtils.loadAnimation(requireContext(),R.anim
                .button_animation)
            binding.categoryBeautyBtn.animation = animation
            animation.start()
            cleaAllBg()

            binding.categoryBeautyBtn.setBackgroundResource(R.drawable.bg_category)
            setImages("Beauty")
        }
        binding.categorySportBtn.setOnClickListener {
            var animation = AnimationUtils.loadAnimation(requireContext(),R.anim
                .button_animation)
            binding.categorySportBtn.animation = animation
            animation.start()
            cleaAllBg()

            binding.categorySportBtn.setBackgroundResource(R.drawable.bg_category)
            setImages("Sport")
        }
        binding.categoryBooksBtn.setOnClickListener {
            var animation = AnimationUtils.loadAnimation(requireContext(),R.anim
                .button_animation)
            binding.categoryBooksBtn.animation = animation
            animation.start()

            cleaAllBg()
            binding.categoryBooksBtn.setBackgroundResource(R.drawable.bg_category)
            setImages("Books")
        }
        binding.categoryToysBtn.setOnClickListener {
            var animation = AnimationUtils.loadAnimation(requireContext(),R.anim
                .button_animation)
            binding.categoryToysBtn.animation = animation
            animation.start()

            cleaAllBg()
            binding.categoryToysBtn.setBackgroundResource(R.drawable.bg_category)
            setImages("Toys")
        }
        binding.categoryAutomotiveBtn.setOnClickListener {
            var animation = AnimationUtils.loadAnimation(requireContext(),R.anim
                .button_animation)
            binding.categoryAutomotiveBtn.animation = animation
            animation.start()

            cleaAllBg()
            binding.categoryAutomotiveBtn.setBackgroundResource(R.drawable.bg_category)
            setImages("Automotive")
        }

        binding.categoryPetFoodBtn.setOnClickListener {
            var animation = AnimationUtils.loadAnimation(requireContext(),R.anim
                .button_animation)
            binding.categoryPetFoodBtn.animation = animation
            animation.start()

            cleaAllBg()
            binding.categoryPetFoodBtn.setBackgroundResource(R.drawable.bg_category)
            setImages("PetFood")
        }


    }
    fun cleaAllBg(){

        binding.categoryFashionBtn.background = null
        binding.categoryHomeBtn.background = null
        binding.categoryBeautyBtn.background = null
        binding.categoryToysBtn.background = null
        binding.categoryBooksBtn.background = null
        binding.categoryElectronicBtn.background = null
        binding.categorySportBtn.background = null
        binding.categoryPetFoodBtn.background = null
        binding.categoryAutomotiveBtn.background = null
    }
    fun registerAllimageViews(){

        categoryTextViewList = ArrayList()
        imageViewsList = ArrayList()
        imageViewsList.add(binding.trendingImage)
        imageViewsList.add(binding.mensClothing)
        imageViewsList.add(binding.womenClothing)
        imageViewsList.add(binding.kidsClothing)
        imageViewsList.add(binding.footwear)
        imageViewsList.add(binding.watches)
        imageViewsList.add(binding.jewelry)

        categoryTextViewList.add(binding.firstTV)
        categoryTextViewList.add(binding.secondTV)
        categoryTextViewList.add(binding.thirdTV)
        categoryTextViewList.add(binding.fourthTV)
        categoryTextViewList.add(binding.fifthTV)
        categoryTextViewList.add(binding.sixthTV)



    }


    fun setImages(category : String){
        dataBase = DataBase(requireContext())
        ImagesUrlList = dataBase.getData(category)

        if (ImagesUrlList.size>0) {

            for (i in 0..6) {
                val imageUrl =
                    ImagesUrlList[i].photoLink

                Glide.with(this)
                    .load(imageUrl)
                    .into(imageViewsList[i])




            }
            for (i in 0..5){
                categoryTextViewList[i].text= ImagesUrlList[i+1].photoName
            }

        }
        else{
            Toast.makeText(requireContext(), "There is no SubCategory", Toast.LENGTH_SHORT).show()
        }



    }
    fun addImagesToDataBase(){


        //For Fashion Category
        dataBase = DataBase(requireContext())
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783333080/Fashion_Category_x5di4n.jpg","Fashion","Fashion")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783321765/Men_s_Cloths_o8plaj.jpg","Mens Cloth", "Fashion")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783321767/Women_s_Cloths_b3x96z.jpg","Women Cloth", "Fashion")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783321762/Kid_s_Clothes_mcjll5.jpg","Kids Cloth", "Fashion")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783321761/Footwear_awiwsb.jpg","Footwear", "Fashion")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783321766/Witches_and_Accessories_qgjtti.jpg","Watches", "Fashion")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783321763/jewelry_wznj12.jpg","Jewelry", "Fashion")


        //For Electronic Category
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783333081/Electronic_Category_aq7krw.jpg","Mobile","Electronic")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783321764/Mobile_Accessories_fjhmnp.jpg","Mobile","Electronic")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783321762/Laptops_and_Computers_kirhyw.jpg","Laptop","Electronic")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783321761/Headphones_atqsqa.jpg","Headphones","Electronic")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783321762/Camera_aznvnk.jpg","Camera","Electronic")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783321764/Smarts_Wearabele_hmt90g.jpg","Smart Wachtes","Electronic")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783321763/Home_Appliances_oqpry0.jpg","Home Appliances","Electronic")


        //For Home Category
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783333081/Home_Category_ncz1ke.jpg","Home Category", "Home")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783328662/Furniture_wydvpc.jpg","Furniture", "Home")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783328663/Home_Decor_nk2oe1.jpg","Home Decor", "Home")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783328663/Kitchen_Products_qnztgu.jpg","Kitchen", "Home")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783328663/Budding_and_Towels_phhsdb.jpg","Budding and Towels", "Home")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783328663/Lightning_c0vd5j.jpg","Lightnings", "Home")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783328663/Home_Improvements_vizhy4.jpg","Tool's Improvements", "Home")

        //For Beauty Category
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783332185/Beauty_Category_ejio14.jpg","Beauty Category", "Beauty")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783332184/Makeup_vehpnk.jpg","Makeup", "Beauty")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783332184/Skin_Care_zgckmz.jpg","Skincare", "Beauty")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783332183/Haircare_k2pain.jpg","Haircare", "Beauty")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783332185/Perfumes_ckazlf.jpg","Perfumes", "Beauty")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783332185/Personal_Care_bb4iml.jpg","Personal Care", "Beauty")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783332183/Beauty_Cream_s_qhoggb.jpg","Beauty Cream's", "Beauty")

        //For Sports Category
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783334566/Sports_Category_f22ekq.jpg","Sports Category", "Sport")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783334564/Fitness_Equipment_zqxunc.jpg","Fitnes Equipment", "Sport")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783334565/Sport_s_Clothing_rivrcq.jpg","Sports Clothing", "Sport")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783334565/Graphics_Shoe_s_yqgfgw.jpg","Graphic Shoe's", "Sport")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783334565/Outdoor_Camping_kyxwf9.jpg","Outdoor Camping", "Sport")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783334564/Cycling_hz8h4x.jpg","Cycling", "Sport")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783334565/Indoor_Game_s_fxswbj.jpg","Indoor Game's", "Sport")

        //For Books Category
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783336008/Books_Category_f71p8c.jpg","Books Category", "Books")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783336006/Literature_cj26wx.jpg","Literature", "Books")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783336006/Non_Fiction_xgx0tj.jpg","Non Fiction", "Books")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783336003/Academic_Textbook_qdeuvz.jpg","Academic Textbook's", "Books")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783336004/Kid_s_Books_bdgcue.jpg","Kids Books", "Books")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783336005/Self-Improves_yrx6jn.jpg","Self Improvement", "Books")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783336004/Biographies_pebmvf.jpg","Biographies", "Books")

        //For Toys Category
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783337299/Toys_Category_qyljdr.jpg","Toys Category", "Toys")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783337299/Action_Figures_ehiilg.jpg","Action Figures", "Toys")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783337299/Puzzle_Board_pljfvy.jpg","Puzzle Board", "Toys")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783337299/Educational_Toys_uj4aem.jpg","Education Toys", "Toys")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783337305/Remote_Toys_tvq9k6.jpg","Remote Toys", "Toys")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783337304/Soft_Toys_tzywl9.jpg","Soft Toys", "Toys")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783337302/Reassemble_Toys_jaxh7r.jpg","Reassemble Toys", "Toys")

        //For Automotive Category
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783343681/Automotives_Category_a3jymt.jpg","Automotive Category", "Automotive")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783343679/Car_s_Accessories_x8b82l.jpg","Car's Accessories", "Automotive")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783343679/Bike_Accessories_kqmzlt.jpg","Bike Accessories", "Automotive")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783343678/Car_Cleaning_cqdxzq.jpg","Car Cleanings", "Automotive")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783343679/Ridding_and_Helmet_fdxl0q.jpg","Riding and Helmet", "Automotive")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783343678/Spare_Part_s_r1qqbw.jpg","Spare Parts", "Automotive")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783334564/Cycling_hz8h4x.jpg","Cycling", "Automotive")

        //For PetFood
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783344883/Petfood_Category_e24fxo.jpg","PetFood Category", "PetFood")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783344879/Dog_Food_j3kvr2.jpg","Dog Food","PetFood")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783344877/Cats_Food_gzmj0v.jpg","Cat Food","PetFood")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783344877/Birds_Food_m1w7co.jpg","Birds Food","PetFood")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783344879/Fish_Food_dif6ot.jpg","Fish Food","PetFood")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1783344880/Pet_Treatments_oczeh9.jpg","Pets Treatment","PetFood")
        dataBase.insert_information("https://res.cloudinary.com/q3pn4aap/image/upload/v1782994289/Make_cove_image_for_app_202607011954_ycru7b.jpg","More Product's","PetFood")

    }

}

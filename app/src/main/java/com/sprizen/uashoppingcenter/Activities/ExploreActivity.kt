package com.sprizen.uashoppingcenter.Activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.sprizen.uashoppingcenter.R
import com.sprizen.uashoppingcenter.databinding.ActivityExploreBinding
import okhttp3.*

class ExploreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExploreBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityExploreBinding.inflate(layoutInflater)
        setContentView(binding.root)


        initializeEveryThing()

    }

    fun initializeEveryThing() {

        binding.backIcon.setOnClickListener {

            binding.backIcon.setBackgroundResource(R.drawable.button_click_background)
            binding.backIcon.postDelayed({ binding.backIcon.background = null }, 50)
            finish()
        }
    }


}
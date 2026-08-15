package com.sprizen.uashoppingcenter.Activities

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.sprizen.uashoppingcenter.R
import com.sprizen.uashoppingcenter.databinding.ActivityExploreBinding
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ExploreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExploreBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityExploreBinding.inflate(layoutInflater)
        setContentView(binding.root)


        initializeEveryThing()

    }
    fun initializeEveryThing(){

        binding.backIcon.setOnClickListener {

            val animation = AnimationUtils.loadAnimation(
                this,
                R.anim.button_animation
            )

            // کلک ہوتے ہی Background لگ جائے
            binding.backIcon.setBackgroundResource(R.drawable.button_click_background)

            animation.setAnimationListener(object : Animation.AnimationListener {

                override fun onAnimationStart(animation: Animation?) {}

                override fun onAnimationEnd(animation: Animation?) {

                    // Animation ختم ہوتے ہی Background ہٹا دیں
                    binding.backIcon.background = null

                    finish()
                }

                override fun onAnimationRepeat(animation: Animation?) {}
            })

            binding.backIcon.startAnimation(animation)

        }




    }


}
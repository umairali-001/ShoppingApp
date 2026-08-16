package com.sprizen.uashoppingcenter.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.google.firebase.auth.FirebaseAuth
import com.sprizen.uashoppingcenter.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignUpBinding

    var firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        window.statusBarColor = ContextCompat.getColor(this, android.R.color.white)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true


        initializeEverything()

    }


    //=======================================================================================================================
    fun initializeEverything() {

        //Check User

        if (firebaseAuth.currentUser != null){
            startActivity(Intent(this@SignUpActivity, HomeActivity::class.java))
            finish()
        }

        binding.logBT.setOnClickListener {
            finish()
            startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
        }
        binding.SignBT.setOnClickListener {
            signpage()
        }

    }



    fun signpage(){
        val email=binding.emailET.text.toString()
        val password=binding.passwordET.text.toString()
        val confirm_password=binding.CpasswordET.text.toString()

        if (email.isEmpty()||password.isEmpty()||confirm_password.isEmpty()){
            Toast.makeText(this@SignUpActivity,"Error please enter all detail",Toast.LENGTH_SHORT).show()
            return
        }
        if (password  != confirm_password){
            Toast.makeText(this@SignUpActivity,"Error incorrect password",Toast.LENGTH_SHORT).show()
            return
        }

        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this) {
            if (it.isSuccessful){
                startActivity(Intent(this@SignUpActivity, HomeActivity::class.java))
                finish()
            }else{
                Toast.makeText(this@SignUpActivity,it.exception?.message,Toast.LENGTH_SHORT).show()
            }

        }
    }

}
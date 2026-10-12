package com.sprizen.uashoppingcenter.Activities

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.google.firebase.auth.FirebaseAuth
import com.sprizen.uashoppingcenter.R
import com.sprizen.uashoppingcenter.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding

    var firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       binding= ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        window.statusBarColor = ContextCompat.getColor(this, android.R.color.white)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true


        initializeEverything()
    }

    //============================================================================================================================

    fun initializeEverything() {

    // Check User
        if (firebaseAuth.currentUser != null){
            finish()
            startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
        }

        binding.regBT.setOnClickListener {
            finish()
            startActivity(Intent(this@LoginActivity, SignUpActivity::class.java))
        }
        binding.loginBT.setOnClickListener { loginpage() }

        binding.showPasswordEye.setOnClickListener {
            show_password()
        }
    }

    private fun loginpage(){
        val email=binding.emailET.text.toString()
        val password=binding.passwordET.text.toString()

        if (email.isEmpty()||password.isEmpty()){
            Toast.makeText(this@LoginActivity,"Error please enter all detail",Toast.LENGTH_SHORT).show()
            return
        }

        firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this) {
            if (it.isSuccessful){
                startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                finish()
            }else{
                Toast.makeText(this@LoginActivity,it.exception?.message,Toast.LENGTH_SHORT).show()
            }

        }
    }
    fun show_password(){
        if (binding.passwordET.inputType ==
            (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        ) {

            // Password SHOW
            binding.passwordET.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

            // Eye Open Icon
            binding.showPasswordEye.setImageResource(R.drawable.ic_password_hide)

        } else {

            // Password HIDE
            binding.passwordET.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            // Eye Closed Icon
            binding.showPasswordEye.setImageResource(R.drawable.ic_password_show)

        }
    }

}
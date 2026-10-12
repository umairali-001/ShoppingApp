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
import com.sprizen.uashoppingcenter.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignUpBinding

    var firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()


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
        binding.passwordET.setOnClickListener {
            show_password()
        }
        binding.conformShowPasswordEye.setOnClickListener {
            show_conform_password()
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
    fun show_conform_password(){
        if (binding.CpasswordET.inputType ==
            (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        ) {

            // Password SHOW
            binding.CpasswordET.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

            // Eye Open Icon
            binding.showPasswordEye.setImageResource(R.drawable.ic_password_hide)

        } else {

            // Password HIDE
            binding.CpasswordET.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            // Eye Closed Icon
            binding.showPasswordEye.setImageResource(R.drawable.ic_password_show)

        }
    }


}
package com.c.firbaseotpkotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.c.firbaseotpkotlin.databinding.ActivityMainBinding
import com.c.firbaseotpkotlin.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()


        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }

    }

    private fun checkUser() {
        var firebaseUser = firebaseAuth.currentUser
        if(firebaseUser==null){
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }else{
            val phone=firebaseUser.phoneNumber
            binding.phoneTv.text=phone
        }
    }
}
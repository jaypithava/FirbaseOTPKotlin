package com.c.firbaseotpkotlin

import android.app.ProgressDialog
import android.content.Intent
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.c.firbaseotpkotlin.databinding.ActivityMainBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var forceResendingToken:PhoneAuthProvider.ForceResendingToken?=null

    private var mCallBacks:PhoneAuthProvider.OnVerificationStateChangedCallbacks?=null
    private var mVerificationId:String?=null
    private lateinit var firebaseAuth: FirebaseAuth

    private val TAG="MAIN_TAG"

    private lateinit var progressProgressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val animationFadeIn = AnimationUtils.loadAnimation(this@MainActivity, R.anim.bounce)
        binding.phoneLl.startAnimation(animationFadeIn)
        binding.iconIv.startAnimation(animationFadeIn)
        
        binding.phoneLl.visibility= View.VISIBLE
        binding.codeLl.visibility= View.GONE

        firebaseAuth= FirebaseAuth.getInstance()

        progressProgressDialog= ProgressDialog(this)
        progressProgressDialog.setTitle("Please Wait")
        progressProgressDialog.setCanceledOnTouchOutside(false)

        mCallBacks=object :PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                progressProgressDialog.dismiss()
                Log.d(TAG, "onVerificationFailed: ${e.message}")
                Toast.makeText(this@MainActivity, "${e.message}", Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(VerificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                Log.d(TAG, "onCodeSent: $VerificationId")
                mVerificationId=VerificationId
                forceResendingToken=token
                progressProgressDialog.dismiss()

                Log.d(TAG, "onCodeSent: $VerificationId")
                val animationFadeIn = AnimationUtils.loadAnimation(this@MainActivity, R.anim.slide_down)
                binding.codeLl.startAnimation(animationFadeIn)
                binding.phoneLl.visibility=View.GONE
                binding.codeLl.visibility=View.VISIBLE
                Toast.makeText(this@MainActivity, "Verification Code Sent...", Toast.LENGTH_SHORT).show()
                binding.codeSentDescriptionTv.text="Please type the verification code we send to ${"+91"+binding.phoneEt.text.toString().trim()}"
            }
        }

        binding.phoneContinueBtn.setOnClickListener {
            val phone= "+91"+binding.phoneEt.text.toString().trim()
            if(TextUtils.isEmpty(phone)){
                Toast.makeText(this@MainActivity, "Please enter phone number", Toast.LENGTH_SHORT).show()
            }else{
                startVerificationPhoneNumber(phone)
            }
        }
        binding.resendCodeTv.setOnClickListener {
            val phone= "+91"+binding.phoneEt.text.toString().trim()
            if(TextUtils.isEmpty(phone)){
                Toast.makeText(this@MainActivity, "Please enter phone number", Toast.LENGTH_SHORT).show()
            }else{
                resendVerificationCode(phone, forceResendingToken!!)
            }
        }
        binding.codeSubmitBtn.setOnClickListener {
            val code=binding.phoneEt.text.toString().trim()
            if(TextUtils.isEmpty(code)){
                Toast.makeText(this@MainActivity, "Please enter verification code", Toast.LENGTH_SHORT).show()
            }else{
                verifyPhoneNumberWithCode(mVerificationId!!,code)
            }
        }
    }
    private fun startVerificationPhoneNumber(phone:String){
        Log.d(TAG, "startVerificationPhoneNumber: $phone")
        progressProgressDialog.setMessage("Verifying Phone Number...")
        progressProgressDialog.show()

        val option=PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phone)
            .setTimeout(60L,TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallBacks!!)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(option)
    }

    private fun resendVerificationCode(phone: String,token:PhoneAuthProvider.ForceResendingToken){
        progressProgressDialog.setMessage("Resending Code...")
        progressProgressDialog.show()

        val option=PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phone)
            .setTimeout(60L,TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallBacks!!)
            .setForceResendingToken(token)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(option)
    }

    private fun verifyPhoneNumberWithCode(verificationId:String?,code:String){
        Log.d(TAG, "verifyPhoneNumberWithCode: $verificationId $code")
        progressProgressDialog.setMessage("Verifying Code...")
        progressProgressDialog.show()

        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        progressProgressDialog.setMessage("Logging In...")
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener {
                progressProgressDialog.dismiss()
                val phone=firebaseAuth.currentUser?.phoneNumber
                Toast.makeText(this, "Logged In As $phone", Toast.LENGTH_SHORT).show()

                startActivity(Intent(this,ProfileActivity::class.java))
                finish()
            }
            .addOnFailureListener{e->
                progressProgressDialog.dismiss()
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}
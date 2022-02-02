package com.c.firbaseotpkotlin

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.c.firbaseotpkotlin.databinding.ActivityMainBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var forceResendingToken:PhoneAuthProvider.ForceResendingToken?=null

    private var mCallBacks:PhoneAuthProvider.OnVerificationStateChangedCallbacks?=null
    private var mVerificationId:String?=null
    private lateinit var firebaseAuth: FirebaseAuth

    private val TAG="MAIN_TAG"

    private lateinit var progressDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.phoneLl.visibility= View.VISIBLE
        binding.codeLl.visibility= View.GONE

        firebaseAuth= FirebaseAuth.getInstance()

        progressDialog= Dialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        mCallBacks=object :PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                signInWithPhoneAuthCredentials(phoneAuthCredential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                progressDialog.dismiss()
                Log.d(TAG, "onVerificationFailed: ${e.message}")
                Toast.makeText(this@MainActivity, "${e.message}", Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(VerificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                Log.d(TAG, "onCodeSent: $VerificationId")
                mVerificationId=VerificationId
                forceResendingToken=token
                progressDialog.dismiss()
                
                Log.d(TAG, "onCodeSent: $VerificationId")

                binding.phoneLl.visibility=View.GONE
                binding.codeLl.visibility=View.VISIBLE
                Toast.makeText(this@MainActivity, "Verification Code Sent...", Toast.LENGTH_SHORT).show()
                binding.codeSentDescriptionTv.text="Please type the verification code we send to ${binding.phoneEt.text.toString().trim()}"
            }
        }

        binding.phoneContinueBtn.setOnClickListener {
            val phone=binding.phoneEt.text.toString().trim()
            if(TextUtils.isEmpty(phone)){
                Toast.makeText(this@MainActivity, "Please enter phone number", Toast.LENGTH_SHORT).show()
            }else{
                startVerificationPhoneNumber(phone)
            }
        }
        binding.resendCodeTv.setOnClickListener {
            val phone=binding.phoneEt.text.toString().trim()
            if(TextUtils.isEmpty(phone)){
                Toast.makeText(this@MainActivity, "Please enter phone number", Toast.LENGTH_SHORT).show()
            }else{
                resendVerificationCode(phone, forceResendingToken!!)
            }
        }
        binding.phoneContinueBtn.setOnClickListener {
            val code=binding.phoneEt.text.toString().trim()
            if(TextUtils.isEmpty(code)){
                Toast.makeText(this@MainActivity, "Please enter verification code", Toast.LENGTH_SHORT).show()
            }else{
                verifyPhoneNumberWithCode(mVerificationId,code)
            }
        }
    }
    private fun startVerificationPhoneNumber(phone:String){
        progressDialog.setTitle("Verifying Phone Number...")
        progressDialog.show()

        val option=PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phone)
            .setTimeout(60L,TimeUnit.SECONDS)
            .setCallbacks(mCallBacks!!)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(option)
    }

    private fun resendVerificationCode(phone: String,token:PhoneAuthProvider.ForceResendingToken){
        progressDialog.setTitle("Resending Code...")
        progressDialog.show()

        val option=PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phone)
            .setTimeout(60L,TimeUnit.SECONDS)
            .setCallbacks(mCallBacks!!)
            .setForceResendingToken(token)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(option)
    }

    private fun verifyPhoneNumberWithCode(verificationId:String?,code:String){
        progressDialog.setTitle("Verifying Code...")
        progressDialog.show()

        val credential=PhoneAuthProvider.getCredential(verificationId!!,code)
        signInWithPhoneAuthCredentials(credential)
    }

    private fun signInWithPhoneAuthCredentials(credential: PhoneAuthCredential) {
        progressDialog.setTitle("Logging In...")
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener {
                progressDialog.dismiss()
                val phone=firebaseAuth.currentUser?.phoneNumber
                Toast.makeText(this, "Logged In As $phone", Toast.LENGTH_SHORT).show()

                startActivity(Intent(this,ProfileActivity::class.java))
                finish()
            }
            .addOnFailureListener{e->
                Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}
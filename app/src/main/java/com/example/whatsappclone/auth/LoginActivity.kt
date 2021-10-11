package com.example.whatsappclone.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import com.example.whatsappclone.R
import com.example.whatsappclone.databinding.ActivityLoginBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var phoneNumber: String
    private lateinit var countryCode: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_login)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        binding.phnoEt.addTextChangedListener {
            binding.nextBtn.isEnabled = !(binding.nextBtn.text.isNullOrEmpty() || it!!.length < 10)
        }

        binding.nextBtn.setOnClickListener{
            checkNumber()
        }
    }

    private fun checkNumber() {
        countryCode = binding.ccp.selectedCountryCodeWithPlus
        phoneNumber = countryCode + binding.phnoEt.text.toString()

        //Create a dialogue box to show the user
        notifyUser()
    }

    private fun notifyUser() {
        MaterialAlertDialogBuilder(this).apply {
            setMessage("We will be verifying the phone number: $phoneNumber\n"+
                        "Is this OK, or would you like to edit the number?")

            setPositiveButton("Ok") { _,_ ->
                showOtpActivity()
            }
            setNegativeButton("Edit") {dialog, which ->
                dialog.dismiss()
            }
            setCancelable(false)
            create()
            show()
        }
    }

    //Function to direct to OTP activity
    private fun showOtpActivity() {
        Log.e("Check", "Starting OTP activity")
        startActivity(Intent(this, OTPActivity::class.java).putExtra(PHONE_NUMBER, phoneNumber))
        finish()
    }
}
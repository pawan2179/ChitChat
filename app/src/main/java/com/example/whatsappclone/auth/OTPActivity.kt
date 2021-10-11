package com.example.whatsappclone.auth

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.example.whatsappclone.MainActivity
import com.example.whatsappclone.R
import com.example.whatsappclone.SignUpActivity
import com.example.whatsappclone.databinding.ActivityOtpactivityBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

const val PHONE_NUMBER = "phoneNumber"

class OTPActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding : ActivityOtpactivityBinding
    var phoneNumber : String? = null

    //Variables for OTP verification
    var mVerificationId: String? = null
    var mResentToken: PhoneAuthProvider.ForceResendingToken? = null
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var progressDialog : ProgressDialog
    private var mCounterDown: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_otpactivity)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_otpactivity)
        initViews()
        startVerify()

    }

    //Cancelling the timer when the activity is destroyed otherwise can cause crash
    override fun onDestroy() {
        super.onDestroy()
        if(mCounterDown != null)
            mCounterDown!!.cancel()
    }

    //Starting the verification process(Sending OTP usng firebase auth)
    private fun startVerify() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber!!, 60,
            TimeUnit.SECONDS,
            this,
            callbacks
        )
        showTimer(60000)
        progressDialog = createProgressDialog("Sending a verification code", false)
        progressDialog.show()
    }

    //Function to show time remaining before resending otp
    private fun showTimer(milliSecInFuture: Long) {
        binding.resendVerificationCodeBtn.isEnabled = false
        mCounterDown = object: CountDownTimer(milliSecInFuture, 1000) {

            override fun onTick(milliSecFinished: Long) {
                binding.counterTv.isVisible = true
                binding.counterTv.text = getString(R.string.seconds_remaining, milliSecFinished/1000)
            }

            override fun onFinish() {
                binding.counterTv.isEnabled = false
                binding.resendVerificationCodeBtn.isEnabled = true
            }
        }.start()
    }

    //Initialize important variables such as callbacks required to
    //start the verification process. Must be done before starting verification

    private fun initViews() {
        phoneNumber = intent.getStringExtra(PHONE_NUMBER)
        binding.verifyTv.text = getString(R.string.verifyString, phoneNumber)
        setSpannableString()

        binding.sendVerificationCodeBtn.setOnClickListener(this)
        binding.resendVerificationCodeBtn.setOnClickListener(this)

        //This callback is required for authentication
        callbacks = object: PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {

                if(progressDialog.isShowing)    progressDialog.dismiss()
                val smsCode = credential.smsCode
                if(!(smsCode.isNullOrBlank()))
                    binding.otpEt.setText(smsCode)

                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                if(e is FirebaseAuthInvalidCredentialsException) {

                }else if(e is FirebaseTooManyRequestsException) {

                }
            }

            override fun onCodeSent(verificationId : String,
                                    token: PhoneAuthProvider.ForceResendingToken) {

                if(progressDialog.isShowing)    progressDialog.dismiss()
                mVerificationId = verificationId
                mResentToken = token
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        val mAuth = FirebaseAuth.getInstance()
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    if(progressDialog.isShowing)    progressDialog.dismiss()
                    startActivity(Intent(this, SignUpActivity::class.java))
                    finish()
                }
                else {
                    notifyUserAndRetry("Your Phone Number verification failed. Try again!!")
                }
            }
    }

    //Creates Dialog which takes message and shows in dialog box
    private fun notifyUserAndRetry(message: String) {
        MaterialAlertDialogBuilder(this).apply {
            setMessage(message)
            setPositiveButton("Ok") {_,_ ->
                showLoginActivity()
            }
            setNegativeButton("Cancel") {dialog,_ ->
                dialog.dismiss()
            }
            setCancelable(false)
            create()
            show()
        }
    }

    private fun setSpannableString() {
        val span = SpannableString(getString(R.string.waiting_for_otp_text, phoneNumber))
        val clickableSpan = object : ClickableSpan(){
            override fun onClick(p0: View) {
                //Redirect to Login Activity
                showLoginActivity()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = ds.linkColor
            }
        }
        span.setSpan(clickableSpan, span.length-13, span.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.waitingForOTPtv.movementMethod = LinkMovementMethod.getInstance()
        binding.waitingForOTPtv.text = span
    }

    private fun showLoginActivity() {
        startActivity(Intent(this, LoginActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
    }

    //Disables back key on this activity
    override fun onBackPressed() {

    }

    override fun onClick(v: View?) {
        when(v) {
            binding.sendVerificationCodeBtn -> {

                val receivedCode = binding.otpEt.text.toString()
                if(receivedCode.isNotEmpty() and !(mVerificationId.isNullOrBlank())) {
                    progressDialog = createProgressDialog("Please wait..", false)
                    progressDialog.show()
                    val credential: PhoneAuthCredential =
                        PhoneAuthProvider.getCredential(mVerificationId!!, receivedCode)

                    signInWithPhoneAuthCredential(credential)
                }
            }

            binding.resendVerificationCodeBtn -> {

                val receivedCode = binding.otpEt.text.toString()
                if(mResentToken != null) {
                    showTimer(60000)
                    progressDialog = createProgressDialog("Sending a verification code", false)
                    progressDialog.show()

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                        phoneNumber!!,
                        60,
                        TimeUnit.SECONDS,
                        this,
                        callbacks,
                        mResentToken
                    )
                }
            }
        }
    }
}

//Dialog which can be used multiple times by passing different messages
fun Context.createProgressDialog(message: String, isCancelable: Boolean): ProgressDialog {
    return ProgressDialog(this).apply {
        setCancelable(false)
        setMessage(message)
        setCanceledOnTouchOutside(false)
    }
}
package com.example.whatsappclone

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.example.whatsappclone.databinding.ActivitySignUpBinding
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.util.jar.Manifest

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    private val storage by lazy {
        FirebaseStorage.getInstance()
    }

    private val auth by lazy {
        FirebaseAuth.getInstance()
    }

    private val database by lazy {
        FirebaseFirestore.getInstance()
    }

    lateinit var downloadUrl : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_sign_up)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)

        binding.userImgView.setOnClickListener{
            checkPermissionForImage()
        }
        binding.nextBtn.setOnClickListener {
            val name = binding.profileNameEt.text.toString()
            if (name.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            } else if (!::downloadUrl.isInitialized) {
                Toast.makeText(this, "Image cannot be empty", Toast.LENGTH_SHORT).show()
            } else {
                val user = User(name, downloadUrl, downloadUrl, auth.uid!!)
                database.collection("users").document(auth.uid!!)
                    .set(user).addOnSuccessListener {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }.addOnFailureListener {
                        binding.nextBtn.isEnabled = true
                        Toast.makeText(this, "Failed to create User", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    //Function to get read/write permission to get access to gallery for uploading images
    private fun checkPermissionForImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if((checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) &&
                    (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED))
            {
                val permissionRead = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                val permissionWrite = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

                requestPermissions(permissionRead, 1001)
                requestPermissions(permissionWrite, 1002)
            }
            else {
                pickImageFromGallery()
            }
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent,
        1000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == 1000) {
            data?.data.let {
                binding.userImgView.setImageURI(it)
                uploadImage(it)
            }
        }
    }

    private fun uploadImage(it: Uri?) {
        binding.nextBtn.isEnabled = false

        //scaling down image for decreasing size
        /*val fullSizeBitmap: Bitmap = MediaStore.Images.Media
            .getBitmap(this.contentResolver, it!!)

        val reductedBitmap = ImageResizer.reduceBitmapSize(fullSizeBitmap, 240000)*/

        val ref = storage.reference.child("uploads/" + auth.uid.toString())
        val uploadTask = ref.putFile(it!!)
        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if(!task.isSuccessful) {
                task.exception.let {
                    throw it!!
                }
            }
            return@Continuation ref.downloadUrl
        }).addOnCompleteListener { task ->
            binding.nextBtn.isEnabled = true
            if(task.isSuccessful) {
                downloadUrl = task.result.toString()
            }else {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Something went wrong. Check permissions", Toast.LENGTH_SHORT).show()
        }
    }
}


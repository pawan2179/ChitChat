package com.example.whatsappclone

import com.google.firebase.firestore.FieldValue

data class User(
    val name: String,
    val imgUrl: String,
    val thumbImage: String,
    val uid: String,
    val deviceToken: String,
    val status: String,
    val onlineStatus: String,

    ) {
    /* Whenever data class is created for Firebase, we need to make an empty constructor
      For code to work
     */
    constructor() : this("",
        "",
        "",
        "",
        "",
        "",
        ""
    )

    constructor(name : String,imgUrl : String, thumbImage : String, uid: String) : this(
        name,
        imgUrl,
        thumbImage,
        uid,
        "",
        "Hey there, I am using whatsapp",
        ""
    )
}
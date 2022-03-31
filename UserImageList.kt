package com.example.anew.models

import com.google.firebase.firestore.PropertyName


data class UserImageList (
    //define field name that this class will hold which is images
    @PropertyName("images") val images: List<String>? = null //firebase mandates that this kind of maping requires a default value of null

        )

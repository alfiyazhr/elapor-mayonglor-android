package com.example.elapormayonglor.data.model

import android.os.Parcelable
import com.google.firebase.firestore.PropertyName
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(

    @get:PropertyName("nik")
    @set:PropertyName("nik")
    var nik: String = "",

    @get:PropertyName("nama")
    @set:PropertyName("nama")
    var nama: String = "",

    @get:PropertyName("email")
    @set:PropertyName("email")
    var email: String = "",

    @get:PropertyName("alamat")
    @set:PropertyName("alamat")
    var alamat: String = "",

    @get:PropertyName("role")
    @set:PropertyName("role")
    var role: String = "warga",

    @get:PropertyName("uid")
    @set:PropertyName("uid")
    var uid: String = "",

    @get:PropertyName("created_at")
    @set:PropertyName("created_at")
    var createdAt: String = ""
) : Parcelable
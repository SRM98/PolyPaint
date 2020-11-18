package com.example.polypaintapp

import android.graphics.Bitmap

object User {

    var username: String = ""
    var password: String = ""
    var lastName: String = ""
    var firstName: String = ""
    var avatarByteArray: ByteArray? = null
    var avatarUrl: String? = null
    var avatarBitmap: Bitmap? = null
    var stats: Stats? = null
    var socketId: String = ""
}
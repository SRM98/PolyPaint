package com.example.polypaintapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.provider.MediaStore
import androidx.core.net.toFile
import java.io.ByteArrayOutputStream
import java.io.InputStream

class ImageConverter {

    fun toByteArray (bitmap: Bitmap): ByteArray {

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)

        return stream.toByteArray()
    }

    fun toBitmap (byteArray: ByteArray): Bitmap {

        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0 , byteArray.size)
        return bitmap
    }

//    fun toByteArray (uri: Uri): ByteArray{
////        val imageDecoder: ImageDecoder
////        imageDecoder.create
////        val inputStream: Bitmap = MediaStore.Images.Media.getBitmap()
//
//
//        return stream.toByteArray()
//    }
}
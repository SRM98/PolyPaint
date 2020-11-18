package com.example.polypaintapp

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.net.Uri
import android.os.Build
import android.provider.Settings.Global.getString
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.FragmentActivity
import com.mikepenz.iconics.Iconics.applicationContext

import okhttp3.internal.notify

//import kotlin.js

class Notification {

    private var mNotificationManager: NotificationManager? = null
    private var builder : NotificationCompat.Builder?  = null


    fun createNotification(context: Context, ChannelID : String, text : String, title : String) {
         builder = NotificationCompat.Builder(context, ChannelID)
            .setSmallIcon(R.drawable.other_message_bubble)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    }
    fun createNotificationChannel(channelID : String, context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "name"
            val descriptionText = "test"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification (notificationId : Int, context: Context) {
        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            notify(notificationId, builder!!.build())
        }
    }

    fun cancel(notificationId : Int, context: Context) {
        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            cancel(notificationId)
        }
    }

    fun cancelAll( context: Context) {
        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            cancelAll()
        }
    }

}
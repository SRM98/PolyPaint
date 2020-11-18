package com.example.polypaintapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import io.socket.client.Socket


class LostConnectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lost_connection)
        SocketUser.socket.socket.on(Socket.EVENT_RECONNECT) {
            SocketUser.socket.isConnected = true
            val intent = Intent(this as Context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra("EXIT", true)
            ContextCompat.startActivity(this as Context, intent, null)
        }
    }
}
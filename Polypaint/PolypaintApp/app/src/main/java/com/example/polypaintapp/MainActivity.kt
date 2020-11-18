package com.example.polypaintapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_chat.*
import okhttp3.*
import java.io.IOException
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.mikepenz.iconics.Iconics.applicationContext
import java.sql.Time
import java.time.Instant


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (!SocketUser.socket.isConnected) {
            SocketUser.socket.connect(this as Context)
            SocketUser.connectTime = System.currentTimeMillis()
        }
    }
}

enum class GameModes {
    Classic,
    Coop,
    Duel,
    Solo
}

object CurrentGameMode {
    var gameMode: Int = GameModes.Classic.ordinal
}

object PlayersInfo {
    var playersNames = ArrayList<String>()
    var url = ArrayList<String>()
}

object Channel {
    var matchChannel : String = ""
    var isAMatch = false
    var name: String = ""
}

object WaitingRoom {
    var name : String = ""
    var Game : Any = ""

}

object SocketUser {
    var gameModes = false
    var socket = Socket()
    var joinedChannels = ArrayList<String>()
    var matchChannels = ArrayList<String>()
    var notif = Notification()
    var messagesFromConnection = ArrayList<Message>()
    var connectTime: Long = 0

    fun onMessage(context: Context) {
        socket.socket.on("message") { data ->
            var json = JSON()
            val messageReturn = json.fromJson(data[0].toString())
            val tmp = messageReturn["senderAvatarUrl"]
            var senderUrl = ""
            if(tmp != null)
                senderUrl = messageReturn["senderAvatarUrl"] as String
            val message = Message(messageReturn["sender"] as String, messageReturn["text"] as String, messageReturn["date"] as Long, messageReturn["room"] as String, senderUrl)
            messagesFromConnection.add(message)
            println(message.text)
            println(message.room)
            println(message.sender)

            if(joinedChannels.contains(messageReturn["room"] as String) && User.username != messageReturn["sender"] as String) {
                notif.createNotification(context, messageReturn["room"] as String, messageReturn["text"] as String, messageReturn["room"] as String + " Channel")
                notif.createNotificationChannel(messageReturn["room"] as String, context)
                notif.showNotification(joinedChannels.indexOf(messageReturn["room"] as String), context)
            }
        }
    }
}





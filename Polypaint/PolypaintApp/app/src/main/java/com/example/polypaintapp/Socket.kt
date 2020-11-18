
package com.example.polypaintapp

import android.app.Application
import android.app.PendingIntent.getActivity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat.startActivity
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.socket.client.Socket
import io.socket.client.IO
import org.json.JSONException
import org.json.JSONObject
//import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import io.socket.emitter.Emitter


//import kotlin.js

class Socket {
    val socket = IO.socket(IP )
    var id : String = ""
    var isConnected: Boolean = false
    fun connect(context: Context) {
        socket.on("connection") { data ->
            this.id = data[0].toString()
        }
        socket.connect()
            .on(Socket.EVENT_CONNECTING) { println("connecting--------------------------------") }
            .on(Socket.EVENT_CONNECT) { println("connected---------------------------") }
            .on(Socket.EVENT_DISCONNECT) {
                println("disconnected---------------------------")
                isConnected = false
                Handler(Looper.getMainLooper()).postDelayed({
                    if(!isConnected) {
                        User.socketId = ""
                        val intent = Intent(context, LostConnectionActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        intent.putExtra("EXIT", true);
                        startActivity(context, intent, null)
                    }
                }, 8000)
            }
            .on(Socket.EVENT_CONNECT_ERROR) { data ->
                println(data[0])
            }
            .on(Socket.EVENT_ERROR) { println("ERROR-----------------------------------") }
            .on(Socket.EVENT_CONNECT_TIMEOUT) { println("TIMEOUT-----------------------------------") }
            .on(Socket.EVENT_RECONNECT) {
                println("RECONNECT---------------------------")
                isConnected = true
            }
    }

    fun disonnect() {
        socket.disconnect()

    }

    fun disconnectUser() {
        socket.emit("disconnectUser")
    }

    fun checkFirstTimeUser() {
        socket.emit("checkFirstTimeUserThinClient", User.username)
    }

    fun join(room : JsonObject) {
        socket.emit("join", room)
    }

    fun getImages(players : String) {
        socket.emit("getAvatarUrl", players)
    }


    fun send(message: JsonObject) {
        socket.emit("send", message)
    }

    fun createRoom(room: JsonObject) {
        socket.emit("create", room)
    }

    fun leaveRoom(room: JsonObject) {
        socket.emit("leave", room)
    }

    fun getRooms(room : String) {
        socket.emit("getAllRooms", room)
    }

    fun createMatch(match : JsonObject) {
        socket.emit("createMatch", match)
    }

    fun getOwnRooms() {
        socket.emit("getOwnRooms", User.username)
    }

    fun joinMatch(matchInfos : JsonObject) {
        socket.emit("joinMatch", matchInfos)

    }

    fun leaveInMatch(matchInfos : JsonObject) {
        socket.emit("leaveInMatch", matchInfos)

    }

    fun leaveMatch(matchInfos : JsonObject) {
        socket.emit("leaveMatch", matchInfos)

    }



}
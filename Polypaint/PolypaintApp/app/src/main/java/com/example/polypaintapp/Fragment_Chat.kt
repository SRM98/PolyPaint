package com.example.polypaintapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils.replace
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.mikepenz.fastadapter.dsl.genericFastAdapter
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.chat_row.view.*
import kotlinx.android.synthetic.main.chat_row_right.*
import kotlinx.android.synthetic.main.chat_row_right.view.*
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.fragment_profilview.*
import org.json.JSONArray
import java.lang.ClassCastException
import java.util.*

class FragmentChat : Fragment() {

    private val adapter = GroupAdapter<ViewHolder>()
    private val messagesHistory = ArrayList<Message>()
    private var gotIt = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_chat, container, false)
        v.isFocusableInTouchMode = true
        v.requestFocus()

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(Channel.name == WaitingRoom.name + CurrentGameMode.gameMode.toString() ) {
            if(CurrentGameMode.gameMode == GameModes.Solo.ordinal){
                Channel.name = WaitingRoom.name+GameModes.Solo.ordinal
            }
            chatName.text = Channel.name.subSequence(0,Channel.name.length-1)
            display_messages_button.isEnabled = false
        } else {
            chatName.text = Channel.name
            display_messages_button.isEnabled = true
        }

        context?.let {it ->
            SocketUser.notif.cancel(SocketUser.joinedChannels.indexOf(Channel.name), it)
        }

        myChannels.setOnClickListener {view ->
            view.findNavController().navigate(R.id.action_fragmentChat_to_fragmentChannel)

        }

        send_chat_button.setOnClickListener {

            sendMessage()
        }

        editText_chat.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                sendMessage()
                return@OnKeyListener true
            }
            false
        })



        display_messages_button.setOnClickListener {
            val json = JSON()
            SocketUser.socket.join(json.toJson(Room(User.username, Channel.name)))
            while(!gotIt) {}
            displayHistoryMessages()
            gotIt = false

        }

        recycleView_chat.adapter = adapter
        adapter.clear()
        onMessage()
        onBadWord()
        onHistoryMessages()
        getConnectionMessages()
    }

    override fun onDestroyView() {
        super.onDestroyView()

    }


    fun getConnectionMessages() {
        adapter.clear()
        if(!Channel.isAMatch) {
            recycleView_chat.adapter = adapter
            for (messages in SocketUser.messagesFromConnection) {
                if (messages.room == Channel.name) {
                    println(messages.text)
                    getMessage(messages)
                }
            }
        }
    }

    private fun addRightMessage(text: String, username: String, time: Long, bitmap: Bitmap) {

        adapter.add(ChatItemRight(text, username, time, bitmap))
        if (recycleView_chat != null){
            recycleView_chat.scrollToPosition(adapter.itemCount - 1)
        }
    }

    private fun addLeftMessage(text: String, username: String, time: Long, bitmap: Bitmap) {


        adapter.add(ChatItem(text, username, time, bitmap))
        if (recycleView_chat != null){
            recycleView_chat.scrollToPosition(adapter.itemCount - 1)
        }
    }

    private fun onMessage() {
        SocketUser.socket.socket.on("message") { data ->
            var json = JSON()
            val messageReturn = json.fromJson(data[0].toString())

            val tmp = messageReturn["senderAvatarUrl"]
            var senderUrl = ""
            if(tmp != null)
                senderUrl = messageReturn["senderAvatarUrl"] as String

            val messageText = Message(messageReturn["sender"] as String, messageReturn["text"] as String +" ",messageReturn["date"] as Long, messageReturn["room"] as String, senderUrl)
            context?.let {
                SocketUser.notif.cancel(SocketUser.joinedChannels.indexOf(Channel.name), it)
            }

            if(messageReturn["room"] as String == Channel.name) {
                activity?.runOnUiThread(java.lang.Runnable {
                    getMessage(messageText)
                })
            }
        }
    }

    fun displayHistoryMessages() {
        adapter.clear()
        recycleView_chat.adapter = adapter
        for (i in 0 until messagesHistory.size) {
            getMessage(messagesHistory[i])
        }
        recycleView_chat.scrollToPosition(adapter.getItemCount() - 1)
    }

    fun onHistoryMessages() {

        SocketUser.socket.socket.on("roomMessages") { data ->
            var json = JSON()
            var messages = JSONArray(data[0].toString())
            messagesHistory.clear()
            for (i in 0 until messages.length()) {
                val messageReturn = json.fromJson(messages[i].toString())
                val tmp = messageReturn["senderAvatarUrl"]
                var senderUrl = ""
                if(tmp != null)
                    senderUrl = messageReturn["senderAvatarUrl"] as String

                messagesHistory.add(Message(messageReturn["sender"] as String, messageReturn["text"] as String +" ",messageReturn["date"] as Long, messageReturn["room"] as String, senderUrl))
            }
            gotIt = true
        }
    }

    private fun onBadWord() {
        SocketUser.socket.socket.on("badWord") {
            activity?.runOnUiThread {
                Toast.makeText(activity, "Mauvais mot détecté, Attention !", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendMessage() {

        var message = Message(User.username,editText_chat.text.toString(), 4, Channel.name, "")
        val json = JSON()

        editText_chat.text.clear()

        SocketUser.socket.send(json.toJson(message))

        try{
            (activity as Game_Modes).hideKeyboard()
        } catch (e: ClassCastException) {
            (activity as ChatActivity).hideKeyboard()
        }

    }

    private fun getMessage(message: Message) {
//        val json = JSON()
//        val room = Room(User.username, Channel.name)
//        SocketUser.socket.join(json.toJson(room))

        if(message.sender == User.username) {
            if(message.avatarUrl == ""){
                val bitmapNoPhoto = BitmapFactory.decodeResource(this.resources, R.drawable.no_photo)
                addRightMessage(message.text, User.username, message.date, bitmapNoPhoto)
            }else{
                val uri = IP + "/" + message.avatarUrl
                Glide.with(this).asBitmap().load(uri.toUri())
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            addRightMessage(message.text, User.username, message.date, resource)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                        }
                    })
            }
        } else {
            if(message.avatarUrl == "") {
                val bitmapNoPhoto = BitmapFactory.decodeResource(this.resources, R.drawable.no_photo)
                addLeftMessage(message.text, message.sender, message.date, bitmapNoPhoto)
            } else {
                val uri = IP + "/" + message.avatarUrl
                Glide.with(this).asBitmap().load(uri.toUri())
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            addLeftMessage(message.text, message.sender, message.date, resource)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                        }
                    })
            }
        }
    }

    private fun containsAiPlayer(name : String): Boolean {
        if(name.contains("(Ai Player")){
            return true
        }
        return false
    }

    class ChatItem(var text: String, var username: String, var time: Long, var senderBitmap: Bitmap?): Item<ViewHolder>() {

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.username_message_left.text = username
            viewHolder.itemView.message_left.text = text
            var date = Date(time)
            var realDate = date.toString().subSequence(date.toString().length -18, date.toString().length-8)
            viewHolder.itemView.timeView_left.text = realDate

            if(senderBitmap != null)
                viewHolder.itemView.chat_image_sender.setImageBitmap(senderBitmap)
        }

        override fun getLayout(): Int {
            return R.layout.chat_row
        }
    }

    class ChatItemRight(val text: String, val username: String, var time: Long, var bitmap: Bitmap): Item<ViewHolder>() {

        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.username_message_right.text = username
            viewHolder.itemView.message_right.text = text
            var date = Date(time)
            var realDate = date.toString().subSequence(date.toString().length -18, date.toString().length-8)
            viewHolder.itemView.timeView_right.text = realDate

            if(bitmap != null)
                viewHolder.itemView.chat_image_user.setImageBitmap(bitmap)
        }

        override fun getLayout(): Int {
            return R.layout.chat_row_right
        }
    }
}
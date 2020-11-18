//package com.example.polypaintapp
//
//import android.annotation.TargetApi
//import android.app.Activity
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.content.Context
//import android.content.Intent
//import android.graphics.Color
//import android.media.RingtoneManager
//import android.os.Build
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.AdapterView
//import android.widget.ArrayAdapter
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.appcompat.widget.Toolbar
//import androidx.core.app.NotificationCompat
//import androidx.core.app.NotificationManagerCompat
//import androidx.core.content.ContextCompat.getSystemService
//import androidx.core.view.get
//import androidx.fragment.app.Fragment
//import androidx.navigation.findNavController
//import com.example.polypaintapp.Channel.name
//import com.mikepenz.iconics.Iconics.applicationContext
//import com.xwray.groupie.GroupAdapter
//import com.xwray.groupie.Item
//import com.xwray.groupie.ViewHolder
//import kotlinx.android.synthetic.main.activity_chat.*
//import kotlinx.android.synthetic.main.channel_row.view.*
//import kotlinx.android.synthetic.main.fragment_channel.*
//import kotlinx.android.synthetic.main.fragment_channel.recyclerView_channel
//import kotlinx.android.synthetic.main.fragment_chat.*
//import kotlinx.android.synthetic.main.fragment_my_channel.*
//import org.json.JSONArray
//import java.util.*
//import java.util.Arrays.asList
//import kotlin.collections.ArrayList
//
//
//class Fragment_My_Channels : Fragment() {
//
//    private val adapter = GroupAdapter<ViewHolder>()
//    private val channelNames = ArrayList<String>()
//    private val ownChannels = ArrayList<String>()
//
//
//
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        return inflater.inflate(R.layout.fragment_my_channel, container, false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        if(Channel.isAMatch){
//            backButton.visibility = View.INVISIBLE
//
//        }
//
//        backButton.setOnClickListener {view ->
//            view.findNavController().navigate(R.id.action_fragment_My_Channels_to_fragmentChannel)
//        }
//
//        getMyRooms()
//        onRoomDelete()
//        onChannelRooms()
//
//        adapter.clear()
//        recyclerView_channel.adapter = adapter
//
//        //context?.let { SocketUser.onMessage(it) }
//        SocketUser.socket.getOwnRooms()
//
//
//
//    }
//
//    fun getMyRooms() {
//
//        SocketUser.socket.socket.on("ownRooms") { data ->
//            SocketUser.joinedChannels.clear()
//            var ownChan = JSONArray(data[0].toString())
//            val json = JSON()
//            for (i in 0 until ownChan.length()) {
//                SocketUser.joinedChannels.add(ownChan[i].toString())
//                val room = Room(User.username, ownChan[i].toString())
//                SocketUser.socket.join(json.toJson(room))
//            }
//            activity?.runOnUiThread(java.lang.Runnable {
//                onChannelRooms()
//            })
//        }
//
//    }
//
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//
//    }
//
//
//    fun addChannel(name: String) {
//
//        adapter.add(MyChannelItem(name))
//
//    }
//
//
//    fun onRoomDelete() {
//        SocketUser.socket.socket.on("leaveRoom") {data ->
//            println(data[0].toString())
//            SocketUser.joinedChannels.remove(data[0].toString())
//            activity?.runOnUiThread(java.lang.Runnable {
//                onChannelRooms()
//            })
//
//
//        }
//
//    }
//
//    fun onChannelRooms() {
//        adapter.clear()
//        for(channel in SocketUser.joinedChannels.reversed()) {
//            addChannel(channel)
//        }
//        if(Channel.isAMatch == true){
//            addChannel(Channel.matchChannel)
//        }
//    }
//
//}
//
//class MyChannelItem(var name: String): Item<ViewHolder>() {
//
//    override fun bind(viewHolder: ViewHolder, position: Int) {
//        viewHolder.itemView.channel_name.text = name
//        if(Channel.matchChannel == name && Channel.isAMatch == true) {
//            viewHolder.itemView.channel_name.text = name.subSequence(0,Channel.matchChannel.length-1).toString()
//        }
//        if(SocketUser.joinedChannels.contains(name) || Channel.matchChannel == name) {
//            viewHolder.itemView.button_leave_channel.visibility = View.VISIBLE
//            viewHolder.itemView.button_join_channel.visibility = View.GONE
//            viewHolder.itemView.channel_name.isEnabled = true
//        }
//        else {
//            viewHolder.itemView.button_leave_channel.visibility = View.GONE
//            viewHolder.itemView.button_join_channel.visibility = View.VISIBLE
//            viewHolder.itemView.channel_name.isEnabled = false
//        }
//
//        viewHolder.itemView.button_leave_channel.setOnClickListener {
//            //SocketUser.joinedChannels.remove(name)
//            viewHolder.itemView.channel_name.isEnabled = false
//            viewHolder.itemView.button_leave_channel.visibility = View.GONE
//            viewHolder.itemView.button_join_channel.visibility = View.VISIBLE
//            val room = Room(User.username, name)
//            val json = JSON()
//            SocketUser.socket.leaveRoom(json.toJson(room))
//
//        }
//
//        viewHolder.itemView.button_join_channel.setOnClickListener {
//            //SocketUser.joinedChannels.add(name)
//            viewHolder.itemView.channel_name.isEnabled = true
//            viewHolder.itemView.button_leave_channel.visibility = View.VISIBLE
//            viewHolder.itemView.button_join_channel.visibility = View.GONE
//            val room = Room(User.username, name)
//            val json = JSON()
//            SocketUser.socket.join(json.toJson(room))
//        }
//
//        viewHolder.itemView.channel_name.setOnClickListener {Activity ->
//            Channel.name = name
//            Activity.findNavController().navigate(R.id.action_fragment_My_Channels_to_fragmentChat)
//
//        }
//
//    }
//
//    override fun getLayout(): Int {
//        return R.layout.channel_row
//    }
//}
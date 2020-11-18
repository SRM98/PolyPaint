package com.example.polypaintapp

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.polypaintapp.Channel.name
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.channel_row.view.*
import kotlinx.android.synthetic.main.fragment_channel.*
import org.json.JSONArray


class FragmentChannel : Fragment() {

    private val adapter = GroupAdapter<ViewHolder>()
    private val channelNames = ArrayList<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_channel, container, false)
        v.isFocusableInTouchMode = true
        v.requestFocus()
        v.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if(SocketUser.gameModes) {
                        println("test")
                        SocketUser.gameModes = false
                        val intent = Intent(context, Game_Modes::class.java)
                        startActivity(intent)
                    }
                }
            }
            false
        }
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getOwnRooms()
        SocketUser.socket.getOwnRooms()

        adapter.clear()
        recyclerView_channel.adapter = adapter

        onChannelRooms()
        SocketUser.socket.getRooms("")



        val adapterChannel = ArrayAdapter<String>(
            activity!!.applicationContext,
            android.R.layout.simple_list_item_1,
            channelNames
        )



        autoCompleteTextView2.setAdapter(adapterChannel)




        autoCompleteTextView2.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                val selected = parent.getItemAtPosition(position) as String
                autoCompleteTextView2.text.clear()
                val json = JSON()
                val room = Room(User.username, selected)
                SocketUser.joinedChannels.add(selected)
                SocketUser.socket.join(json.toJson(room))
                SocketUser.socket.getRooms("")
                Channel.name = selected
                Toast.makeText(context, "Channel $selected is joined", Toast.LENGTH_SHORT).show()
                activity?.findNavController(R.id.channel_view)?.navigate(R.id.action_fragmentChannel_to_fragmentChat)
            }

        addChannel.setOnClickListener {view ->
            var exists = false
            for (i in 0 until channelNames.size) {
                if(addRoomButton.text.toString() == channelNames[i]) {
                    exists = true


                }
            }
            if(addRoomButton.text!!.startsWith(" ")  || addRoomButton.text!!.isBlank()) {
                Toast.makeText(context, "You cannot add this", Toast.LENGTH_SHORT).show()
            }
            else if(exists) {
                Toast.makeText(context, "Channel already exist", Toast.LENGTH_SHORT).show()

            }
            else {
                val json = JSON()
                val room = Room(User.username, addRoomButton.text.toString() )
                SocketUser.socket.createRoom(json.toJson(room))
                name = addRoomButton.text.toString()
                addRoomButton.text!!.clear()
                view.findNavController()?.navigate(R.id.action_fragmentChannel_to_fragmentChat)
            }
        }

    }


    private fun addChannel(name: String) {

        adapter.add(ChannelItem(name))

    }

    private fun getOwnRooms() {

        SocketUser.socket.socket.on("ownRooms") { data ->
            SocketUser.joinedChannels.clear()
            val json = JSON()
            var ownChan = JSONArray(data[0].toString())

            for (i in 0 until ownChan.length()) {
                SocketUser.joinedChannels.add(ownChan[i].toString())
                val room = Room(User.username, ownChan[i].toString())
                SocketUser.socket.join(json.toJson(room))
            }

        }

    }

    private fun onChannelRooms() {
        SocketUser.socket.socket.on("allRooms") { data ->
            var chan = JSONArray(data[0].toString())

            activity?.runOnUiThread(java.lang.Runnable {
                adapter?.clear()
                recyclerView_channel?.adapter = adapter
                channelNames.clear()
                for (i in 0 until chan.length()) {
                    channelNames.add(chan[i] as String)

                }
                if(Channel.isAMatch){
                    for(objects in SocketUser.matchChannels.reversed()) {
                        addChannel(objects)
                    }
                }
                for(objects in SocketUser.joinedChannels.reversed()) {
                        addChannel(objects)
                }
                for(objects in channelNames) {
                    if(!SocketUser.joinedChannels.contains(objects))
                        addChannel(objects)
                }
            })
        }

    }

}

class ChannelItem(var name: String): Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.channel_name.text = name

        if(SocketUser.matchChannels.contains(name)) {
            viewHolder.itemView.button_leave_channel.visibility = View.VISIBLE
            viewHolder.itemView.button_join_channel.visibility = View.INVISIBLE
            viewHolder.itemView.channel_name.isEnabled = true
            viewHolder.itemView.channel_name.text = name.subSequence(0, name.length-1)
            viewHolder.itemView.Layout1.isEnabled = true
        }

        if(SocketUser.joinedChannels.contains(name) || SocketUser.matchChannels.contains(name)) {
            viewHolder.itemView.button_leave_channel.visibility = View.VISIBLE
            viewHolder.itemView.button_join_channel.visibility = View.INVISIBLE
            viewHolder.itemView.channel_name.isEnabled = true
            viewHolder.itemView.Layout1.isEnabled = true
        }
        else {
            viewHolder.itemView.button_leave_channel.visibility = View.INVISIBLE
            viewHolder.itemView.button_join_channel.visibility = View.VISIBLE
            viewHolder.itemView.channel_name.isEnabled = false
            viewHolder.itemView.Layout1.isEnabled = false
        }




        viewHolder.itemView.button_leave_channel.setOnClickListener {

            viewHolder.itemView.channel_name.isEnabled = false
            //SocketUser.joinedChannels.remove(name)
            viewHolder.itemView.Layout1.isEnabled = false
            viewHolder.itemView.button_leave_channel.visibility = View.INVISIBLE
            viewHolder.itemView.button_join_channel.visibility = View.VISIBLE
            val room = Room(User.username, name)
            val json = JSON()
            if(!SocketUser.matchChannels.contains(name)) {
                SocketUser.socket.leaveRoom(json.toJson(room))
            }
        }

        viewHolder.itemView.button_join_channel.setOnClickListener {

            viewHolder.itemView.channel_name.isEnabled = true
            viewHolder.itemView.Layout1.isEnabled = true
            //SocketUser.joinedChannels.add(name)
            viewHolder.itemView.button_leave_channel.visibility = View.VISIBLE
            viewHolder.itemView.button_join_channel.visibility = View.INVISIBLE
            val room = Room(User.username, name)
            val json = JSON()
            if(!SocketUser.matchChannels.contains(name)) {
                SocketUser.socket.join(json.toJson(room))
            }
        }

        viewHolder.itemView.channel_name.setOnClickListener {Activity ->
            //Channel.isAMatch = SocketUser.matchChannels.contains(name)
            Channel.name = name
            Activity.findNavController().navigate(R.id.action_fragmentChannel_to_fragmentChat)

        }

        viewHolder.itemView.Layout1.setOnClickListener {Activity ->
            //Channel.isAMatch = SocketUser.matchChannels.contains(name)
            Channel.name = name
            Activity.findNavController().navigate(R.id.action_fragmentChannel_to_fragmentChat)
        }

    }
    override fun getLayout(): Int {
        return R.layout.channel_row
    }

}
package com.example.polypaintapp

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_coop.*
import kotlinx.android.synthetic.main.games_row.view.*
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class fragment_sprint_coop : Fragment() {

    private val adapter = GroupAdapter<ViewHolder>()
    private val coopGames = ArrayList<CoopGame>()
    private val gson = Gson()
    private val parser = JsonParser()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_coop, container, false)
        //Back pressed Logic for fragment
        v.setFocusableInTouchMode(true)
        v.requestFocus()
        v.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
                if (event.getAction() === KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {

                        val intent = Intent(context, Game_Modes::class.java)

                        startActivity(intent)
                    }
                }
                return false
            }
        })
        // Inflate the layout for this fragment
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val json = JSON()

        chatButton_coop.setOnClickListener {view ->

            view.findNavController().navigate(R.id.action_fragment_sprint_coop_to_nav_graph_chat)
        }

        adapter.clear()
        recycle_view_coop.adapter = adapter
        createCoop.setOnClickListener{ Activity ->
            CurrentGameMode.gameMode = GameModes.Coop.ordinal
            Activity?.findNavController()?.navigate(R.id.action_fragment_sprint_coop_to_fragment_CreateMatch)
        }
        onMatchCreated()
        getCoopMatches()
        onEditMatch()
        SocketUser.socket.socket.emit("getMatches", json.toJson(GameRequest(1)))

    }


    override fun onResume() {
        super.onResume()

    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun addCoopGame(game : CoopGame) {
        adapter.add(CoopItem(game))
    }

    fun onEditMatch() {

        SocketUser.socket.socket.on("matchEdit") { data ->
            val json = JSON()
            SocketUser.socket.socket.emit("getMatches", json.toJson(GameRequest(1)))

        }
    }

    fun onMatchCreated() {
        SocketUser.socket.socket.on("matchCreated") { data ->
            val json = JSON()
            var jsonObj = json.fromJson(data[0].toString())
            var type = jsonObj["type"]
            if (type == 1) {
                var games: JsonObject = jsonObj["content"] as JsonObject
                val players: ArrayList<MatchUserDetails> = ArrayList()
                val coopgame = games
                    var playersArray = coopgame["players"] as JsonArray<JsonObject>
                    for (player in playersArray) {
                        val pNotParsed = parser.parse(player.toJsonString()).asJsonObject
                        val playersp = gson.fromJson(pNotParsed, MatchUserDetails::class.java)
                        players.add(playersp)
                    }
                    val coop = CoopGame(
                        coopgame["name"] as String,
                        coopgame["nbRounds"] as Int,
                        coopgame["type"] as Int,
                        coopgame["creator"] as String,
                        coopgame["playerCount"] as Int,
                        players,
                        coopgame["aiplayer"] as String,
                        coopgame["placesLeft"] as Int
                    )
                if (!arrayContainsGame(coop)) {
                    activity?.runOnUiThread(java.lang.Runnable {
                        coopGames.add(coop)
                        addCoopGame(coop)
                    })
                }
                }

        }
    }

    fun arrayContainsGame(testGame: CoopGame): Boolean {
        coopGames.forEach { it
            if (it.name == testGame.name)
                return true
        }
        return false
    }

    fun getCoopMatches() {
        val json = JSON()


        SocketUser.socket.socket.on("getMatches") { data ->


            var jsonObj = json.fromJson(data[0].toString())
            var type = jsonObj["type"]
            if (type == 1) {
            var games = jsonObj["content"] as JsonArray<JsonObject>

            var players = ArrayList<MatchUserDetails>()

                activity?.runOnUiThread(java.lang.Runnable {
                    adapter.clear()
                })
            coopGames.clear()
            for (i in 0 until games.size) {
                val coopgame = games[i]

                    var playersArray = coopgame["players"] as JsonArray<JsonObject>
                    for (player in playersArray) {
                        val pNotParsed = parser.parse(player.toJsonString()).asJsonObject
                        val playersp = gson.fromJson(pNotParsed, MatchUserDetails::class.java)
                        players.add(playersp)
                    }
                    val coop = CoopGame(
                        coopgame["name"] as String,
                        coopgame["nbRounds"] as Int,
                        coopgame["type"] as Int,
                        coopgame["creator"] as String,
                        coopgame["playerCount"] as Int,
                        players,
                        coopgame["aiplayer"] as String,
                        coopgame["placesLeft"] as Int
                    )

                if (!arrayContainsGame(coop)) {
                    coopGames.add(coop)
                    activity?.runOnUiThread(java.lang.Runnable {
                        addCoopGame(coop)
                    })
                }
                }
            }
        }

    }
}

class CoopItem(var Cgame : CoopGame): Item<ViewHolder>() {

    val json = JSON()
    var gotIt = false
    val gson = Gson()
    val parser = JsonParser()


    fun onJoin() {

        SocketUser.socket.socket.on("matchJoined") { data ->

            var jsonObj = json.fromJson(data[0].toString())
            var type = jsonObj["type"]
            if (type == 1) {
                var games: JsonObject = jsonObj["content"] as JsonObject
                val players: ArrayList<MatchUserDetails> = ArrayList()
                val coopgame = games
                if (coopgame["name"] as String == Cgame.name) {
                    var playersArray = coopgame["players"] as JsonArray<JsonObject>
                    for (player in playersArray) {
                        val pNotParsed = parser.parse(player.toJsonString()).asJsonObject
                        val playersp = gson.fromJson(pNotParsed, MatchUserDetails::class.java)
                        players.add(playersp)
                    }
                    val coop = CoopGame(
                        coopgame["name"] as String,
                        coopgame["nbRounds"] as Int,
                        coopgame["type"] as Int,
                        coopgame["creator"] as String,
                        coopgame["playerCount"] as Int,
                        players,
                        coopgame["aiplayer"] as String,
                        coopgame["placesLeft"] as Int
                    )
                    WaitingRoom.Game = coop
                    gotIt = true


                }


            }
        }
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        onJoin()
        viewHolder.itemView.game_name.text = Cgame.name
        viewHolder.itemView.game_participants.text = Cgame.placesLeft.toString()
        viewHolder.itemView.rounds.text = Cgame.nbRounds.toString()
        if(Cgame.placesLeft == 0) {
            viewHolder.itemView.button_join_game.visibility = View.INVISIBLE
        }else {
            viewHolder.itemView.button_join_game.visibility = View.VISIBLE
        }

        viewHolder.itemView.game_name.setOnClickListener {

        }

        viewHolder.itemView.button_join_game.setOnClickListener { Activity ->
            CurrentGameMode.gameMode = 1
            WaitingRoom.name = Cgame.name
            Channel.name = WaitingRoom.name + CurrentGameMode.gameMode.toString()
            SocketUser.matchChannels.add(Channel.name)
            Channel.isAMatch = true

            SocketUser.socket.joinMatch(json.toJson(MatchRequest(User.username, Cgame.name + CurrentGameMode.gameMode.toString())))
            SocketUser.joinedChannels.add(Cgame.name)
            var canJoin = true
            try {
                Timer().schedule(1500) {
                    if (!gotIt)
                        canJoin = false
                    gotIt = true
                }
                while (!gotIt) {
                }

                if(canJoin)
                    Activity.findNavController()
                        ?.navigate(R.id.action_fragment_sprint_coop_to_fragment_WaitingRoom)
                else
                    Toast.makeText(Activity.context, "Someone has been faster! Sorry!", Toast.LENGTH_SHORT).show()
            } catch(e : Exception) {
                println("HAMDOULLAH")
            }
        }

        viewHolder.itemView.game_participants.setOnClickListener {

        }

    }

    override fun getLayout(): Int {
        return R.layout.games_row
    }
}


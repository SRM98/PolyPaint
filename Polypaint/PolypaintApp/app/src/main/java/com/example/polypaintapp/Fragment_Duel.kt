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
import com.beust.klaxon.Json
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_classique.*
import kotlinx.android.synthetic.main.fragment_duel.*
import kotlinx.android.synthetic.main.games_row.view.*
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class Fragment_Duel : Fragment() {
    private val adapter = GroupAdapter<ViewHolder>()
    private val DuelGamesArr = ArrayList<DuelGame>()
    private val gson = Gson()
    private val parser = JsonParser()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_duel, container, false)
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
        chatButton_duel.setOnClickListener { view ->

            view.findNavController().navigate(R.id.action_fragment_Duel_to_nav_graph_chat)
        }

        adapter.clear()
        recycle_view_duel.adapter = adapter
        getDuelMatches()
        createDuel.setOnClickListener { Activity ->
            CurrentGameMode.gameMode = GameModes.Duel.ordinal
            Activity?.findNavController()
                ?.navigate(R.id.action_fragment_Duel_to_fragment_CreateMatch)
        }
        onMatchCreated()
        onEditMatch()
        SocketUser.socket.socket.emit("getMatches", json.toJson(GameRequest(2)))
    }

    fun addDuelGame(game: DuelGame) {

        adapter.add(DuelItem(game))

    }

    fun onMatchCreated() {
        SocketUser.socket.socket.on("matchCreated") { data ->
            val json = JSON()
            var jsonObj = json.fromJson(data[0].toString())
            var type = jsonObj["type"]
            if (type == 2) {
                var games: JsonObject = jsonObj["content"] as JsonObject
                println(games)
                val duelGameobj = games
                val player1UnParsed = duelGameobj["player1"] as JsonObject
                val player2UnParsed = duelGameobj["player2"] as JsonObject

                val player1parsed = gson.fromJson(
                    parser.parse(player1UnParsed.toJsonString()).asJsonObject,
                    MatchUserDetails::class.java
                )
                val player2parsed = gson.fromJson(
                    parser.parse(player2UnParsed.toJsonString()).asJsonObject,
                    MatchUserDetails::class.java
                )
                val duel = DuelGame(
                    duelGameobj["name"] as String,
                    duelGameobj["nbRounds"] as Int,
                    duelGameobj["type"] as Int,
                    duelGameobj["creator"] as String,
                    duelGameobj["playerCount"] as Int,
                    player1parsed,
                    player2parsed,
                    duelGameobj["placesLeft"] as Int
                )
                if (!arrayContainsGame(duel)) {
                    DuelGamesArr.add(duel)
                    activity?.runOnUiThread(java.lang.Runnable {
                        addDuelGame(duel)
                    })
                }
            }


        }
    }


    fun onEditMatch() {

        SocketUser.socket.socket.on("matchEdit") { data ->
            val json = JSON()
            SocketUser.socket.socket.emit("getMatches", json.toJson(GameRequest(2)))


        }
    }

    fun arrayContainsGame(testGame: DuelGame): Boolean {
        DuelGamesArr.forEach { it
            if (it.name == testGame.name)
                return true
        }
        return false
    }


    fun getDuelMatches() {

        val json = JSON()

        SocketUser.socket.socket.on("getMatches") { data ->


            var jsonObj = json.fromJson(data[0].toString())
            var type = jsonObj["type"]
            if (type == 2) {
                var games = jsonObj["content"] as JsonArray<JsonObject>
                activity?.runOnUiThread(java.lang.Runnable {
                    adapter.clear()
                })
                DuelGamesArr.clear()
                for (i in 0 until games.size) {
                    val duelGameobj = games[i]
                    val player1UnParsed = duelGameobj["player1"] as JsonObject
                    val player2UnParsed = duelGameobj["player2"] as JsonObject

                    val player1parsed = gson.fromJson(
                        parser.parse(player1UnParsed.toJsonString()).asJsonObject,
                        MatchUserDetails::class.java
                    )
                    val player2parsed = gson.fromJson(
                        parser.parse(player2UnParsed.toJsonString()).asJsonObject,
                        MatchUserDetails::class.java
                    )
                    val duel = DuelGame(
                        duelGameobj["name"] as String,
                        duelGameobj["nbRounds"] as Int,
                        duelGameobj["type"] as Int,
                        duelGameobj["creator"] as String,
                        duelGameobj["playerCount"] as Int,
                        player1parsed,
                        player2parsed,
                        duelGameobj["placesLeft"] as Int
                    )
                    if (!arrayContainsGame(duel)) {
                        DuelGamesArr.add(duel)
                        activity?.runOnUiThread(java.lang.Runnable {
                            addDuelGame(duel)
                        })
                    }
                }
            }
        }

    }
}


class DuelItem(var Cgame: DuelGame) : Item<ViewHolder>() {

    val json = JSON()
    var gotIt = false
    val gson = Gson()
    val parser = JsonParser()

    fun onJoin() {
        SocketUser.socket.socket.on("matchJoined") { data ->

            var jsonObj = json.fromJson(data[0].toString())
            var type = jsonObj["type"]
            if (type == 2) {
                var games: JsonObject = jsonObj["content"] as JsonObject
                println(games)
                val duelGameobj = games
                if (duelGameobj["name"] as String == Cgame.name) {
                    val player1UnParsed = duelGameobj["player1"] as JsonObject
                    val player2UnParsed = duelGameobj["player2"] as JsonObject

                    val player1parsed = gson.fromJson(
                        parser.parse(player1UnParsed.toJsonString()).asJsonObject,
                        MatchUserDetails::class.java
                    )
                    val player2parsed = gson.fromJson(
                        parser.parse(player2UnParsed.toJsonString()).asJsonObject,
                        MatchUserDetails::class.java
                    )
                    val duel = DuelGame(
                        duelGameobj["name"] as String,
                        duelGameobj["nbRounds"] as Int,
                        duelGameobj["type"] as Int,
                        duelGameobj["creator"] as String,
                        duelGameobj["playerCount"] as Int,
                        player1parsed,
                        player2parsed,
                        duelGameobj["placesLeft"] as Int
                    )
                    WaitingRoom.Game = duel
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
            CurrentGameMode.gameMode = 2
            WaitingRoom.name = Cgame.name

            Channel.name = WaitingRoom.name + CurrentGameMode.gameMode.toString()
            SocketUser.matchChannels.add(Channel.name)
            Channel.isAMatch = true

            SocketUser.socket.joinMatch(
                json.toJson(
                    MatchRequest(
                        User.username,
                        Cgame.name + CurrentGameMode.gameMode.toString()
                    )
                )
            )
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
                    ?.navigate(R.id.action_fragment_Duel_to_fragment_WaitingRoom)
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


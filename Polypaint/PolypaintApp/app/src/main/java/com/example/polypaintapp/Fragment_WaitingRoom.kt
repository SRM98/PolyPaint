package com.example.polypaintapp

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle

import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import kotlinx.android.synthetic.main.fragment_waiting_room.*
import kotlinx.android.synthetic.main.games_row.*
import android.view.KeyEvent.KEYCODE_BACK
import android.widget.Toast
import androidx.core.net.toUri
import androidx.navigation.NavOptions
import com.beust.klaxon.Json
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.mikepenz.iconics.Iconics.applicationContext
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_profilview.*
import kotlinx.android.synthetic.main.users_row.view.*
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule


enum class MatchEditActions {
    AddAI,
    RemoveAI,
    ChangeAI,
    SwitchTeam,
    Ready,
}

enum class Team {
    TeamA,
    TeamB,
}


class Fragment_WaitingRoom : Fragment() {
    var onLeave = false
    val parser = JsonParser()
    val gson = Gson()
    private val adapterA = GroupAdapter<ViewHolder>()
    private val adapterB = GroupAdapter<ViewHolder>()
    var gotAll = false
    var gotIt = false



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_waiting_room, container, false)
        //Back pressed Logic for fragment
        v.setFocusableInTouchMode(true)
        v.requestFocus()
        v.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
                if (event.getAction() === KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        val json = JSON()
                        //Channel.matchChannel = ""
                        Channel.isAMatch = false
                        SocketUser.matchChannels.remove(WaitingRoom.name + CurrentGameMode.gameMode.toString())
                        val request = MatchRequest(User.username, WaitingRoom.name + CurrentGameMode.gameMode.toString())
                        val room = Room(User.username,WaitingRoom.name + CurrentGameMode.gameMode.toString())
                        SocketUser.socket.leaveRoom(json.toJson(room))
                        SocketUser.joinedChannels.remove(WaitingRoom.name + CurrentGameMode.gameMode.toString())
                        SocketUser.socket.leaveMatch(json.toJson(request))
                        SocketUser.joinedChannels.remove(WaitingRoom.name + CurrentGameMode.gameMode.toString())
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


    override fun onDestroy() {
        val json = JSON()
        Channel.matchChannel = ""
        Channel.isAMatch = false
        SocketUser.matchChannels.remove(WaitingRoom.name + CurrentGameMode.gameMode.toString())
        val request = MatchRequest(User.username, WaitingRoom.name + CurrentGameMode.gameMode.toString())
        val room = Room(User.username,WaitingRoom.name + CurrentGameMode.gameMode.toString())
        SocketUser.socket.leaveRoom(json.toJson(room))
        SocketUser.joinedChannels.remove(WaitingRoom.name + CurrentGameMode.gameMode.toString())
        SocketUser.socket.leaveMatch(json.toJson(request))
        SocketUser.joinedChannels.remove(WaitingRoom.name + CurrentGameMode.gameMode.toString())
        val intent = Intent(context, Game_Modes::class.java)
        startActivity(intent)
        super.onDestroy()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        start_match_button?.visibility = View.INVISIBLE
        adapterA.clear()
        teamA_players.adapter = adapterA

        adapterB.clear()
        teamB_players.adapter = adapterB

        onEditMatch()
        onMatchStarted()

        Game_name.text = WaitingRoom.name
        //Channel.name = WaitingRoom.name + CurrentGameMode.gameMode.toString()
        //Channel.matchChannel = User.username + CurrentGameMode.gameMode.toString()
        Channel.isAMatch = true


        leave_button.setOnClickListener {

            val json = JSON()
            //Channel.matchChannel = ""
            SocketUser.matchChannels.remove(WaitingRoom.name + CurrentGameMode.gameMode.toString())
            Channel.isAMatch = false
            val request = MatchRequest(User.username, WaitingRoom.name + CurrentGameMode.gameMode.toString())
            val room = Room(User.username,WaitingRoom.name + CurrentGameMode.gameMode.toString())
            SocketUser.socket.leaveRoom(json.toJson(room))
            SocketUser.joinedChannels.remove(WaitingRoom.name + CurrentGameMode.gameMode.toString())
            SocketUser.socket.leaveMatch(json.toJson(request))
            SocketUser.joinedChannels.remove(WaitingRoom.name + CurrentGameMode.gameMode.toString())
            val intent = Intent(context, Game_Modes::class.java)
            startActivity(intent)

        }

        start_match_button.setOnClickListener {
            val json = com.google.gson.JsonObject()
            json.addProperty("id", WaitingRoom.name+CurrentGameMode.gameMode)
            json.addProperty("options", User.username)
            json.addProperty("action", MatchEditActions.Ready.ordinal)
            SocketUser.socket.socket.emit("editMatch", json)
            if (start_match_button.text == "Ready") {
                start_match_button.text = "Cancel"
            } else {
                start_match_button.text = "Ready"
            }
        }

        refresh()
        getImages()
        sendImagessocket()

    }

    fun refresh() {
        val json = com.google.gson.JsonObject()
        json.addProperty("id", WaitingRoom.name + CurrentGameMode.gameMode)
        json.addProperty("action", MatchEditActions.RemoveAI.ordinal)
        json.addProperty("options", Team.TeamA.ordinal)
        SocketUser.socket.socket.emit("editMatch", json)
    }


    override fun onDestroyView() {
        onLeave = true
        val json = JSON()
        val request = MatchRequest(User.username, WaitingRoom.name+ CurrentGameMode.gameMode.toString())
        SocketUser.socket.leaveMatch(json.toJson(request))
        super.onDestroyView()
    }



    fun sendImagessocket() {
        val json = JSON()
        val players = players(PlayersInfo.playersNames)
        SocketUser.socket.getImages(json.toJson(players))

    }

    fun getImages() {
        SocketUser.socket.socket.on("giveAvatarUrl") {data->

            //PlayersInfo.playersNames.clear()
            //PlayersInfo.url.clear()
            val informations = JsonParser().parse(data[0].toString()).asJsonObject
            var players = informations["players"] as com.google.gson.JsonArray
            var url = informations["url"] as com.google.gson.JsonArray

            for(names in players) {
                PlayersInfo.playersNames.add(names.toString())
            }

            for(names in url) {
                PlayersInfo.url.add(names.toString())
            }

            updateView()

        }
    }

    private fun addUserA(name: String, avatar: String, ready : Boolean) {
        context?.let { context ->
            adapterA.add(UsersItem(name, avatar, ready, context))
        }
    }

    private fun addUserB(name: String, avatar: String, ready : Boolean) {

        context?.let { context ->
            adapterB.add(UsersItem(name, avatar, ready, context))
        }
    }

    private fun containsAiPlayer(team: ArrayList<MatchUserDetails>): Boolean {
        for (member in team) {
            if(member.username.contains("(Ai Player"))
                return true
        }
        return false
    }

    fun updateView() {
        activity?.runOnUiThread {
            adapterA.clear()
            adapterB.clear()
            when (CurrentGameMode.gameMode) {
                0 -> {
                    var cGame: ClassicGame = WaitingRoom.Game as ClassicGame
                    nb_rounds_number?.text = cGame.nbRounds.toString()

                    for (players in cGame.teamA) {
                        if (PlayersInfo.url.size != 0 && PlayersInfo.playersNames.indexOf(players.username) < PlayersInfo.url.size) {
                            addUserA(
                                    players.username,
                                    PlayersInfo.url.get(PlayersInfo.playersNames.indexOf(players.username)),
                                    players.ready
                            )
                        }
                    }
                    for (players in cGame.teamB) {
                        if (PlayersInfo.url.size != 0 && PlayersInfo.playersNames.indexOf(players.username) < PlayersInfo.url.size) {
                            addUserB(
                                    players.username,
                                    PlayersInfo.url.get(PlayersInfo.playersNames.indexOf(players.username)),
                                    players.ready
                            )
                        }
                    }
                    if (add_bot_1 != null && add_bot_2 != null) {
                        if((cGame.teamA.size + cGame.teamB.size) == 4) {
                            start_match_button.visibility = View.VISIBLE
                        } else {
                            start_match_button.visibility = View.INVISIBLE
                        }

                        if (this.containsAiPlayer(cGame.teamA)) {
                            add_bot_2.text = "REMOVE BOT"
                        } else {
                            add_bot_2.text = "ADD BOT"
                        }

                        if (this.containsAiPlayer(cGame.teamB)) {
                            add_bot_1.text = "REMOVE BOT"
                        } else {
                            add_bot_1.text = "ADD BOT"
                        }

                        add_bot_1.visibility = View.VISIBLE
                        add_bot_2.visibility = View.VISIBLE

                        if (cGame.teamA.size == 2 && !containsAiPlayer(cGame.teamA)) {
                            add_bot_2.visibility = View.INVISIBLE
                        }
                        if (cGame.teamB.size == 2 && !containsAiPlayer(cGame.teamB)) {
                            add_bot_1.visibility = View.INVISIBLE
                        }

                        add_bot_1.setOnClickListener {
                            if (add_bot_1.text == "ADD BOT") {
                                val json = com.google.gson.JsonObject()
                                json.addProperty("id", WaitingRoom.name + CurrentGameMode.gameMode)
                                json.addProperty("action", MatchEditActions.AddAI.ordinal)
                                json.addProperty("options", Team.TeamA.ordinal)
                                add_bot_1.text = "REMOVE BOT"
                                SocketUser.socket.socket.emit("editMatch", json)
                            } else {
                                add_bot_1.text = "ADD BOT"
                                val json = com.google.gson.JsonObject()
                                json.addProperty("id", WaitingRoom.name + CurrentGameMode.gameMode)
                                json.addProperty("action", MatchEditActions.RemoveAI.ordinal)
                                json.addProperty("options", Team.TeamA.ordinal)
                                SocketUser.socket.socket.emit("editMatch", json)
                            }
                        }
                        add_bot_2.setOnClickListener {
                            if (add_bot_2.text == "ADD BOT") {
                                add_bot_2.text = "REMOVE BOT"
                                val json = com.google.gson.JsonObject()
                                json.addProperty("id", WaitingRoom.name + CurrentGameMode.gameMode)
                                json.addProperty("action", MatchEditActions.AddAI.ordinal)
                                json.addProperty("options", Team.TeamB.ordinal)
                                SocketUser.socket.socket.emit("editMatch", json)
                            } else {
                                add_bot_2.text = "ADD BOT"
                                val json = com.google.gson.JsonObject()
                                json.addProperty("id", WaitingRoom.name + CurrentGameMode.gameMode)
                                json.addProperty("action", MatchEditActions.RemoveAI.ordinal)
                                json.addProperty("options", Team.TeamB.ordinal)
                                SocketUser.socket.socket.emit("editMatch", json)
                            }
                        }
                    }
                }

                1 -> {
                    var coGame: CoopGame = WaitingRoom.Game as CoopGame
                    nb_rounds_number?.text = coGame.nbRounds.toString()
                    for (players in coGame.players) {
                        if (PlayersInfo.url.size != 0 && PlayersInfo.playersNames.indexOf(players.username) < PlayersInfo.url.size) {
                            addUserA(
                                players.username,
                                PlayersInfo.url.get(PlayersInfo.playersNames.indexOf(players.username)),
                                players.ready
                            )

                        }
                    }
                    if(coGame.players.size >= 2 ) {
                        start_match_button?.visibility = View.VISIBLE
                    }else {
                        start_match_button?.visibility = View.INVISIBLE
                    }
                    VS_text?.visibility = View.GONE
                    teamB_players?.visibility = View.GONE
                }
                2 -> {
                    var dGame: DuelGame = WaitingRoom.Game as DuelGame
                    nb_rounds_number?.text = dGame.nbRounds.toString()
                    if (dGame.player1.username != "" && PlayersInfo.url.size != 0 && PlayersInfo.playersNames.indexOf(dGame.player1.username) < PlayersInfo.url.size) {
                        addUserA(
                            dGame.player1.username,
                            PlayersInfo.url.get(PlayersInfo.playersNames.indexOf(dGame.player1.username)),
                            dGame.player1.ready
                        )
                    }
                    if (dGame.player2.username != "" && PlayersInfo.url.size != 0 && PlayersInfo.playersNames.indexOf(dGame.player1.username) < PlayersInfo.url.size) {
                        addUserB(
                            dGame.player2.username,
                            PlayersInfo.url.get(PlayersInfo.playersNames.indexOf(dGame.player2.username)),
                            dGame.player2.ready
                        )
                    }

                    if(dGame.player1.username != "" && dGame.player2.username != "") {
                        start_match_button?.visibility = View.VISIBLE
                    }else {
                        start_match_button?.visibility = View.INVISIBLE
                    }
                }

                3 -> {
                    view?.findNavController()
                        ?.navigate(R.id.action_fragment_WaitingRoom_to_fragment_Inmatch)
                }


            }
        }
    }


    fun onMatchStarted() {
        SocketUser.socket.socket.on("matchStarted") { data ->
            val matchJson = JsonParser().parse(data[0].toString()).asJsonObject
            var matchId = matchJson.get("content").toString().removePrefix("\"")
            matchId = matchId.removeSuffix("\"")
            var gameType = -1
            if (WaitingRoom.Game is ClassicGame) {
                gameType = GameModes.Classic.ordinal
            } else if (WaitingRoom.Game is DuelGame) {
                gameType = GameModes.Duel.ordinal
            } else if (WaitingRoom.Game is CoopGame) {
                gameType = GameModes.Coop.ordinal
            } else if (WaitingRoom.Game is SoloGame) {
                gameType = GameModes.Solo.ordinal
            }
            val currentId = WaitingRoom.name + gameType.toString()
            if (matchId == currentId && CurrentGameMode.gameMode != GameModes.Solo.ordinal) {
                    view?.findNavController()
                        ?.navigate(R.id.action_fragment_WaitingRoom_to_fragment_Inmatch)
            } else {
                activity?.runOnUiThread {
                    Toast.makeText(activity, "Error while redirecting to game", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun onEditMatch() {

        SocketUser.socket.socket.on("matchEdit") { data ->
            PlayersInfo.playersNames.clear()
            PlayersInfo.url.clear()
            val json = JSON()
            var jsonObj = json.fromJson(data[0].toString())
            var type = jsonObj["type"]
            var games: JsonObject = jsonObj["content"] as JsonObject
            if (games["id"] == WaitingRoom.name + CurrentGameMode.gameMode) {
                when (type) {
                    0 -> {
                        var arrayTeamA = ArrayList<MatchUserDetails>()
                        var arrayTeamB = ArrayList<MatchUserDetails>()

                        val classicGameobj = games
                        var teamA = classicGameobj["teamA"] as JsonArray<JsonObject>
                        var teamB = classicGameobj["teamB"] as JsonArray<JsonObject>
                        for (i in 0 until teamA.size) {
                            val aNotParsed = parser.parse(teamA[i].toJsonString()).asJsonObject
                            val A = gson.fromJson(aNotParsed, MatchUserDetails::class.java)
                            arrayTeamA.add(A)
                            PlayersInfo.playersNames.add(A.username)
                        }
                        for (i in 0 until teamB.size) {
                            val bNotParsed = parser.parse(teamB[i].toJsonString()).asJsonObject
                            val B = gson.fromJson(bNotParsed, MatchUserDetails::class.java)
                            arrayTeamB.add(B)
                            PlayersInfo.playersNames.add(B.username)
                        }
                        val classic = ClassicGame(
                            classicGameobj["name"] as String,
                            classicGameobj["nbRounds"] as Int,
                            classicGameobj["type"] as Int,
                            classicGameobj["creator"] as String,
                            classicGameobj["playerCount"] as Int,
                            arrayTeamB,
                            arrayTeamA,
                            classicGameobj["placesLeft"] as Int
                        )
                        WaitingRoom.Game = classic
                        if(!onLeave) {
                            sendImagessocket()
                        }

                    }

                    1 -> {
                        val players: ArrayList<MatchUserDetails> = ArrayList()
                        val coopgame = games
                        var playersArray = coopgame["players"] as JsonArray<JsonObject>
                        for (player in playersArray) {
                            val pNotParsed = parser.parse(player.toJsonString()).asJsonObject
                            val playersp = gson.fromJson(pNotParsed, MatchUserDetails::class.java)
                            players.add(playersp)
                            PlayersInfo.playersNames.add(playersp.username)
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
                        if(!onLeave) {
                            sendImagessocket()
                        }
                    }


                    2 -> {
                        val duelGameobj = games
                        val player1UnParsed = duelGameobj["player1"] as JsonObject
                        val player2UnParsed = duelGameobj["player2"] as JsonObject

                        val player1parsed = gson.fromJson(parser.parse(player1UnParsed.toJsonString()).asJsonObject, MatchUserDetails::class.java)
                        val player2parsed = gson.fromJson(parser.parse(player2UnParsed.toJsonString()).asJsonObject, MatchUserDetails::class.java)
                        PlayersInfo.playersNames.add(player1parsed.username)
                        PlayersInfo.playersNames.add(player2parsed.username)
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
                        if(!onLeave) {
                            sendImagessocket()
                        }
                    }
                }
            }
        }
    }
}

class UsersItem(var name: String, var imageUrl : String, var ready : Boolean, var Ucontext: Context): Item<ViewHolder>() {

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.player_name.text = name


        if(imageUrl.substring(1, imageUrl.length-1) != "") {
            val uri = IP + "/" + imageUrl.substring(1, imageUrl.length - 1)
            Glide.with(Ucontext).load(uri.toUri()).into(viewHolder.itemView.player_image)
        } else {
            val bitmapNoPhoto = BitmapFactory.decodeResource(Ucontext.resources, R.drawable.no_photo)
            viewHolder.itemView.player_image.setImageBitmap(bitmapNoPhoto)
        }

        if(ready) {
            viewHolder.itemView.ready.visibility = View.VISIBLE
            viewHolder.itemView.not_ready.visibility = View.INVISIBLE
        } else {
            viewHolder.itemView.ready.visibility = View.INVISIBLE
            viewHolder.itemView.not_ready.visibility = View.VISIBLE
        }



    }
    override fun getLayout(): Int {
        return R.layout.users_row
    }

}
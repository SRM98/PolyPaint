package com.example.polypaintapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.example.polypaintapp.SocketUser.socket
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.mikepenz.iconics.Iconics
import kotlinx.android.synthetic.main.fragment_create_match.*
import org.json.JSONObject


class Fragment_CreateMatch : Fragment() {

    private val matchOptions = ArrayList<Int>()
    var gotIt = false
    val parser = JsonParser()
    val gson = Gson()
    var matchMakingError = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) =
        inflater.inflate(R.layout.fragment_create_match, container, false)!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        chatButton_create.setOnClickListener {view ->
            view?.findNavController()?.navigate(R.id.action_fragment_CreateMatch_to_nav_graph_chat)
        }
        if(CurrentGameMode.gameMode == GameModes.Coop.ordinal) {
            outlinedSpinner.visibility = View.INVISIBLE
            nbOfRoundsTitle.visibility = View.INVISIBLE
        }

        matchOptions.add(1)
        matchOptions.add(3)
        matchOptions.add(5)
        matchOptions.add(7)
        matchOptions.add(9)
        val adapter = ArrayAdapter<Int>(
            activity!!.applicationContext, android.R.layout.simple_list_item_1,
            matchOptions)
        nbMatches.adapter = adapter
        socket.socket.on("matchmakingError") { data ->
            val json = JSON()
            var jsonObj = json.fromJson(data[0].toString())
            val message: String = jsonObj["description"] as String
            activity?.runOnUiThread {
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
            }
            matchMakingError = true
            gotIt = true
        }
        createMatchWithOp.setOnClickListener {Activity ->
            var name = matchName.text.toString()
            name = name.trim()
            WaitingRoom.name = name
            if(name.isEmpty()) {
                activity?.runOnUiThread {
                    Toast.makeText(activity, "Please enter a match name", Toast.LENGTH_SHORT).show()
                }
            } else {
                val json = JSON()
                val match = Match(name, nbMatches.selectedItem.toString().toInt(),
                    CurrentGameMode.gameMode, User.username)
                socket.createMatch(json.toJson(match))
                Channel.name = name + CurrentGameMode.gameMode.toString()
                SocketUser.matchChannels.add(Channel.name)
                Channel.isAMatch = true
                onEditMatch()
                while (!gotIt){}
                gotIt = false
                if (!matchMakingError) {
                    Activity?.findNavController()?.navigate(R.id.action_fragment_CreateMatch_to_fragment_WaitingRoom)
                }
            }
        }
    }


    fun onEditMatch() {
        socket.socket.on("matchJoined") { data ->
            matchMakingError = false
            val json = JSON()
            var jsonObj = json.fromJson(data[0].toString())
            var type = jsonObj["type"]
            var games: JsonObject = jsonObj["content"] as JsonObject
            if (games["name"] == WaitingRoom.name) {
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
                        gotIt = true

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
                        gotIt = true
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
                        gotIt = true
                    }
                }

            }
        }
    }
}
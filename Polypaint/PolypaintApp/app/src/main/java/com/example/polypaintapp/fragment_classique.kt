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
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_classique.*
import kotlinx.android.synthetic.main.games_row.view.*
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule


class fragment_classique : Fragment() {

    private val adapter = GroupAdapter<ViewHolder>()
    private val classicGamesArr = ArrayList<ClassicGame>()
    private val gson = Gson()
    private val parser = JsonParser()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_classique, container, false)
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

        chatButton_classic.setOnClickListener { view ->

            view.findNavController().navigate(R.id.action_fragment_classique_to_nav_graph_chat)
        }

        createClassic.setOnClickListener { Activity ->
            CurrentGameMode.gameMode = GameModes.Classic.ordinal
            Activity?.findNavController()
                ?.navigate(R.id.action_fragment_classique_to_fragment_CreateMatch)
        }
        val json = JSON()
        adapter.clear()
        recycle_view_classic.adapter = adapter

        onMatchCreated()
        getClassicMatches()
        onEditMatch()
        SocketUser.socket.socket.emit("getMatches", json.toJson(GameRequest(0)))
    }


    private fun loadBannerAd() {

//        val adRequest = AdRequest.Builder().addTestDevice("86EBBBCCD6C3B5BB6ACC49E06D651F0D").build()
//        adView.loadAd(adRequest)

    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroyView() {
        adapter.clear()
        classicGamesArr.clear()
        println("DESTROY")
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun addClassicGame(game: ClassicGame) {

        adapter.add(ClassicItem(game))

    }

    fun onMatchCreated() {
        SocketUser.socket.socket.on("matchCreated") { data ->
            println("CREATION")
            val json = JSON()
            var jsonObj = json.fromJson(data[0].toString())
            println(jsonObj)
            var type = jsonObj["type"]
            if (type == 0) {
                var games: JsonObject = jsonObj["content"] as JsonObject
                var arrayTeamA = ArrayList<MatchUserDetails>()
                var arrayTeamB = ArrayList<MatchUserDetails>()

                val classicGameobj = games
                var teamA = classicGameobj["teamA"] as JsonArray<JsonObject>
                var teamB = classicGameobj["teamB"] as JsonArray<JsonObject>
                for (i in 0 until teamA.size) {
                    val aNotParsed = parser.parse(teamA[i].toJsonString()).asJsonObject
                    val A = gson.fromJson(aNotParsed, MatchUserDetails::class.java)
                    arrayTeamA.add(A)
                }
                for (i in 0 until teamB.size) {
                    val bNotParsed = parser.parse(teamA[i].toJsonString()).asJsonObject
                    val B = gson.fromJson(bNotParsed, MatchUserDetails::class.java)
                    arrayTeamB.add(B)
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
                if (!arrayContainsGame(classic)) {
                    classicGamesArr.add(classic)
                    activity?.runOnUiThread(java.lang.Runnable {
                        addClassicGame(classic)
                    })
                }
            }

        }
    }

    fun arrayContainsGame(testGame: ClassicGame): Boolean {
        classicGamesArr.forEach {
            it
            if (it.name == testGame.name)
                return true
        }
        return false
    }

    fun getClassicMatches() {

        val json = JSON()


        SocketUser.socket.socket.on("getMatches") { data ->

            var jsonObj = json.fromJson(data[0].toString())
            var type = jsonObj["type"]
            if (type == 0) {
                var games = jsonObj["content"] as JsonArray<JsonObject>
                //var games = JSONArray(data[0].toString())

                var arrayTeamA = ArrayList<MatchUserDetails>()
                var arrayTeamB = ArrayList<MatchUserDetails>()

                activity?.runOnUiThread(java.lang.Runnable {
                    adapter.clear()
                })
                classicGamesArr.clear()
                for (i in 0 until games.size) {
                    val classicGameobj = (games[i])
                    var teamA = classicGameobj["teamA"] as JsonArray<JsonObject>
                    var teamB = classicGameobj["teamB"] as JsonArray<JsonObject>
                    for (i in 0 until teamA.size) {

                        val aNotParsed = parser.parse(teamA[i].toJsonString()).asJsonObject
                        val A = gson.fromJson(aNotParsed, MatchUserDetails::class.java)
                        arrayTeamA.add(A)
                    }
                    for (i in 0 until teamB.size) {

                        val bNotParsed = parser.parse(teamB[i].toJsonString()).asJsonObject
                        val B = gson.fromJson(bNotParsed, MatchUserDetails::class.java)
                        arrayTeamB.add(B)
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
                    if (!arrayContainsGame(classic)) {
                        classicGamesArr.add(classic)
                        activity?.runOnUiThread(java.lang.Runnable {
                            addClassicGame(classic)
                        })
                    }
                }
            }
        }

    }

    fun onEditMatch() {

        SocketUser.socket.socket.on("matchEdit") { data ->
            val json = JSON()
            SocketUser.socket.socket.emit("getMatches", json.toJson(GameRequest(0)))


        }
    }


}


class ClassicItem(var Cgame : ClassicGame): Item<ViewHolder>() {

    var gotIt = false
    val json = JSON()
    val gson = Gson()
    val parser = JsonParser()

    fun onJoin() {
        SocketUser.socket.socket.on("matchJoined") { data ->

            var jsonObj = json.fromJson(data[0].toString())
            println(jsonObj)
            var type = jsonObj["type"]
            if (type == 0) {
                var games: JsonObject = jsonObj["content"] as JsonObject
                var arrayTeamA = ArrayList<MatchUserDetails>()
                var arrayTeamB = ArrayList<MatchUserDetails>()

                val classicGameobj = games
                if (classicGameobj["name"] == Cgame.name) {
                    var teamA = classicGameobj["teamA"] as JsonArray<JsonObject>
                    var teamB = classicGameobj["teamB"] as JsonArray<JsonObject>
                    for (i in 0 until teamA.size) {
                        val aNotParsed = parser.parse(teamA[i].toJsonString()).asJsonObject
                        val A = gson.fromJson(aNotParsed, MatchUserDetails::class.java)
                        arrayTeamA.add(A)
                    }
                    for (i in 0 until teamB.size) {
                        val bNotParsed = parser.parse(teamB[i].toJsonString()).asJsonObject
                        val B = gson.fromJson(bNotParsed, MatchUserDetails::class.java)
                        arrayTeamB.add(B)
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
        } else {
            viewHolder.itemView.button_join_game.visibility = View.VISIBLE
        }

        viewHolder.itemView.game_name.setOnClickListener {

        }

        viewHolder.itemView.button_join_game.setOnClickListener { Activity ->

            CurrentGameMode.gameMode = 0
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
                        ?.navigate(R.id.action_fragment_classique_to_fragment_WaitingRoom)
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




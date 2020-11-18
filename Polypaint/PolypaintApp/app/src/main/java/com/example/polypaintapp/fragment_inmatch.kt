package com.example.polypaintapp

import android.Manifest
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder

import android.graphics.Color
import android.media.MediaPlayer
import android.widget.SeekBar
import androidx.core.view.isInvisible
import com.madrapps.pikolo.listeners.SimpleColorSelectionListener
import kotlinx.android.synthetic.main.fragment_in_match.*
import nl.dionsegijn.konfetti.models.Shape
import nl.dionsegijn.konfetti.models.Size
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import kotlinx.android.synthetic.main.dialog_team_infos.view.*
import kotlinx.android.synthetic.main.end_game.view.*
import kotlinx.android.synthetic.main.fragment_profilview.*
import kotlinx.android.synthetic.main.round_transition.*
import kotlinx.android.synthetic.main.round_transition.view.*
import kotlinx.android.synthetic.main.fragment_tutorial.*
import kotlinx.android.synthetic.main.fragment_tutorial.view.*
import kotlinx.android.synthetic.main.round_transition.view.message
import java.util.*
import kotlin.concurrent.schedule


class RoundInfos {
    constructor(pdrawer: String, pduration: Int, pGuessing: Boolean, pguessesLeft: Int) {
        drawer = pdrawer
        roundDuration = pduration
        isGuessing = pGuessing
        guessesLeft = pguessesLeft
    }

    val drawer: String
    val roundDuration: Int
    val isGuessing: Boolean
    val guessesLeft: Int
}

class Fragment_Inmatch : Fragment() {
    private val adapter = GroupAdapter<ViewHolder>()
    private var gotIt = false

    private var errorMediaPlayer : MediaPlayer? = null
    private var victoryMediaPlayer : MediaPlayer? = null

    private var teamAText = ""
    private var teamBText = ""

    private val gameId = WaitingRoom.name + CurrentGameMode.gameMode.toString()

    private var currentRound = 0
    var strokeColor = 0x000000

    private val adapterA = GroupAdapter<ViewHolder>()
    private val adapterB = GroupAdapter<ViewHolder>()
    var imageList = intArrayOf(R.drawable.tut001, R.drawable.tut002, R.drawable.tut003, R.drawable.tut004, R.drawable.tut005)

    private var soundOn: Boolean = true

    private var roundTransitionBuilder: AlertDialog.Builder? = null
    private var alertShow: AlertDialog? = null
    private var endDiagShow: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_in_match, container, false)
        //Back pressed Logic for fragment
        v.setFocusableInTouchMode(true)
        v.requestFocus()
        v.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        val json = JSON()
                        Channel.matchChannel = ""
                        SocketUser.matchChannels.remove(WaitingRoom.name + CurrentGameMode.gameMode.toString())
                        Channel.isAMatch = false
                        val request = MatchRequest(User.username, WaitingRoom.name + CurrentGameMode.gameMode.toString())
                        //SocketUser.joinedChannels.remove(WaitingRoom.name + CurrentGameMode.gameMode.toString())
                        SocketUser.socket.leaveInMatch(json.toJson(request))
                        //SocketUser.joinedChannels.remove(WaitingRoom.name + CurrentGameMode.gameMode.toString())
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
        //SocketUser.joinedChannels.remove(WaitingRoom.name + CurrentGameMode.gameMode.toString())
        SocketUser.socket.leaveInMatch(json.toJson(request))
        //SocketUser.joinedChannels.remove(WaitingRoom.name + CurrentGameMode.gameMode.toString())
        val intent = Intent(context, Game_Modes::class.java)
        startActivity(intent)
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        adapter.clear()
        errorMediaPlayer = MediaPlayer.create(activity!!.applicationContext, R.raw.error2)
        victoryMediaPlayer = MediaPlayer.create(activity!!.applicationContext, R.raw.victoire)
        getImages()

        quitMatch.setOnClickListener {
            SocketUser.matchChannels.remove(WaitingRoom.name + CurrentGameMode.gameMode.toString())
            val intent = Intent(context, Game_Modes::class.java)
            context!!.startActivity(intent)
        }

        if(CurrentGameMode.gameMode == GameModes.Duel.ordinal) {
            guessesLeft.visibility = View.INVISIBLE
            separateur.visibility = View.INVISIBLE
        }

        buttonHelp.setOnClickListener{
            val dialogBuilder = android.app.AlertDialog.Builder(activity!!)
            val inflater = this.layoutInflater
            val dialogView = inflater.inflate(R.layout.fragment_tutorial, null)
            dialogBuilder.setView(dialogView)

            var dialog = dialogBuilder.create();
            dialogView.previousButton.setOnClickListener{
                dialogView.tutorialViewFlipper.setInAnimation(activity!!, android.R.anim.slide_in_left)
                dialogView.tutorialViewFlipper.setOutAnimation(activity!!, android.R.anim.slide_out_right)
                dialog.tutorialViewFlipper.showPrevious()
            }
            dialogView.nextButton.setOnClickListener{
                dialogView.tutorialViewFlipper.setInAnimation(activity!!, android.R.anim.slide_in_left)
                dialogView.tutorialViewFlipper.setOutAnimation(activity!!, android.R.anim.slide_out_right)
                dialogView.tutorialViewFlipper.showNext()
            }
            if (dialogView.tutorialViewFlipper != null) {
                for (image in imageList) {
                    val imageView = ImageView(activity!!)
                    val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    layoutParams.setMargins(30, 30, 30, 30)
                    layoutParams.gravity = Gravity.CENTER
                    imageView.layoutParams = layoutParams
                    imageView.setImageResource(image)
                    dialogView.tutorialViewFlipper.addView(imageView)
                }
            }
            dialog.show()
        }

        Game_infos.setOnClickListener {

                var mDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_team_infos, null)

                adapterA.clear()
                adapterB.clear()
                mDialogView.teamA_infos.adapter = adapterA
                mDialogView.teamB_infos.adapter = adapterB

                // Initialize a new instance of
                val builder = AlertDialog.Builder(activity!!)
                    .setTitle("Match Informations")
                    .setView(mDialogView)
            when {
                WaitingRoom.Game is ClassicGame -> {
                    val game = WaitingRoom.Game as ClassicGame

                    for (users in game.teamA) {
                        if(PlayersInfo.playersNames.indexOf(users.username) >= 0 && PlayersInfo.playersNames.indexOf(users.username) < PlayersInfo.url.size)
                        addUserA(users.username, PlayersInfo.url.get(PlayersInfo.playersNames.indexOf(users.username)),true)
                    }

                    for (users in game.teamB) {
                        if(PlayersInfo.playersNames.indexOf(users.username) >= 0 && PlayersInfo.playersNames.indexOf(users.username) < PlayersInfo.url.size)
                        addUserB(users.username, PlayersInfo.url.get(PlayersInfo.playersNames.indexOf(users.username)),true)
                    }
                }
                    WaitingRoom.Game is DuelGame -> {
                        val game = WaitingRoom.Game as DuelGame
                        if(PlayersInfo.playersNames.indexOf(game.player1.username) >= 0 && PlayersInfo.playersNames.indexOf(game.player1.username) < PlayersInfo.url.size)
                        addUserA(game.player1.username, PlayersInfo.url.get(PlayersInfo.playersNames.indexOf(game.player1.username)), true)
                        if(PlayersInfo.playersNames.indexOf(game.player1.username) >= 0 && PlayersInfo.playersNames.indexOf(game.player2.username) < PlayersInfo.url.size)
                        addUserB(game.player2.username, PlayersInfo.url.get(PlayersInfo.playersNames.indexOf(game.player2.username)), true)

                    }
                    WaitingRoom.Game is CoopGame -> {
                        val game = WaitingRoom.Game as CoopGame
                        for (users in game.players) {
                            if(PlayersInfo.playersNames.indexOf(users.username) >= 0 && PlayersInfo.playersNames.indexOf(users.username) < PlayersInfo.url.size)
                            addUserA(users.username, PlayersInfo.url.get(PlayersInfo.playersNames.indexOf(users.username)),true)
                        }
                        mDialogView.teamA_name.visibility = View.INVISIBLE
                        mDialogView.teamB_name.visibility = View.INVISIBLE
                        mDialogView.teamB_infos.visibility = View.INVISIBLE
                    }
                    WaitingRoom.Game is SoloGame -> {
                        val game = WaitingRoom.Game as SoloGame
                        PlayersInfo.playersNames.add(User.username)
                        PlayersInfo.playersNames.add(game.aiplayer)
                        sendImagessocket()
                        while(!gotIt){}

                        if(PlayersInfo.playersNames.indexOf(User.username) >= 0 && PlayersInfo.playersNames.indexOf(User.username) < PlayersInfo.url.size)
                            addUserA(User.username, PlayersInfo.url.get(PlayersInfo.playersNames.indexOf(User.username)),true)
                        if(PlayersInfo.playersNames.indexOf(game.aiplayer) >= 0 && PlayersInfo.playersNames.indexOf(game.aiplayer) < PlayersInfo.url.size)
                            addUserA(game.aiplayer, PlayersInfo.url.get(PlayersInfo.playersNames.indexOf(game.aiplayer)), true)
                        mDialogView.teamA_name.visibility = View.INVISIBLE
                        mDialogView.teamB_infos.visibility = View.INVISIBLE
                        mDialogView.teamB_name.visibility = View.INVISIBLE

                    }
            }

                builder.show()
        }

        sound.setOnClickListener {
            if(soundOn){
                soundOn = false
                val speaker = BitmapFactory.decodeResource(this.resources, R.drawable.speaker_off)
                sound.setImageBitmap(speaker)
                victoryMediaPlayer?.setVolume(0f, 0f)
                errorMediaPlayer?.setVolume(0f, 0f)
            } else {
                soundOn = true
                val speaker = BitmapFactory.decodeResource(this.resources, R.drawable.speaker_on)
                sound.setImageBitmap(speaker)
                victoryMediaPlayer?.setVolume(1f,1f)
                errorMediaPlayer?.setVolume(1f, 1f)
            }
        }

        setGameInfos()
        onSendGuess()
        onBadGuess()
        onReply()
        onRoundStart()
        onRoundStartDrawer()
        onRoundEnd()
        onMatchEnd()
        initPaint()
        onUpdateTimeRemaining()
        onAskHint()
    }

    fun showRoundTransition(message : String){
        val tmp = message.replace("\"", "")
        var mRoundTransitionDialog = LayoutInflater.from(context).inflate(R.layout.round_transition, null)

        val builder = AlertDialog.Builder(activity!!)
            .setView(mRoundTransitionDialog)

        mRoundTransitionDialog.message.text = tmp

        alertShow = builder.show()
    }

    fun closeRoundTransition(){
        alertShow?.dismiss()
    }

    fun showMatchEndDiag(reason: String?){
        var diag = LayoutInflater.from(context).inflate(R.layout.end_game, null)

        val builder = AlertDialog.Builder(activity!!)
            .setView(diag)

        diag.message.text = reason?.replace("\"", "")
        diag.btn_ok.setOnClickListener{
            closeMatchtEndDiag()
        }
        endDiagShow = builder.show()
    }

    fun closeMatchtEndDiag(){
        endDiagShow?.dismiss()
    }

    fun sendImagessocket() {
        val json = JSON()
        val players = players(PlayersInfo.playersNames)
        SocketUser.socket.getImages(json.toJson(players))

    }

    fun getImages() {
        SocketUser.socket.socket.on("giveAvatarUrl") {data->

            val informations = JsonParser().parse(data[0].toString()).asJsonObject
            var players = informations["players"] as com.google.gson.JsonArray
            var url = informations["url"] as com.google.gson.JsonArray

            for(names in players) {
                PlayersInfo.playersNames.add(names.toString())
            }

            for(names in url) {
                PlayersInfo.url.add(names.toString())
            }

            gotIt = true

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

    fun onAskHint() {
        hint.setOnClickListener {
            val json = JSON()
            SocketUser.socket.socket.emit("send", json.toJson(Message(User.username, "hint!", 0, WaitingRoom.name + CurrentGameMode.gameMode.toString(), "")))
        }
    }

    override fun onDestroyView() {
        val json = JSON()
        val request = MatchRequest(User.username, WaitingRoom.name + CurrentGameMode.gameMode.toString())
        SocketUser.socket.leaveInMatch(json.toJson(request))
        //SocketUser.joinedChannels.remove(WaitingRoom.name)
        //SocketUser.joinedChannels.remove(WaitingRoom.name + CurrentGameMode.gameMode.toString())
        super.onDestroyView()
    }

    private fun setGameInfos() {
        Game_name.text = WaitingRoom.name
        when {
            WaitingRoom.Game is ClassicGame -> {
                val game = WaitingRoom.Game as ClassicGame
                nbRoundsInmatch.text = "${currentRound}/${game.nbRounds} rounds"
                teamAText = "Team A: \n"
                teamBText = "Team B: \n"
                game.teamA.forEach { member ->
                    teamAText = "$teamAText\t $member\n"
                }
                game.teamB.forEach { member ->
                    teamBText = "$teamBText\t $member\n"
                }
            }
            WaitingRoom.Game is DuelGame -> {
                val game = WaitingRoom.Game as DuelGame
                nbRoundsInmatch.text = "${currentRound}/${game.nbRounds} rounds"
                teamAText = "Team A: \n"
                teamBText = "Team B: \n"
                teamAText = "${game.player1} vs ${game.player2}"
                teamBText = ""
            }
            WaitingRoom.Game is CoopGame -> {
                val game = WaitingRoom.Game as CoopGame
                nbRoundsInmatch.text = "Round ${currentRound}"
                teamBText = "Ai player is ${game.aiplayer}"
                teamAText = "Current players: \n"
                game.players.forEach { member ->
                    teamAText = "${teamAText}\t $member\n"
                }
            }
            WaitingRoom.Game is SoloGame -> {
                val game = WaitingRoom.Game as SoloGame
                nbRoundsInmatch.text = " Round ${currentRound}"
                teamAText = "Ai player is ${game.aiplayer}"
                teamBText = ""

            }
        }
    }

    private fun onRoundEnd() {
        SocketUser.socket.socket.on("roundEnd") { data ->
            activity?.runOnUiThread {
                //wordToDraw.visibility = View.INVISIBLE
                val roundEndJson = JsonParser().parse(data[0].toString()).asJsonObject
                val roundEndMessage = roundEndJson.get("message").toString()
                timeRemainingRound.text = "The round is finish"
                // Toast.makeText(activity, "The round ended, $roundEndMessage", Toast.LENGTH_SHORT).show()
                val teamAPoints = roundEndJson.get("teamA").toString().toInt()
                val teamBPoints = roundEndJson.get("teamB")?.toString()?.toInt()
                teamA.text = "Team A: \n\t$teamAPoints points"
                if(teamBPoints != null)
                    teamB.text="Team B: \n\t$teamBPoints points"

                clearCanvas()
                showRoundTransition(roundEndMessage)
            }
        }
    }

    private fun onRoundStartDrawer() {
        SocketUser.socket.socket.on("roundStartDrawer") { data ->

            activity?.runOnUiThread {
                closeRoundTransition()
                clearCanvas()
                showTools()
                //wordToDraw.visibility = View.VISIBLE
                val roundJson = JsonParser().parse(data[0].toString()).asJsonObject
                val word = roundJson.get("wordToDraw").toString()
                wordToDraw.text = "Word to draw: $word"
            }
        }
    }

    private fun onMatchEnd() {
        SocketUser.socket.socket.on("matchEnd") { data ->
            activity?.runOnUiThread {
                closeRoundTransition()
                val matchEndJson = JsonParser().parse(data[0].toString()).asJsonObject
                val reason = matchEndJson.get("reason")?.toString()
                val wins = matchEndJson.get("wins")?.toString()?.toBoolean()
                if (reason != null && wins != null && WaitingRoom.Game !is SoloGame && WaitingRoom.Game !is CoopGame) {
                    if (wins!!) {
                        victoryMediaPlayer?.start()
                        viewKonfetti.build()
                            .addColors(Color.YELLOW, Color.GREEN, Color.MAGENTA)
                            .setDirection(0.0, 359.0)
                            .setSpeed(1f, 5f)
                            .setFadeOutEnabled(true)
                            .setTimeToLive(2000L)
                            .addShapes(Shape.RECT, Shape.CIRCLE)
                            .addSizes(Size(12))
                            .setPosition(-50f, viewKonfetti.width + 50f, -50f, -50f)
                            .streamFor(300, 5000L)
                    }
                }
                // Toast.makeText(activity, "The match has ended, $reason", Toast.LENGTH_SHORT).show()
                showMatchEndDiag(reason)
            }
            val json = JSON()
            val request = MatchRequest(User.username, WaitingRoom.name + CurrentGameMode.gameMode.toString())
            //SocketUser.socket.leaveInMatch(json.toJson(request))
            //SocketUser.joinedChannels.remove(WaitingRoom.name)
        }
    }

    private fun onUpdateTimeRemaining() {
        SocketUser.socket.socket.on("timerUpdate") { data ->
            activity?.runOnUiThread {
                val time = JsonParser().parse(data[0].toString()).asJsonObject
                val timeRound = time.get("roundTime")
                val matchTime = time.get("matchTime").toString().toInt()
                if (timeRound.toString().toInt() <= 0) {
                    timeRemainingRound?.text = "There is no time remaining"
                    guessesLeft?.text = "Guesses left: 0"
                    enterGuess?.visibility = View.GONE
                    sendGuess?.visibility = View.GONE
                } else {
                    timeRemainingRound?.text = "Time Remaining for this round: $timeRound"
                    if(maxTime != null) {
                        maxTime.text =
                            "Match max time: ${matchTime / 60} min and ${matchTime % 60} sec"
                    }
                }
            }
        }
    }


    private fun onRoundStart() {
        SocketUser.socket.socket.on("roundStart") { data ->
            val gson = Gson()
            val roundJson = JsonParser().parse(data[0].toString()).asJsonObject
            val round = gson.fromJson(roundJson, RoundInfos::class.java)
            guessesLeft?.text = "Guesses left: ${round.guessesLeft}"

            activity?.runOnUiThread {
                closeRoundTransition()
                currentRound++
                setGameInfos()
                if (round.isGuessing) {
                    enterGuess.visibility = View.VISIBLE
                    sendGuess.visibility = View.VISIBLE
                } else {
                    enterGuess.visibility = View.GONE
                    sendGuess.visibility = View.GONE
                }
                if (round.drawer == User.username) {
                    showTools()
                } else {
                    removeTools()
                }
            }
        }
    }

    private fun onSendGuess() {
        enterGuess.setOnKeyListener (View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                sendGuess.callOnClick()
                return@OnKeyListener true
            }
            false
        })

        sendGuess.setOnClickListener {
            val guess = enterGuess.text.toString().trim()
            if (guess.isEmpty()) {
                Toast.makeText(activity, "Please enter a guess", Toast.LENGTH_SHORT).show()
            } else {
                val guessJson = com.google.gson.JsonObject()
                guessJson.addProperty("guess", guess)
                guessJson.addProperty("id", gameId)
                SocketUser.socket.socket.emit("guess", guessJson)
                enterGuess.setText("")
                (activity as Game_Modes).hideKeyboard()
            }
        }
    }

    private fun onBadGuess() {
        SocketUser.socket.socket.on("badGuess") { data ->
            activity?.runOnUiThread {
                errorMediaPlayer?.start()
                Toast.makeText(activity, "Bad Guess!", Toast.LENGTH_SHORT).show()
                val numberTriesJson = JsonParser().parse(data[0].toString()).asJsonObject
                val nbTries = numberTriesJson.get("triesLeft").toString().toInt()
                guessesLeft.text = "Guesses left: $nbTries"
                YoYo.with(Techniques.Wobble).duration(500).playOn(enterGuess)
            }
        }
    }

    fun onReply() {
        SocketUser.socket.socket.on("reply") { data ->
            activity?.runOnUiThread {
                val numberTriesJson = JsonParser().parse(data[0].toString()).asJsonObject
                val isGuessing = numberTriesJson.get("isGuessing").toString().toBoolean()
                if (isGuessing != null) {
                    if (isGuessing) {
                        Toast.makeText(activity, "It's your time to guess", Toast.LENGTH_SHORT).show()
                        enterGuess?.visibility = View.VISIBLE
                        sendGuess?.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(activity, "It's time for your opponent to guess", Toast.LENGTH_SHORT).show()
                        enterGuess?.visibility = View.INVISIBLE
                        sendGuess?.visibility = View.INVISIBLE
                    }
                }
            }
        }
    }
    fun startSetup() {
        seekBar_width.visibility = View.GONE
        color_picker.visibility = View.GONE
        stroke_change_square.isInvisible = false
        stroke_change_round.isInvisible = true
        draw_view.setStrokeWidth(50.0f)
    }

    fun changeStroke(round : Boolean) {
        if(round) {
            draw_view.changeStrokeCapToRound()
        }
        else {
            draw_view.changeStrokeCapToSquare()
        }
    }
    fun removeTools() {
        draw_tools.visibility = View.INVISIBLE
        seekBar_width.visibility = View.GONE
        color_picker.visibility = View.GONE
        draw_view.deactivateDraw()
    }

    fun showTools() {
        draw_tools.visibility = View.VISIBLE
        draw_view.activateDraw()
    }

    fun clearCanvas() {
        draw_view.clearCanvas()
    }

    fun initPaint() {

        val room = WaitingRoom.name + CurrentGameMode.gameMode
        draw_view.setSocket(SocketUser.socket.socket)
        draw_view.setRoom(room)
        draw_view.setStrokeWidth(50.0f)


        SocketUser.socket.socket.on("eraseStroke") {data ->
            val roomStroke = JsonParser().parse(data[0].toString()).asJsonObject.get("room").asString
            val id = JsonParser().parse(data[0].toString()).asJsonObject.get("id").asInt
            if (roomStroke == room) {
                draw_view.onEraseStroke(id)
            }
        }

        draw_view.setStrokeWidth(50.0f)
        //SocketUser.onMessage(this)

        color_picker.setColorSelectionListener(object : SimpleColorSelectionListener() {
            override fun onColorSelected(color: Int) {
                // Do whatever you want with the color
                draw_view.setColor(color)
                strokeColor = color
            }
        })
        startSetup()
        image_effaceTrait.setOnClickListener {
            color_picker.visibility = View.GONE
            seekBar_width.visibility = View.GONE
            activateDraw.isSelected = false
            image_draw_eraser.isSelected = false
            it.isSelected = true
            draw_view.mIsErasingStroke = true
        }
        activateDraw.isSelected = true
        activateDraw.setOnClickListener {
            draw_view.setColor(strokeColor)
            color_picker.visibility = View.GONE
            draw_view.mIsErasingStroke = false
            seekBar_width.visibility = View.GONE
            image_effaceTrait.isSelected = false
            image_draw_eraser.isSelected = false
            it.isSelected = true
        }

        save_image.setOnClickListener {
            color_picker.visibility = View.GONE
            if (ContextCompat.checkSelfPermission(activity!!,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity!!,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 4)
            } else {
                draw_view.saveImageToExternalStorage(draw_view.getBitmap())
            }
        }

        stroke_change_round.setOnClickListener {


            color_picker.visibility = View.GONE
            seekBar_width.visibility = View.GONE
            image_effaceTrait.isSelected = false
            if (draw_view.mIsErasingStroke) {
                activateDraw.isSelected = true
                draw_view.setColor(strokeColor)
            }
            draw_view.mIsErasingStroke = false
            changeStroke(true)

            stroke_change_square.isInvisible = false
            stroke_change_round.isInvisible = true
        }

        stroke_change_square.setOnClickListener {

            color_picker.visibility = View.GONE
            seekBar_width.visibility = View.GONE
            image_effaceTrait.isSelected = false
            if (draw_view.mIsErasingStroke) {
                activateDraw.isSelected = true
                draw_view.setColor(strokeColor)
            }
            draw_view.mIsErasingStroke = false
            changeStroke(false)


            stroke_change_square.isInvisible = true
            stroke_change_round.isInvisible = false
        }


        image_draw_eraser.setOnClickListener {

            color_picker.visibility = View.GONE
            activateDraw.isSelected = false
            seekBar_width.visibility = View.GONE
            it.isSelected = true
            image_effaceTrait.isSelected = false
            draw_view.mIsErasingStroke = false
            draw_view.setColor(0xFFFFFF)
        }

        image_draw_width.setOnClickListener {

            draw_view.mIsErasingStroke = false
            draw_view.setColor(strokeColor)
            color_picker.visibility = View.GONE
            if(seekBar_width.visibility == View.VISIBLE)
                seekBar_width.visibility = View.GONE
            else
                seekBar_width.visibility = View.VISIBLE

        }

        image_draw_color.setOnClickListener {
            draw_view.mIsErasingStroke = false
            seekBar_width.visibility = View.GONE
            if(color_picker.visibility == View.VISIBLE)
                color_picker.visibility = View.GONE
            else
                color_picker.visibility = View.VISIBLE
        }

        seekBar_width.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                draw_view.setStrokeWidth(progress.toFloat()+1)
                draw_view.mIsErasingStroke = true
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
}
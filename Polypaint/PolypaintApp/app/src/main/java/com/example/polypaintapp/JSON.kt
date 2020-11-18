package com.example.polypaintapp
import com.beust.klaxon.Parser
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.json.JSONObject
import java.lang.StringBuilder
import kotlin.reflect.typeOf

class JSON {

    constructor(){
        this.Json = JsonObject()
        this.Gson = Gson()
    }

    fun toJson(message: Message): JsonObject {

        Json.addProperty("sender", message.sender)
        Json.addProperty("text", message.text)
        Json.addProperty("time", message.date)
        Json.addProperty("room", message.room)

        return Json

    }

    fun toJson(players: players): String {

       return Gson.toJson(players)


    }

    fun toJson(request: GameRequest): JsonObject {

        Json.addProperty("type", request.type)


        return Json

    }

    fun toJson(request: MatchRequest): JsonObject {

        Json.addProperty("username", request.username)
        Json.addProperty("id", request.id)

        return Json

    }

    fun toJson(match: Match): JsonObject {
        Json.addProperty("name", match.name)
        Json.addProperty("nbRounds", match.nbRounds)
        Json.addProperty("type", match.type)
        Json.addProperty("creator", match.creator)
        Json.addProperty("playerCount", match.playerCount)

        return Json

    }

    fun toJson(room: Room): JsonObject {

        Json.addProperty("username", room.username)
        Json.addProperty("room", room.room)


        return Json

    }

    fun fromJson(obj: String): com.beust.klaxon.JsonObject {
        val parser: Parser = Parser.default()
        val stringBuilder: StringBuilder = StringBuilder(obj)
        val message: com.beust.klaxon.JsonObject = parser.parse(stringBuilder) as com.beust.klaxon.JsonObject
        return message
    }

    fun fromJsonUserInfos(obj: String): Infos{
        val infos = Gson.fromJson<Infos>(obj, Infos::class.java)

        return infos
    }

    fun toJson(account: Account): String {
        return Gson.toJson(account)
    }


    fun toJson(username: String, password: String, socketId: String): String {
        val user = userTmpInSignIn(username, password, socketId)
        println(user)
        return Gson.toJson(user)
    }

    fun toJson(editBody: EditBody): String{
        return Gson.toJson(editBody)
    }

    private val Json: JsonObject
    private val Gson: Gson
}

class userTmpInSignIn {
    constructor(username: String, password: String, socketId: String){
        this.username = username
        this.password = password
        this.socketId = socketId
    }
    var username: String = ""
    var password: String = ""
    var socketId: String = ""
}

class Infos{
    constructor(firstName: String, lastName: String, username: String, avatarUrl: String, stats: Stats){
        this.firstName = firstName
        this.lastName= lastName
        this.username = username
        this.avatarUrl = avatarUrl
        this.stats = stats
    }

    var firstName: String = ""
    var lastName: String = ""
    var username: String = ""
    var avatarUrl: String = ""
    var stats: Stats
}

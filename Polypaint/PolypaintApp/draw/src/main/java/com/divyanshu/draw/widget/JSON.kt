package com.example.polypaintapp
import com.beust.klaxon.Parser
import com.divyanshu.draw.widget.StrokeAttributes
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

    fun fromJson(obj: String): com.beust.klaxon.JsonObject {
        val parser: Parser = Parser.default()
        val stringBuilder: StringBuilder = StringBuilder(obj)
        val message: com.beust.klaxon.JsonObject = parser.parse(stringBuilder) as com.beust.klaxon.JsonObject
        return message
    }


    fun toStrokeAttributes(drawAttributes: StrokeAttributes): JsonObject {

        Json.addProperty("StylusType", drawAttributes.stylusType)
        //Json.addProperty("rgb", drawAttributes.rgb)
        Json.addProperty("width", drawAttributes.width)
        Json.addProperty("height", drawAttributes.height)

//        val stylusType: Int
//        val rgb: ArrayList<Byte>
//        val width: Float
//        val height: Float

        return Json

    }




    fun toJson(username: String, password: String): String {
        val user = userTmp(username, password)
        println(user)
        return Gson.toJson(user)
    }

    private val Json: JsonObject
    private val Gson: Gson
}

class userTmp {
    constructor(username: String, password: String){
        this.username = username
        this.password = password
    }
    var username: String = ""
    var password: String = ""
}

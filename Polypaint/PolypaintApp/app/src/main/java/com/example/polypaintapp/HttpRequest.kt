package com.example.polypaintapp

import android.content.Context
import android.content.Intent
import com.google.gson.Gson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import java.io.IOException
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.*
import kotlin.concurrent.schedule
import kotlin.concurrent.thread

class HttpRequest {

    constructor() {
        //this.client = OkHttpClient()
    }
    fun signUpRequest(account: Account, callbackSignUp: CallbackSignUp){
        val url = IP
        val signUpUrl = url + "/account/signUp"

        val jsonObj = this.json.toJson(account)

        val body = jsonObj.toRequestBody(mediaType)

        val accountRequest = Request.Builder().url(signUpUrl).post(body).build()

        thread {
            this.client.newCall(accountRequest).enqueue(callbackSignUp)
        }
    }

    fun signInRequest(user: User, callbackSignIn: CallbackSignIn, fromSignUp: Boolean) {
        val url = IP
        val signInUrl = url + "/account/signIn"
        val jsonObj = this.json.toJson(user.username, user.password, user.socketId)
        val body = jsonObj.toRequestBody(mediaType)

        val userRequest = Request.Builder().url(signInUrl).post(body).build()

        thread{
            val client = this.client
            if(fromSignUp){
                Timer().schedule(1000){
                    client.newCall(userRequest).enqueue(callbackSignIn)
                }
            } else {
                client.newCall(userRequest).enqueue(callbackSignIn)
            }
        }

    }

    fun getUserInfosRequest(user: User, context: Context, fromSignIn: Boolean) {
        val url = IP
        val getInfoUrl = url + "/account/infos/" + user.username
        val userRequest = Request.Builder().url(getInfoUrl).get().build()

        thread{
            // this.client.newCall(userRequest).enqueue(callbackGetUserInfos)

            val response = this.client.newCall(userRequest).execute()

            val tmp = response.body?.string()
            val userInfos = json.fromJsonUserInfos(tmp as String)

            User.firstName = userInfos.firstName
            User.lastName = userInfos.lastName
            User.stats = userInfos.stats

            if(userInfos.avatarUrl == null){
                User.avatarUrl = "noPhoto"
            } else {
                User.avatarUrl = userInfos.avatarUrl
            }

            if(fromSignIn) {
                val intent = Intent(context, LobbyActivity::class.java)
                context.startActivity(intent)
            }
        }
    }

    fun putUserFirstName(firstName: String, callbackFirstName: CallbackFirstName) {
        val url = IP
        val getInfoUrl = url + "/account/editFirstName"

        val editBody = EditBody(User.username, firstName)
        val jsonObj = this.json.toJson(editBody)
        val body = jsonObj.toRequestBody(mediaType)

        val userRequest = Request.Builder().url(getInfoUrl).put(body).build()

        thread{
            this.client.newCall(userRequest).enqueue(callbackFirstName)
        }
    }

    fun putUserLastName(lastName: String, callbackLastName: CallbackLastName) {
        val url = IP
        val getInfoUrl = url + "/account/editLastName"

        val editBody = EditBody(User.username, lastName)
        val jsonObj = this.json.toJson(editBody)
        val body = jsonObj.toRequestBody(mediaType)

        val userRequest = Request.Builder().url(getInfoUrl).put(body).build()

        thread{
            this.client.newCall(userRequest).enqueue(callbackLastName)
        }
    }


//    fun putUserUsername(username: String, callbackUsername: CallbackUsername) {
//        val url = IP
//        val getInfoUrl = url + "/account/editUsername"
//
//        val editBody = EditBody(User.username, username)
//        val jsonObj = this.json.toJson(editBody)
//        val body = jsonObj.toRequestBody(mediaType)
//
//        val userRequest = Request.Builder().url(getInfoUrl).put(body).build()
//
//        thread{
//            this.client.newCall(userRequest).enqueue(callbackUsername)
//        }
//    }

    fun putUserAvatar(array: ByteArray, callbackAvatar: CallbackAvatar){
        val url = IP
        val getInfoUrl = url + "/account/editAvatar"

        val editBody = EditBody(User.username, array)
        val jsonObj = this.json.toJson(editBody)
        val body = jsonObj.toRequestBody(mediaType)

        val userRequest = Request.Builder().url(getInfoUrl).put(body).build()

        thread{
            this.client.newCall(userRequest).enqueue(callbackAvatar)
        }
    }

    fun putUserPassword(newPassword: String, callbackPassword: CallbackPassword) {
        val url = IP
        val getInfoUrl = url + "/account/editPassword"

        val editBody = EditBody(User.username, newPassword)
        val jsonObj = this.json.toJson(editBody)
        val body = jsonObj.toRequestBody(mediaType)

        val userRequest = Request.Builder().url(getInfoUrl).put(body).build()

        thread{
            this.client.newCall(userRequest).enqueue(callbackPassword)
        }
    }


    private val mediaType = "application/json; charset=utf-8".toMediaType()
    private val json: JSON = JSON()
    private var client = OkHttpClient()

}

class EditBody{

    constructor(username: String, dataToEdit: Any){
        this.username = username
        this.dataToEdit = dataToEdit
    }

    val username: String
    val dataToEdit: Any
}


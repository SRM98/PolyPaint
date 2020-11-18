package com.example.polypaintapp

import kotlinx.android.synthetic.main.fragment_signin.*

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.fragment_signin.view.*
import kotlinx.android.synthetic.main.fragment_waiting_room.view.*
import kotlinx.coroutines.delay
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.util.*
import kotlin.concurrent.schedule
import kotlin.concurrent.thread


class FragmentSignIn : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_signin, container, false)
        v.setFocusableInTouchMode(true)
        v.requestFocus()
        v.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
                if (event.getAction() === KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        val intent = Intent(Intent.ACTION_MAIN)
                        intent.addCategory(Intent.CATEGORY_HOME)
                        startActivity(intent)
                }
                }
                return false
            }
        })
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spin_waiting.visibility = View.INVISIBLE

        val signedUp = arguments?.getBoolean("signedUp")
        if(signedUp != null){
            if(signedUp == true){
                val username = arguments?.getString("username")
                val password = arguments?.getString("password")
                User.username = username as String
                User.password = password as String
                User.socketId = SocketUser.socket.id
                spin_waiting.visibility = View.VISIBLE

                signIn(true)
            }
        }

        signin.setOnClickListener {
            User.username = signinUsername.text.toString()
            User.password = signinPassword.text.toString()
            User.socketId = SocketUser.socket.id
            signIn(false)
        }

        signup.setOnClickListener {view ->

            view.findNavController().navigate(R.id.action_fragmentSignIn_to_fragmentSignUp)

        }

    }

    private fun signIn(fromSignUp: Boolean) {
        val http = HttpRequest()
        context?.let {context ->
            val callbackSignIn = CallbackSignIn(context, view!!)
            if (User.socketId != "") {
                http.signInRequest(User, callbackSignIn, fromSignUp)
            } else {
                SocketUser.socket.socket.on("connection") { data ->
                    User.socketId = data[0].toString()
                    http.signInRequest(User, callbackSignIn, fromSignUp)
                }
            }
        }
    }
}

class CallbackSignIn(private val context: Context, private val view: View): Callback{

    override fun onResponse(call: Call, response: Response) {
        println("Server Response for signIn")
        this.responseState = true
        responseBody = response.message

        if (responseBody == "OK") {
            (context as AppCompatActivity).runOnUiThread {
                //Getting user infos
                val http = HttpRequest()
                http.getUserInfosRequest(User, context, true)
            }
            context.runOnUiThread {
                view.spin_waiting.visibility = View.INVISIBLE
            }

        } else {
            (context as AppCompatActivity).runOnUiThread {
                Toast.makeText(context, response.body?.string(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onFailure(call: Call, err: IOException) {
        println(err.toString())
        responseState = false

        (context as AppCompatActivity).runOnUiThread {
            Toast.makeText(context, "Connection failed to server!", Toast.LENGTH_SHORT).show()
        }

        val throwable = Throwable(err.toString())
        Thread.setDefaultUncaughtExceptionHandler { t, e -> System.err.println(e.message) }
        throw throwable
    }

    var responseState: Boolean = false
    var responseBody: String? = ""
}


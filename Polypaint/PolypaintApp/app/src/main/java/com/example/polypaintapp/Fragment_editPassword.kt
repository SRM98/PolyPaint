package com.example.polypaintapp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.mikepenz.materialdrawer.Drawer
import kotlinx.android.synthetic.main.fragment_editpassword.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class FragmentEditPassword(private val supportFragmentManager: FragmentManager): Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_editpassword, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_apply_password_change.setOnClickListener {
            changePassword()
        }
    }

    fun changePassword() {

        if(edit_oldPassword.text.toString() == User.password){
            //Good old password
            if(edit_newPassword.text.toString() == edit_newPassword_confirm.text.toString()){
                // User.password = edit_newPassword.text.toString()
                sendPasswordChangeRequest(edit_newPassword.text.toString())
            } else {
                Toast.makeText(context, "Confirm your password correctly!", Toast.LENGTH_SHORT).show()
            }
        } else{
            //Wrong old password
            Toast.makeText(context, "Wrong password, please enter your old password correctly!", Toast.LENGTH_SHORT).show()
        }
    }

    fun sendPasswordChangeRequest(newPassword: String) {
        val http = HttpRequest()
        context?.let {context ->
            val callbackPassword = CallbackPassword(context, newPassword, supportFragmentManager)
            http.putUserPassword(newPassword, callbackPassword)
        }
    }
}

class CallbackPassword(private val context: Context, val newPassword: String, val supportFragmentManager: FragmentManager): Callback {

    override fun onResponse(call: Call, response: Response) {
        this.responseState = true
        responseBody = response.message

        if (responseBody == "OK") {
            User.password = newPassword

            (context as AppCompatActivity).runOnUiThread {
                Toast.makeText(context, "Password changed succesfully!", Toast.LENGTH_SHORT).show()
            }

            val transaction = supportFragmentManager.beginTransaction().apply {
                replace(R.id.nav_host_fragment_lobby, FragmentProfil(supportFragmentManager))
                addToBackStack(null)
            }
            transaction.commit()

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


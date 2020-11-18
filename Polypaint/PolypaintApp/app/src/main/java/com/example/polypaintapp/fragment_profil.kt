package com.example.polypaintapp

import android.app.Activity
import kotlinx.android.synthetic.main.fragment_lobby_main.*

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.android.synthetic.main.activity_lobby.*
import kotlinx.android.synthetic.main.fragment_profilview.*
import kotlinx.android.synthetic.main.fragment_profilview.view.*
import kotlinx.android.synthetic.main.fragment_signup.*
import kotlinx.android.synthetic.main.password_dialog.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URL
import java.util.*
import kotlin.concurrent.schedule
import kotlin.concurrent.thread


class FragmentProfil(private val supportFragmentManager: FragmentManager) : Fragment() {

    var modifInAvatar: Boolean = false
    val PICKFILE_RESULT_CODE = 1
    var fileUri: Uri = "".toUri()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        getUserInfos()
        return inflater.inflate(R.layout.fragment_profilview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as LobbyActivity).nameZoneLobby.text = "PROFIL"

        try {
            applyStats()
        } catch (e: TypeCastException){
            Toast.makeText(context, "Stats couldn't load. Sorry!", Toast.LENGTH_SHORT).show()
        }

//        thread {
//            for (x in 0 until 5){
//                Timer().schedule(x.toLong() * 5000) {
//                    try {
//                        activity?.runOnUiThread {
//                            applyStats()
//                            Toast.makeText(context, "Stats updated!", Toast.LENGTH_SHORT).show()
//                        }
//                    } catch (e: TypeCastException) {
//                        Toast.makeText(context, "Stats couldn't load. Sorry!", Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//        }


        profil_lastname.text = User.lastName
        profil_firstName.text = User.firstName
        profil_username.text = User.username

        if(User.avatarUrl != "" && User.avatarUrl != "noPhoto"){
            val uri = IP + "/" + User.avatarUrl
            Glide.with(this).load(uri.toUri()).into(profil_avatar)
        } else if (User.avatarUrl == "noPhoto"){
            val bitmapNoPhoto = BitmapFactory.decodeResource(this.resources, R.drawable.no_photo)
            profil_avatar.setImageBitmap(bitmapNoPhoto)
        }

        val tmp =arguments?.getBoolean("avatarUpdated")
        if(tmp != null){
            if(arguments?.getBoolean("avatarUpdated") as Boolean){
                val uri = IP + "/" + User.avatarUrl
                Glide.with(this).asBitmap().load(uri.toUri()).into(object : CustomTarget<Bitmap>(){
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        (activity as LobbyActivity).drawer?.removeHeader()
                        (activity as LobbyActivity).drawer?.removeAllItems()
                        (activity as LobbyActivity).drawer = (activity as LobbyActivity).createDrawer(resource)
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {
                    }
                })
            }
        }

        //make invisible all editTexts and save buttons
        //firstname
        edit_firstname.visibility = View.INVISIBLE
        btn_save_firstname.visibility = View.INVISIBLE
        btn_dont_save_firstname.visibility = View.INVISIBLE

        profil_firstName.visibility = View.VISIBLE
        firstname_edit_btn.visibility = View.VISIBLE

        //lastname
        edit_lastname.visibility = View.INVISIBLE
        btn_save_lastname.visibility = View.INVISIBLE
        btn_dont_save_lastname.visibility = View.INVISIBLE

        profil_lastname.visibility = View.VISIBLE
        lastName_edit_btn.visibility = View.VISIBLE

        firstname_edit_btn.setOnClickListener {
            edit_firstname.visibility = View.VISIBLE
            btn_save_firstname.visibility = View.VISIBLE
            btn_dont_save_firstname.visibility = View.VISIBLE

            profil_firstName.visibility = View.INVISIBLE
            firstname_edit_btn.visibility= View.INVISIBLE
        }

        lastName_edit_btn.setOnClickListener {
            edit_lastname.visibility = View.VISIBLE
            btn_save_lastname.visibility = View.VISIBLE
            btn_dont_save_lastname.visibility = View.VISIBLE

            profil_lastname.visibility = View.INVISIBLE
            lastName_edit_btn.visibility = View.INVISIBLE
        }

        btn_dont_save_firstname.setOnClickListener {
            edit_firstname.visibility = View.INVISIBLE
            btn_save_firstname.visibility = View.INVISIBLE
            btn_dont_save_firstname.visibility = View.INVISIBLE

            profil_firstName.visibility = View.VISIBLE
            firstname_edit_btn.visibility= View.VISIBLE
        }

        btn_dont_save_lastname.setOnClickListener {
            edit_lastname.visibility = View.INVISIBLE
            btn_save_lastname.visibility = View.INVISIBLE
            btn_dont_save_lastname.visibility = View.INVISIBLE

            profil_lastname.visibility = View.VISIBLE
            lastName_edit_btn.visibility= View.VISIBLE
        }

        val mainView = view

        btn_save_firstname.setOnClickListener {
            if(edit_firstname.text.toString() != "")
                changeFirstName(edit_firstname.text.toString(), mainView)
            else
                Toast.makeText(context, "Your first name can't be empty!", Toast.LENGTH_SHORT).show()
        }

        btn_save_lastname.setOnClickListener {
            if(edit_lastname.text.toString() != "")
                changeLastName(edit_lastname.text.toString(), mainView)
            else
                Toast.makeText(context, "Your last name can't be empty!", Toast.LENGTH_SHORT).show()
        }

        //Modif in avatar?

        btn_choose_avatar.visibility = View.INVISIBLE
        btn_draw_it.visibility = View.INVISIBLE

        btn_change_avatar.visibility = View.VISIBLE

        btn_change_avatar.setOnClickListener {
            btn_change_avatar.visibility = View.INVISIBLE
            btn_choose_avatar.visibility = View.VISIBLE
            btn_draw_it.visibility = View.VISIBLE
        }

        btn_draw_it.setOnClickListener {
            val transaction = supportFragmentManager.beginTransaction().apply {
                replace(R.id.nav_host_fragment_lobby, FragmentFreeDraw(true, supportFragmentManager))
                addToBackStack(null)
            }
            transaction.commit()
        }

        btn_choose_avatar.setOnClickListener {
            var chooseFile: Intent = Intent(Intent.ACTION_GET_CONTENT)
            chooseFile.setType("*/*")
            chooseFile = Intent.createChooser(chooseFile, "Choose your avatar")
            startActivityForResult(chooseFile, PICKFILE_RESULT_CODE)
        }

        btn_change_password.setOnClickListener {
            val transaction = supportFragmentManager.beginTransaction().apply {
                replace(R.id.nav_host_fragment_lobby, FragmentEditPassword(supportFragmentManager))
                addToBackStack(null)
            }
            transaction.commit()
        }
    }

    fun applyStats() {
        nbGamePlayed.text = User.stats?.nbGames.toString()

        if(User.stats?.victoryPercentage.toString().length > 5){
            val tmp = User.stats?.victoryPercentage.toString().substring(0, 5) + "%"
            winRate.text = tmp
        }
        else{
            val tmp = User.stats?.victoryPercentage.toString() + "%"
            winRate.text = tmp
        }

        //average game duration
        val gameDuration = User.stats?.averageMatchesTime as Number
        val numberHours = (gameDuration.toFloat() / 3600).toInt()
        val restesHours = gameDuration.toFloat() % 3600

        val minutes = (restesHours / 60).toInt()
        val secondes = (restesHours % 60).toInt()

        val tmp = numberHours.toString() + "h " + minutes.toString() + "m " + secondes.toString() + "s"
        averageGameDuration.text = tmp

        //total time
        val totalTime = User.stats?.totalMatchesTime as Number
        val hours = (totalTime.toFloat() / 3600).toInt()
        val rHours = totalTime.toFloat() % 3600

        val min = (rHours / 60).toInt()
        val sec = (rHours % 60).toInt()

        val tmp2 = hours.toString() + "h " + min.toString() + "m " + sec.toString() + "s"

        totalTimeSpent.text = tmp2
    }
    fun changeFirstName(firstName: String, mainView: View)
    {
        val http = HttpRequest()
        context?.let {context ->
            val callbackFirstName = CallbackFirstName(context, mainView, activity as Activity, firstName)
            http.putUserFirstName(firstName, callbackFirstName)
        }
    }

    fun changeLastName(lastName: String, mainView: View)
    {
        val http = HttpRequest()
        context?.let {context ->
            val callbackLastName = CallbackLastName(context, mainView, activity as Activity, lastName)
            http.putUserLastName(lastName, callbackLastName)
        }
    }

    fun getUserInfos() {
        val http = HttpRequest()
        context?.let {context ->
            http.getUserInfosRequest(User, context, false)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            PICKFILE_RESULT_CODE -> if (resultCode === -1) {
                fileUri = data?.getData() as Uri

                val bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, fileUri)
                val stream: ByteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 30, stream)
                val compressedByteArray = stream.toByteArray()

                val http = HttpRequest()
                http.putUserAvatar(compressedByteArray, CallbackAvatar(context!!, compressedByteArray, supportFragmentManager))

                (activity as LobbyActivity).drawer?.removeHeader()
                (activity as LobbyActivity).drawer?.removeAllItems()

                (activity as LobbyActivity).drawer = (activity as LobbyActivity).createDrawer(bitmap)
            }
        }
    }
}

class CallbackFirstName(private val context: Context, val view: View, val activity: Activity, val newFirstName: String): Callback{

    override fun onResponse(call: Call, response: Response) {
        this.responseState = true
        responseBody = response.message

        if (responseBody == "OK") {
            User.firstName = newFirstName
            activity?.runOnUiThread {
                view.profil_firstName.text = User.firstName

                view.edit_firstname.visibility = View.INVISIBLE
                view.btn_save_firstname.visibility = View.INVISIBLE
                view.btn_dont_save_firstname.visibility = View.INVISIBLE

                view.profil_firstName.visibility = View.VISIBLE
                view.firstname_edit_btn.visibility= View.VISIBLE


                (context as AppCompatActivity).runOnUiThread {
                    Toast.makeText(context, "Your first name is changed!", Toast.LENGTH_SHORT).show()
                }
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

class CallbackLastName(private val context: Context, val view: View, val activity: Activity, val newLastName: String): Callback{

    override fun onResponse(call: Call, response: Response) {
        this.responseState = true
        responseBody = response.message

        if (responseBody == "OK") {
            User.lastName = newLastName
            activity?.runOnUiThread {
                view.profil_lastname.text = User.lastName

                view.edit_lastname.visibility = View.INVISIBLE
                view.btn_save_lastname.visibility = View.INVISIBLE
                view.btn_dont_save_lastname.visibility = View.INVISIBLE

                view.profil_lastname.visibility = View.VISIBLE
                view.lastName_edit_btn.visibility = View.VISIBLE

                (context as AppCompatActivity).runOnUiThread {
                    Toast.makeText(context, "Your last name is changed!", Toast.LENGTH_SHORT).show()
                }
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
package com.example.polypaintapp

import android.app.Activity
import kotlinx.android.synthetic.main.fragment_lobby_main.*

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import androidx.core.os.bundleOf
import androidx.core.view.drawToBitmap
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import com.madrapps.pikolo.listeners.SimpleColorSelectionListener
import com.mikepenz.materialdrawer.Drawer
import kotlinx.android.synthetic.main.activity_lobby.*
import kotlinx.android.synthetic.main.activity_paint.*
import kotlinx.android.synthetic.main.fragment_profilview.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.ByteArrayOutputStream
import java.io.IOException


class FragmentFreeDraw(private var editProfil: Boolean = false, private val supportFragmentManager: FragmentManager? = null, private val inLobby: Boolean = false) : Fragment() {

    var strokeColor = 0x000000

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_paint, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(inLobby)
            (activity as LobbyActivity).nameZoneLobby.text = "FREE DRAW"

        draw_view2.setStrokeWidth(50.0f)
        //SocketUser.onMessage(this)

        color_picker2.setColorSelectionListener(object : SimpleColorSelectionListener() {
            override fun onColorSelected(color: Int) {
                // Do whatever you want with the color
                draw_view2.setColor(color)
                strokeColor = color
            }
        })
        startSetup()
        image_effaceTrait2.setOnClickListener {
            color_picker2.visibility = View.GONE
            seekBar_width2.visibility = View.GONE
            activateDraw2.isSelected = false
            image_draw_eraser2.isSelected = false
            it.isSelected = true
            draw_view2.mIsErasingStroke = true
        }
        activateDraw2.isSelected = true
        activateDraw2.setOnClickListener {
            draw_view2.setColor(strokeColor)
            color_picker2.visibility = View.GONE
            draw_view2.mIsErasingStroke = false
            seekBar_width2.visibility = View.GONE
            image_effaceTrait2.isSelected = false
            image_draw_eraser2.isSelected = false
            it.isSelected = true
        }

        var inSignUp = false
        if(arguments?.getBoolean("inSignUp") != null) {
            val tmp = arguments?.getBoolean("inSignUp")
            inSignUp = tmp as Boolean
        }


        var userFirstNameTmp = arguments?.getString("userFirstName")
        var userLastNameTmp = arguments?.getString("userLastName")
        var userUsernameTmp = arguments?.getString("userUsername")
        var userPasswordTmp = arguments?.getString("userPassword")

        save_image2.setOnClickListener {

            color_picker2.visibility = View.GONE
            if(!editProfil && !inSignUp) {
                draw_view2.saveImageToExternalStorage(draw_view2.getBitmap())

            } else {
                val bitmap = draw_view2.getBitmap()

                val imageConverter = ImageConverter()

                val userByteArray = imageConverter.toByteArray(bitmap)

                if(inSignUp) {
                    val bundle = bundleOf("userByteArray" to userByteArray,
                        "userFirstName" to userFirstNameTmp, "userLastName" to userLastNameTmp, "userUsername" to userUsernameTmp, "userPassword" to userPasswordTmp)
                    view.findNavController().navigate(R.id.action_fragmentFreeDraw_to_fragmentSignUp, bundle)
                }  else if(editProfil) {
                    sendNewAvatarRequest(userByteArray, supportFragmentManager as FragmentManager)
                }
            }
        }

        stroke_change_round2.setOnClickListener {

            color_picker2.visibility = View.GONE
            seekBar_width2.visibility = View.GONE
            image_effaceTrait2.isSelected = false
            if (draw_view2.mIsErasingStroke) {
                activateDraw2.isSelected = true
                draw_view2.setColor(strokeColor)
            }
            draw_view2.mIsErasingStroke = false
            changeStroke(true)

            stroke_change_square2.isInvisible = false
            stroke_change_round2.isInvisible = true
        }

        stroke_change_square2.setOnClickListener {

            color_picker2.visibility = View.GONE
            seekBar_width2.visibility = View.GONE
            image_effaceTrait2.isSelected = false
            if (draw_view2.mIsErasingStroke) {
                activateDraw2.isSelected = true
                draw_view2.setColor(strokeColor)
            }
            draw_view2.mIsErasingStroke = false
            changeStroke(false)

            stroke_change_square2.isInvisible = true
            stroke_change_round2.isInvisible = false
        }


        image_draw_eraser2.setOnClickListener {

            color_picker2.visibility = View.GONE
            activateDraw2.isSelected = false
            seekBar_width2.visibility = View.GONE
            it.isSelected = true
            image_effaceTrait2.isSelected = false
            draw_view2.mIsErasingStroke = false
            draw_view2.setColor(0xFFFFFF)
        }

        image_draw_width2.setOnClickListener {

            draw_view2.mIsErasingStroke = false
            draw_view2.setColor(strokeColor)
            color_picker2.visibility = View.GONE
            if(seekBar_width2.visibility == View.VISIBLE)
                seekBar_width2.visibility = View.GONE
            else
                seekBar_width2.visibility = View.VISIBLE

        }

        image_draw_color2.setOnClickListener {
            draw_view2.mIsErasingStroke = false
            seekBar_width2.visibility = View.GONE
            if(color_picker2.visibility == View.VISIBLE)
                color_picker2.visibility = View.GONE
            else
                color_picker2.visibility = View.VISIBLE
        }

        seekBar_width2.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                draw_view2.setStrokeWidth(progress.toFloat())
                draw_view2.mIsErasingStroke = false
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    fun startSetup() {
        seekBar_width2.visibility = View.GONE
        color_picker2.visibility = View.GONE
        stroke_change_square2.isInvisible = false
        stroke_change_round2.isInvisible = true
        draw_view2.setStrokeWidth(50.0f)
    }

    fun changeStroke(round : Boolean) {
        if(round) {
            draw_view2.changeStrokeCapToRound()
        }
        else {
            draw_view2.changeStrokeCapToSquare()
        }
    }

    fun sendNewAvatarRequest(userByteArray: ByteArray, supportFragmentManager: FragmentManager) {
        val http = HttpRequest()
        context?.let {context ->
            val callbackAvatar = CallbackAvatar(context, userByteArray, supportFragmentManager)
            http.putUserAvatar(userByteArray, callbackAvatar)
        }
    }
}

class CallbackAvatar(private val context: Context, val userByteArray: ByteArray, val supportFragmentManager: FragmentManager): Callback{

    override fun onResponse(call: Call, response: Response) {
        this.responseState = true
        responseBody = response.message

        if (responseBody == "OK") {
            User.avatarByteArray = userByteArray

            (context as AppCompatActivity).runOnUiThread {
                Toast.makeText(context, "Avatar changed succesfully!", Toast.LENGTH_SHORT).show()
            }

            val fragmentProfil = FragmentProfil(supportFragmentManager)
            val bundle = Bundle()
            bundle.putBoolean("avatarUpdated", true)

            fragmentProfil.arguments = bundle
            val transaction = supportFragmentManager.beginTransaction().apply {
                replace(R.id.nav_host_fragment_lobby, fragmentProfil)
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
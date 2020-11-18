package com.example.polypaintapp

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import com.madrapps.pikolo.listeners.SimpleColorSelectionListener
import kotlinx.android.synthetic.main.activity_paint.*
import kotlinx.android.synthetic.main.fragment_in_match.*

class Fragment_Paint : Fragment() {

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

        draw_view.setSocket(SocketUser.socket.socket)
        draw_view.setRoom(WaitingRoom.name + CurrentGameMode.gameMode)
        draw_view.setStrokeWidth(50.0f)


        color_picker.setColorSelectionListener(object : SimpleColorSelectionListener() {
            override fun onColorSelected(color: Int) {
                // Do whatever you want with the color
                draw_view.setColor(color)
            }
        })

        startSetup()

        save_image.setOnClickListener {
            if (ContextCompat.checkSelfPermission(activity!!,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    activity!!,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 4)
            } else {
                draw_view.saveImageToExternalStorage(draw_view.getBitmap())
            }
        }

        stroke_change_round.setOnClickListener {

            changeStroke(true)
            stroke_change_square.isInvisible = false
            stroke_change_round.isInvisible = true
        }

        stroke_change_square.setOnClickListener {

            changeStroke(false)
            stroke_change_square.isInvisible = true
            stroke_change_round.isInvisible = false
        }

        image_draw_eraser.setOnLongClickListener {
            draw_view.clearCanvas()
            true
        }

        image_draw_eraser.setOnClickListener {

            strokeColor = 0xFFFFFF
            draw_view.setColor(strokeColor)
        }

        image_draw_width.setOnClickListener {

            draw_view.setColor(strokeColor)
            color_picker.visibility = View.GONE
            if(seekBar_width.visibility == View.VISIBLE)
                seekBar_width.visibility = View.GONE
            else
                seekBar_width.visibility = View.VISIBLE

        }

        image_draw_color.setOnClickListener {

            seekBar_width.visibility = View.GONE
            if(color_picker.visibility == View.VISIBLE)
                color_picker.visibility = View.GONE
            else
                color_picker.visibility = View.VISIBLE
        }

        seekBar_width.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                draw_view.setStrokeWidth(progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
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
}
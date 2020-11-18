package com.example.polypaintapp


import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Paint
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import kotlinx.android.synthetic.main.activity_paint.*
import com.divyanshu.draw.widget.DrawView
import com.madrapps.pikolo.listeners.SimpleColorSelectionListener
import com.madrapps.pikolo.ColorPicker
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.JsonParser


class PaintActivity : AppCompatActivity() {

    var strokeColor = 0x000000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paint)


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

        save_image2.setOnClickListener {
            color_picker2.visibility = View.GONE
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 4)
            } else {
                draw_view2.saveImageToExternalStorage(draw_view2.getBitmap())
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
                draw_view2.setColor(strokeColor)
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





}
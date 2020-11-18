package com.divyanshu.draw.widget

import android.content.Context
import android.graphics.*
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import io.socket.client.Socket
//import com.sun.imageio.plugins.jpeg.JPEG
import android.os.Environment.getExternalStorageDirectory
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.example.polypaintapp.JSON
import com.google.gson.*
import java.io.*
import java.util.*
import org.json.JSONObject
import kotlin.collections.ArrayList
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.divyanshu.draw.widget.PaintOptions
import kotlinx.android.synthetic.main.activity_drawing.view.*


class PointXY {
    constructor(px: Float, py: Float) {
        this.x = px
        this.y = py
    }

    val x: Float
    val y: Float
}

class StrokeAttributes {

    constructor(att: Int, color: ArrayList<Int>, sWidth: Float, sHeight: Float) {
        this.stylusType = att
        this.rgb = color
        this.width = sWidth
        this.height = sHeight
    }
    val stylusType: Int
    val rgb: ArrayList<Int>
    val width: Float
    val height: Float

}

class StrokeJSON {

    constructor(att: StrokeAttributes, points: ArrayList<PointXY>, width: Int, height: Int, zIndex: Int) {
        this.drawAttributes = att
        this.path = points
        this.imageWidth = width
        this.imageHeight = height
        this.zIndex = zIndex
    }

    val drawAttributes: StrokeAttributes
    val path: ArrayList<PointXY>
    val imageWidth: Int
    val imageHeight: Int
    val zIndex: Int
}

class DrawView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var mPaths = LinkedHashMap<MyPath, PaintOptions>()

    private var mLastPaths = LinkedHashMap<MyPath, PaintOptions>()
    private var mUndonePaths = LinkedHashMap<MyPath, PaintOptions>()

    private var mPaint = Paint()

    private var mPath = MyPath()
    private var mPaintOptions = PaintOptions()
    private var zIndex = 0
    private var socket: Socket? = null
    private var room: String? = null
    private var strokeImageHeight: Int = 1
    private var strokeImageWidth: Int = 1
    private var mCurX = 0f
    private var mCurY = 0f
    private var mStartX = 0f
    private var mStartY = 0f
    private var mIsSaving = false
    private var mIsStrokeWidthBarEnabled = false

    var mIsErasingStroke = false

    private var canDraw: Boolean = true

    var isEraserOn = false
        private set

    init {
        mPaint.apply {
            color = mPaintOptions.color
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = mPaintOptions.strokeWidth
            isAntiAlias = true
        }
    }

    fun changeStrokeCapToSquare() {
        mPaintOptions.strokeCap = Paint.Cap.SQUARE
    }

    fun changeStrokeCapToRound() {
        mPaintOptions.strokeCap = Paint.Cap.ROUND
    }

    fun undo() {
        if (mPaths.isEmpty() && mLastPaths.isNotEmpty()) {
            mPaths = mLastPaths.clone() as LinkedHashMap<MyPath, PaintOptions>
            mLastPaths.clear()
            invalidate()
            return
        }
        if (mPaths.isEmpty()) {
            return
        }
        val lastPath = mPaths.values.lastOrNull()
        val lastKey = mPaths.keys.lastOrNull()

        mPaths.remove(lastKey)
        if (lastPath != null && lastKey != null) {
            mUndonePaths[lastKey] = lastPath
        }
        invalidate()
    }

    fun redo() {
        if (mUndonePaths.keys.isEmpty()) {
            return
        }

        val lastKey = mUndonePaths.keys.last()
        addPath(lastKey, mUndonePaths.values.last())
        mUndonePaths.remove(lastKey)
        invalidate()
    }

    fun setColor(newColor: Int) {
        @ColorInt
        val alphaColor = ColorUtils.setAlphaComponent(newColor, mPaintOptions.alpha)
        mPaintOptions.color = alphaColor
        if (mIsStrokeWidthBarEnabled) {
            invalidate()
        }
    }

    fun setAlpha(newAlpha: Int) {
        val alpha = (newAlpha * 255) / 100
        mPaintOptions.alpha = alpha
        setColor(mPaintOptions.color)
    }

    fun setStrokeWidth(newStrokeWidth: Float) {
        mPaintOptions.strokeWidth = newStrokeWidth
        if (mIsStrokeWidthBarEnabled) {
            invalidate()
        }
    }

    fun saveImageToExternalStorage(bitmap: Bitmap): Uri {
        // Get the external storage directory path
        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        // Create a file to save the image
        val file = File(path, "${UUID.randomUUID()}.jpg")

        try {
            // Get the file output stream
            val stream: OutputStream = FileOutputStream(file)

            // Compress the bitmap
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)

            // Flush the output stream
            stream.flush()

            // Close the output stream
            stream.close()
            Toast.makeText(context, "Image saved !", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) { // Catch the exception
            e.printStackTrace()
            Toast.makeText(context, "You need to grant permissions first!", Toast.LENGTH_SHORT).show()
        }

        // Return the saved image path to uri
        return Uri.parse(file.absolutePath)
    }


    fun getBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        mIsSaving = true
        draw(canvas)
        mIsSaving = false
        return bitmap
    }

    fun addPath(path: MyPath, options: PaintOptions) {
        mPaths[path] = options
    }

    override fun onDraw(canvas: Canvas) {
        synchronized(this) {
            super.onDraw(canvas)
            val sortedMap = mPaths.toSortedMap()
            for((key, value) in sortedMap) {
                changePaint(value)
                canvas.drawPath(key, mPaint)
            }

            changePaint(mPaintOptions)
            canvas.drawPath(mPath, mPaint)
        }
    }

    fun onEraseStroke(id: Int) {
        synchronized(this) {
            mPaths[mPath] = mPaintOptions
            mPath = MyPath()
            mPaintOptions = PaintOptions(
                mPaintOptions.color,
                mPaintOptions.strokeWidth,
                mPaintOptions.alpha,
                mPaintOptions.isEraserOn,
                mPaintOptions.strokeCap
            )
            var keyToRemove = MyPath()
            var found = false
            for ((key, value) in mPaths) {
                if (key.zIndex == id) {
                    found = true
                    keyToRemove = key
                }
            }
            if (found) {
                mPaths.remove(keyToRemove)
            }
            invalidate()
        }
    }

    private fun eraseStroke(event: MotionEvent) {
        val sortedMap = mPaths.toSortedMap()
        var keyToRemove = MyPath()
        var found = false
        for((key, value) in sortedMap) {
            for (action in key.actions) {
                var width = 30
                if (value.strokeWidth > 30)
                    width = value.strokeWidth.toInt()
                if(action is Quad) {
                    val q: Quad = action
                    val distance1 = Math.sqrt(Math.pow((q.x1.toDouble()-event.x.toDouble()), 2.0) + Math.pow((q.y1.toDouble()-event.y.toDouble()), 2.0))
                    val distance2 = Math.sqrt(Math.pow((q.x2.toDouble()-event.x.toDouble()), 2.0) + Math.pow((q.y2.toDouble()-event.y.toDouble()), 2.0))
                    if(value.color != 0xFFFFFFFF.toInt() && (distance1 <= width || distance2 <= width)) {
                        found = true
                        keyToRemove = key
                    }
                } else if(action is Line) {
                    val q: Line = action
                    val distance = Math.sqrt(Math.pow((q.x.toDouble()-event.x.toDouble()), 2.0) + Math.pow((q.y.toDouble()-event.y.toDouble()), 2.0))
                    if(distance <= width && value.color != 0xFFFFFFFF.toInt()) {
                        found = true
                        keyToRemove = key
                    }
                }
            }
        }
        if(found) {
            mPaths.remove(keyToRemove)
            invalidate()
        }
        if(room != null) {
            var json = JsonObject()
            json.addProperty("room", room)
            json.addProperty("id", keyToRemove.zIndex)
            socket?.emit("eraseStroke", json)
        }
    }

    private fun changePaint(paintOptions: PaintOptions) {
        mPaint.color = if (paintOptions.isEraserOn) Color.WHITE else paintOptions.color
        mPaint.strokeWidth = paintOptions.strokeWidth
        mPaint.strokeCap = paintOptions.strokeCap
    }

    fun clearCanvas() {
        synchronized(this) {
            mLastPaths = mPaths.clone() as LinkedHashMap<MyPath, PaintOptions>
            mPath.reset()
            mPaths.clear()
            invalidate()
        }
    }

    private fun actionDown(x: Float, y: Float) {
        mPath.reset()
        mPath.moveTo(x, y)
        mCurX = x
        mCurY = y
    }

    private fun actionMove(x: Float, y: Float) {
        mPath.quadTo(mCurX, mCurY, (x + mCurX) / 2, (y + mCurY) / 2)
        mCurX = x
        mCurY = y
    }

    private fun actionUp() {
        mPath.lineTo(mCurX, mCurY)

        // draw a dot on click
        if (mStartX == mCurX && mStartY == mCurY) {
            mPath.lineTo(mCurX, mCurY + 2)
            mPath.lineTo(mCurX + 1, mCurY + 2)
            mPath.lineTo(mCurX + 1, mCurY)
        }

        mPaths[mPath] = mPaintOptions
        mPath = MyPath()
        mPaintOptions = PaintOptions(
            mPaintOptions.color,
            mPaintOptions.strokeWidth,
            mPaintOptions.alpha,
            mPaintOptions.isEraserOn,
            mPaintOptions.strokeCap
        )
    }

    fun deactivateDraw() {
        canDraw = false
    }

    fun activateDraw() {
        canDraw = true
    }

    fun setSocket(s: Socket) {
        socket = s
        socket?.on("startStroke") { data ->
            synchronized(this) {
                mPaths[mPath] = mPaintOptions
                mPath = MyPath()
                mPaintOptions = PaintOptions(
                    mPaintOptions.color,
                    mPaintOptions.strokeWidth,
                    mPaintOptions.alpha,
                    mPaintOptions.isEraserOn,
                    mPaintOptions.strokeCap
                )
                val gson = Gson()
                val strokeJson = JsonParser().parse(data[0].toString()).asJsonObject
                val strokeJson2 = gson.fromJson(strokeJson, StrokeJSON::class.java)
                strokeImageHeight = strokeJson2.imageHeight
                strokeImageWidth = strokeJson2.imageWidth
                mStartX = strokeJson2.path[0].x * this.width / strokeJson2.imageWidth
                mStartY = strokeJson2.path[0].y * this.height / strokeJson2.imageHeight
                this.mPaintOptions.strokeWidth = strokeJson2.drawAttributes.width
                var rgb : ArrayList<Int> = strokeJson2.drawAttributes.rgb
                if (rgb == null) {
                    rgb = ArrayList(3)
                    rgb.add(0)
                    rgb.add(0)
                    rgb.add(0)
                }

                this.mPaintOptions.color = Color.rgb(rgb[0], rgb[1], rgb[2])
                if (strokeJson2.drawAttributes.stylusType == 0) {
                    mPaintOptions.strokeCap = Paint.Cap.SQUARE
                } else {
                    mPaintOptions.strokeCap = Paint.Cap.ROUND
                }
                actionDown(mStartX, mStartY)
                for (i in 1 until strokeJson2.path.size) {
                    mStartX = strokeJson2.path[i].x * this.width / strokeImageWidth
                    mStartY = strokeJson2.path[i].y * this.height / strokeImageHeight
                    actionMove(mStartX, mStartY)
                }
                mPath.zIndex = strokeJson2.zIndex
                mPaths[mPath] = mPaintOptions
                invalidate()
            }
        }
        socket?.on("draw") { data ->
            synchronized(this) {
                val gson = Gson()
                val points = JsonParser().parse(data[0].toString()).asJsonArray
                for (i in 0 until points.size()) {
                    val pointJson = JsonParser().parse(points[i].toString()).asJsonObject
                    val point = gson.fromJson(pointJson, PointXY::class.java)
                    val x = point.x * this.width / strokeImageWidth
                    val y = point.y * this.height / strokeImageHeight
                    actionMove(x, y)
                }
                invalidate()
            }
        }
    }

    fun setRoom(r: String) {
        room = r
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {

        if (!canDraw) {
            return true
        }
        if (mIsErasingStroke) {
            if (event.action == MotionEvent.ACTION_UP)
                eraseStroke(event)
            return true
        }
        val x = event.x
        val y = event.y
        val stroke = JsonObject()
        stroke.addProperty("room", room)
        val points = PointXY(x, y)
        val path = ArrayList<PointXY>()
        path.add(points)
        val gson = Gson()
        val jsonPath = gson.toJson(path)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mPath = MyPath()
                val byteColor = ArrayList<Int>()
                byteColor.add(Color.red(mPaintOptions.color))
                byteColor.add(Color.green(mPaintOptions.color))
                byteColor.add(Color.blue(mPaintOptions.color))
                var stylusPoint = 1
                if (mPaintOptions.strokeCap == Paint.Cap.SQUARE)
                    stylusPoint = 0
                val att = StrokeAttributes(stylusPoint, byteColor,
                    this.mPaintOptions.strokeWidth, this.mPaintOptions.strokeWidth)
                this.zIndex++
                mPath.zIndex = this.zIndex
                val strokeJson = StrokeJSON(att, path, this.width, this.height, this.zIndex)
                stroke.addProperty("content", gson.toJson(strokeJson))
                mStartX = x
                mStartY = y
                actionDown(x, y)
                socket?.emit("startStroke", stroke)
                mUndonePaths.clear()
            }
            MotionEvent.ACTION_MOVE -> {
                actionMove(x, y)
                stroke.addProperty("content", jsonPath)
                socket?.emit("draw", stroke)
                actionMove(x, y)
            }
            MotionEvent.ACTION_UP -> actionUp()
        }

            invalidate()
            return true
        }

        fun toggleEraser() {
            isEraserOn = !isEraserOn
            mPaintOptions.isEraserOn = isEraserOn
            invalidate()
        }

}
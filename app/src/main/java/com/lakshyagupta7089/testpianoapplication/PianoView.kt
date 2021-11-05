package com.lakshyagupta7089.testpianoapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color.*
import android.graphics.Paint
import android.graphics.RectF
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

class PianoView : View {
    companion object {
        const val NB = 14
        const val TAG = "PianoView"
    }

    private val black: Paint = Paint().apply {
        color = BLACK
        isAntiAlias = true
    }
    private val white: Paint = Paint().apply {
        color = WHITE
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    private val yellow: Paint = Paint().apply {
        color = YELLOW
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    private var whites = HashMap<Int, Key>()
    private var blacks = HashMap<Int, Key>()
    private var keyWidth: Int = 0
    private var keyHeight: Int = 0
    private var soundPlayer: AudioSoundPlayer = AudioSoundPlayer(context)
    private var handler = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            invalidate()
        }
    }
    private val soundPool: SoundPool

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(
                AudioAttributes.USAGE_ASSISTANCE_SONIFICATION
            )
            .setContentType(
                AudioAttributes.CONTENT_TYPE_SONIFICATION
            )
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(50)
            .setAudioAttributes(
                audioAttributes
            )
            .build()
    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        keyWidth = w / NB
        keyHeight = h

        var count = 15

        for (i in 0 until NB) {
            val left = i * keyWidth
            var right = left + keyWidth

            if (i == NB - 1) {
                right = w;
            }

            var rect = RectF(
                left.toFloat(),
                0f,
                right.toFloat(),
                h.toFloat()
            )

            whites[i + 1] = Key(i + 1, rect)

            if (i != 0 && i != 3 && i != 7 && i != 10) {
                rect = RectF(
                    ((i - 1) * keyWidth).toFloat() + 0.25f * keyWidth,
                    0f,
                    ((i - 1) * keyWidth).toFloat() + 0.5f * keyWidth.toFloat() + 0.25f * keyWidth,
                    0.67f * height
                )
                blacks[count] = Key(count, rect)
                count++
            }
        }

        Log.e(TAG, "onSizeChanged: $keyWidth")
    }

    override fun onDraw(canvas: Canvas?) {
        for (key in whites.values) {
            canvas?.drawRect(key.rectF!!, if (key.down) yellow else white)
        }

        for (i in 1 until NB) {
            canvas?.drawLine(
                (i * keyWidth).toFloat(),
                0f,
                (i * keyWidth.toFloat()),
                keyHeight.toFloat(),
                black
            )
        }

        for (key in blacks.values) {
            canvas?.drawRect(key.rectF!!, if (key.down) yellow else black)
        }

        canvas?.save()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val action = event?.action
        val isDown = action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE

        for (touchIndex in 0 until event?.pointerCount!!) {
            val x = event.getX(touchIndex)
            val y = event.getY(touchIndex)

            Log.e(TAG, "onTouchEvent: x: $x, y: $y")
            val key: Key? = keyForCords(x, y)

            if (key != null) {
                key.down = true
            }
        }
        val tmp = ArrayList<Key>(whites.values)
        tmp.addAll(blacks.values)

        for (key in tmp) {
            if (key.down) {
                if (!soundPlayer.isNotePlaying(key.sound)) {
                    soundPlayer.playNote(key.sound)
                    invalidate()
                } else {
                    releaseKey(key)
                }
            } else {
                soundPlayer.stopNote(key.sound)
                releaseKey(key)
            }
        }

        return true
    }

    private fun releaseKey(key: Key) {
        handler.postDelayed({
            key.down = false
            soundPlayer.stopNote(key.sound)
            handler.sendEmptyMessage(0)
        }, 0)

        this.handler
    }

    private fun keyForCords(x: Float, y: Float): Key? {
        for (key in blacks.values) {
            if (key.rectF!!.contains(x, y)) {
                return key
            }
        }

        for (key in whites.values) {
            if (key.rectF!!.contains(x, y)) {
                return key
            }
        }

        return null
    }
}
package com.lakshyagupta7089.testpianoapplication

import android.content.Context
import android.util.SparseArray
import android.media.SoundPool

import android.media.AudioAttributes
import android.media.AudioManager

import android.os.Build
import android.os.Process
import android.os.Process.THREAD_PRIORITY_BACKGROUND
import android.util.Log

class AudioSoundPlayer(private val context: Context) {
    companion object {
        var SOUND_MAP = SparseArray<Int>().apply {
            put(1, R.raw.c4)
            put(2, R.raw.d4)
            put(3, R.raw.e4)
            put(4, R.raw.f4)
            put(5, R.raw.g4)
            put(6, R.raw.a4)
            put(7, R.raw.b4)
            put(8, R.raw.c5)
            put(9, R.raw.d5)
            put(10, R.raw.e5)
            put(11, R.raw.f5)
            put(12, R.raw.g5)
            put(13, R.raw.a5)
            put(14, R.raw.b5)


            put(15,  R.raw.c4black)
            put(16, R.raw.d4black)
            put(17, R.raw.f4black)
            put(18, R.raw.g4black)
            put(19, R.raw.a4black)

            put(20,  R.raw.c5black)
            put(21, R.raw.d5black)
            put(22, R.raw.f5black)
            put(23, R.raw.g5black)
            put(24, R.raw.a5black)
        }
        const val MAX_VOLUME = 100
        const val CURRENT_VOLUME = 90
        const val TAG = "AudioSoundPlayer"
    }

    private var threadMap: SparseArray<PlayerThread>? = null

    init {
        threadMap = SparseArray<PlayerThread>()
    }

    fun playNote(note: Int) {
        stopNote(note)
        if (!isNotePlaying(note)) {
            val thread = PlayerThread(note)
            thread.priority = THREAD_PRIORITY_BACKGROUND
            thread.start()

            threadMap?.put(note, thread)
        }
    }

    fun isNotePlaying(note: Int): Boolean {
        return threadMap?.get(note) != null
    }

    fun stopNote(note: Int) {
        val thread = threadMap?.get(note)

        if (thread != null) {
            threadMap?.remove(note)
        }
    }

    inner class PlayerThread(note: Int) : Thread() {
        private val audioAttributes = AudioAttributes.Builder()
            .setUsage(
                AudioAttributes.USAGE_ASSISTANCE_SONIFICATION
            )
            .setContentType(
                AudioAttributes.CONTENT_TYPE_SONIFICATION
            )
            .build()!!
        private val soundPool: SoundPool = SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(
                    audioAttributes
                )
                .build()
        private val soundId:Int = soundPool.load(context, SOUND_MAP[note], 0)

        override fun run() {
            soundPool.setOnLoadCompleteListener { _, _, _ ->
                soundPool.play(soundId, 0.80f, 0.80f, 0, 0, 0.99f);
                soundPool.autoResume()
            }
        }
    }
}

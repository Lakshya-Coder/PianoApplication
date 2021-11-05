package com.lakshyagupta7089.testpianoapplication

import android.graphics.RectF

class Key {
    var sound: Int = 0
    var rectF: RectF? = null
    var down: Boolean = false

    constructor(sound: Int, rectF: RectF?) {
        this.sound = sound
        this.rectF = rectF
    }
}
package com.bitandik.labs.kotlinbots.basics

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.bitandik.labs.kotlinbots.basics.helpers.Constants.Companion.TAG
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.GpioCallback

class PirSensorActivity : Activity() {
    private var pirSensor: Gpio? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate")
        pirSensor?.let {
            it.setDirection(Gpio.DIRECTION_IN)
            it.setActiveType(Gpio.ACTIVE_HIGH)
            it.setEdgeTriggerType(Gpio.EDGE_BOTH)

            it.registerGpioCallback(object : GpioCallback() {
                override fun onGpioEdge(gpio: Gpio?): Boolean {
                    Log.i(TAG, "Movement Detected")
                    return true
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy")
        pirSensor?.close()
    }
}
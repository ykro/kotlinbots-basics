package com.bitandik.labs.kotlinbots.basics

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.GpioCallback
import com.google.android.things.pio.PeripheralManagerService

class PirSensorActivity : Activity() {
    private var pirSensor: Gpio = PeripheralManagerService().openGpio(pirSensorPinName)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate")
        pirSensor.setDirection(Gpio.DIRECTION_IN)
        pirSensor.setActiveType(Gpio.ACTIVE_HIGH)
        pirSensor.setEdgeTriggerType(Gpio.EDGE_BOTH)

        pirSensor.registerGpioCallback(object : GpioCallback() {
            override fun onGpioEdge(gpio: Gpio?): Boolean {
                Log.i(TAG, "Movement Detected")
                return true
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy")
        pirSensor.close()
    }
}
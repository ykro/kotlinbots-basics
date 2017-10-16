package com.bitandik.labs.kotlinbots.basics

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.bitandik.labs.kotlinbots.basics.helpers.Constants
import com.bitandik.labs.kotlinbots.basics.helpers.Constants.Companion.BACK
import com.bitandik.labs.kotlinbots.basics.helpers.Constants.Companion.DELAY_BETWEEN_DIRECTION_CHANGE
import com.bitandik.labs.kotlinbots.basics.helpers.Constants.Companion.FWD
import com.bitandik.labs.kotlinbots.basics.helpers.Constants.Companion.STOP
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManagerService
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch

class DCMotorActivity : Activity() {
    private var gpioA: Gpio? = null
    private var gpioB: Gpio? = null
    private var motorJob: Job? = null
    private var direction = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(Constants.TAG, "onCreate")

        val service = PeripheralManagerService()
        gpioA = service.openGpio(Constants.gpioAPinName)
        gpioA?.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)

        gpioB = service.openGpio(Constants.gpioBPinName)
        gpioB?.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)

        motorJob = launch(CommonPool) {
            moveDCMotor()
        }
    }

    suspend private fun moveDCMotor() {
        direction++

        when(direction) {
            FWD -> forward()
            BACK -> backward()
            STOP -> {
                stop()
                direction = -1
            }
        }

        delay(DELAY_BETWEEN_DIRECTION_CHANGE)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(Constants.TAG, "onDestroy")
        gpioA?.close()
        gpioB?.close()
    }

    private fun forward(){
        gpioA?.value = true
        gpioB?.value = false
    }

    private fun backward() {
        gpioA?.value = false
        gpioB?.value = true
    }

    private fun stop() {
        gpioA?.value = false
        gpioB?.value = false
    }
}
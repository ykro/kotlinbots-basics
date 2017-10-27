package com.bitandik.labs.kotlinbots.basics

import android.app.Activity
import android.os.Bundle
import android.util.Log

import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManagerService
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch

class DCMotorActivity : Activity() {
    private lateinit var gpioA: Gpio
    private lateinit var gpioB: Gpio
    private lateinit var motorJob: Job
    private var direction = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate")

        val service = PeripheralManagerService()
        gpioA = service.openGpio(gpioAPinName)
        gpioA.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)

        gpioB = service.openGpio(gpioBPinName)
        gpioB.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)

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
        Log.i(TAG, "onDestroy")
        gpioA.close()
        gpioB.close()
    }

    private fun forward(){
        gpioA.value = true
        gpioB.value = false
    }

    private fun backward() {
        gpioA.value = false
        gpioB.value = true
    }

    private fun stop() {
        gpioA.value = false
        gpioB.value = false
    }
}
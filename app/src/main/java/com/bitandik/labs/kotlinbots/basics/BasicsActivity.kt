package com.bitandik.labs.kotlinbots.basics

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.GpioCallback
import com.google.android.things.pio.PeripheralManagerService
import com.google.android.things.pio.Pwm
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch

class BasicsActivity : Activity() {
    private lateinit var ledJob: Job
    private lateinit var servoJob: Job

    private lateinit var led: Gpio
    private lateinit var servo: Pwm
    private lateinit var button: Gpio

    private var pulseDuration = 0.0
    private var isPulseIncreasing = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate")

        val manager = PeripheralManagerService()
        led = manager.openGpio(ledPinName)
        servo = manager.openPwm(pwmPinName)
        button = manager.openGpio(buttonPinName)

        led.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)

        ledJob = launch(CommonPool) {
            ledBlink()
        }

        button.setDirection(Gpio.DIRECTION_IN)
        button.setEdgeTriggerType(Gpio.EDGE_FALLING)

        button.registerGpioCallback(object : GpioCallback() {
            override fun onGpioEdge(gpio: Gpio?): Boolean {
                Log.i(TAG, "GPIO changed, button pressed")
                return true
            }
        })

        pulseDuration = MIN_ACTIVE_PULSE_DURATION_MS
        servo.setPwmFrequencyHz(1000 / PULSE_PERIOD_MS)
        servo.setPwmDutyCycle(pulseDuration)
        servo.setEnabled(true)

        servoJob = launch(CommonPool) {
            moveServo()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy")
        ledJob.cancel()
        led.close()

        button.close()

        servoJob.cancel()
        servo.close()
    }

    private suspend fun ledBlink() {
        led.value = !led.value

        Log.i(TAG, "Status: " + led.value)
        delay(INTERVAL_BETWEEN_BLINKS_MS)
        ledBlink()
    }

    private suspend fun moveServo() {
        if (isPulseIncreasing) {
            pulseDuration += PULSE_CHANGE_PER_STEP_MS
        } else {
            pulseDuration -= PULSE_CHANGE_PER_STEP_MS
        }

        if (pulseDuration > MAX_ACTIVE_PULSE_DURATION_MS) {
            pulseDuration = MAX_ACTIVE_PULSE_DURATION_MS
            isPulseIncreasing = !isPulseIncreasing
        } else if (pulseDuration < MIN_ACTIVE_PULSE_DURATION_MS) {
            pulseDuration = MIN_ACTIVE_PULSE_DURATION_MS
            isPulseIncreasing = !isPulseIncreasing
        }

        servo.setPwmDutyCycle(100 * pulseDuration / PULSE_PERIOD_MS);
        Log.i(TAG, "Pulse duration: " + pulseDuration)
        delay(INTERVAL_BETWEEN_STEPS_MS)
        moveServo()
    }
}

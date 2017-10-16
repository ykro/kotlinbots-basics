package com.bitandik.labs.kotlinbots.basics

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.bitandik.labs.kotlinbots.basics.helpers.Constants.Companion.INTERVAL_BETWEEN_BLINKS_MS
import com.bitandik.labs.kotlinbots.basics.helpers.Constants.Companion.INTERVAL_BETWEEN_STEPS_MS
import com.bitandik.labs.kotlinbots.basics.helpers.Constants.Companion.MAX_ACTIVE_PULSE_DURATION_MS
import com.bitandik.labs.kotlinbots.basics.helpers.Constants.Companion.MIN_ACTIVE_PULSE_DURATION_MS
import com.bitandik.labs.kotlinbots.basics.helpers.Constants.Companion.PULSE_CHANGE_PER_STEP_MS
import com.bitandik.labs.kotlinbots.basics.helpers.Constants.Companion.PULSE_PERIOD_MS
import com.bitandik.labs.kotlinbots.basics.helpers.Constants.Companion.TAG
import com.bitandik.labs.kotlinbots.basics.helpers.Constants.Companion.buttonPinName
import com.bitandik.labs.kotlinbots.basics.helpers.Constants.Companion.ledPinName
import com.bitandik.labs.kotlinbots.basics.helpers.Constants.Companion.pwmPinName
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.GpioCallback
import com.google.android.things.pio.PeripheralManagerService
import com.google.android.things.pio.Pwm
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.io.IOException

class BasicsActivity : Activity() {
    private var ledJob: Job? = null
    private var servoJob: Job? = null

    private var led: Gpio? = null
    private var servo: Pwm? = null
    private var button: Gpio? = null

    private var pulseDuration = 0.0
    private var isPulseIncreasing = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate")

        val manager = PeripheralManagerService()
        try {
            led = manager.openGpio(ledPinName)
            servo = manager.openPwm(pwmPinName)
            button = manager.openGpio(buttonPinName)
        } catch (e: IOException) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }

        led?.let {
            it.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)

            ledJob = launch(CommonPool) {
                ledBlink(it)
            }
        }

        button?.let {
            it.setDirection(Gpio.DIRECTION_IN)
            it.setEdgeTriggerType(Gpio.EDGE_FALLING)

            it.registerGpioCallback(object : GpioCallback() {
                override fun onGpioEdge(gpio: Gpio?): Boolean {
                    Log.i(TAG, "GPIO changed, button pressed")
                    return true
                }
            })
        }

        servo?.let {
            pulseDuration = MIN_ACTIVE_PULSE_DURATION_MS
            it.setPwmFrequencyHz(1000 / PULSE_PERIOD_MS)
            it.setPwmDutyCycle(pulseDuration)
            it.setEnabled(true)

            servoJob = launch(CommonPool) {
                moveServo(it)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy")
        ledJob?.cancel()
        led?.close()

        button?.close()

        servoJob?.cancel()
        servo?.close()
    }

    private suspend fun ledBlink(led: Gpio) {
        led.value = !led.value
        Log.i(TAG, "Status: " + led.value)
        delay(INTERVAL_BETWEEN_BLINKS_MS)
        ledBlink(led)
    }

    private suspend fun moveServo(servo: Pwm) {
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
        moveServo(servo)
    }
}

package com.bitandik.labs.kotlinbots.basics

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.bitandik.labs.kotlinbots.basics.helpers.Constants.Companion.ANGLE_INCREASE_PER_STEP
import com.bitandik.labs.kotlinbots.basics.helpers.Constants.Companion.INTERVAL_BETWEEN_STEPS_MS
import com.bitandik.labs.kotlinbots.basics.helpers.Constants.Companion.SERVO_MAX_ANGLE
import com.bitandik.labs.kotlinbots.basics.helpers.Constants.Companion.SERVO_MIN_ANGLE
import com.bitandik.labs.kotlinbots.basics.helpers.Constants.Companion.SERVO_PULSE_MAX_DURATION
import com.bitandik.labs.kotlinbots.basics.helpers.Constants.Companion.SERVO_PULSE_MIN_DURATION
import com.bitandik.labs.kotlinbots.basics.helpers.Constants.Companion.TAG
import com.bitandik.labs.kotlinbots.basics.helpers.Constants.Companion.buttonPinName
import com.bitandik.labs.kotlinbots.basics.helpers.Constants.Companion.pwmPinName
import com.google.android.things.contrib.driver.button.Button
import com.google.android.things.contrib.driver.pwmservo.Servo
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.io.IOException

class DriversActivity : Activity() {
    private var button: Button? = null
    private var servo: Servo? = null
    private var servoJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate")

        try {
            button = Button(buttonPinName,
                            Button.LogicState.PRESSED_WHEN_HIGH
            )
            servo = Servo(pwmPinName)
        } catch (e: IOException) {
            Log.e(TAG, "Error on PeripheralIO API", e)
        }

        servo?.let {

            it.setPulseDurationRange(SERVO_PULSE_MIN_DURATION, SERVO_PULSE_MAX_DURATION)
            it.setAngleRange(SERVO_MIN_ANGLE, SERVO_MAX_ANGLE)
            it.setEnabled(true)

            servoJob = launch(CommonPool) {
                servoMove(it)
            }
        }

        button?.setOnButtonEventListener({ _, pressed ->
                Log.i(TAG, "GPIO changed, button pressed: " + pressed)
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy")
        servoJob?.cancel()
        button?.close()
        servo?.close()
    }

    suspend private fun servoMove(servo: Servo) {
        if (servo.angle >= servo.maximumAngle) {
            servo.angle = servo.minimumAngle
        } else {
            servo.angle += ANGLE_INCREASE_PER_STEP
        }

        Log.i(TAG, "Angle: " + servo.angle)
        delay(INTERVAL_BETWEEN_STEPS_MS)
        servoMove(servo)
    }

}
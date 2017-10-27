package com.bitandik.labs.kotlinbots.basics

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.google.android.things.contrib.driver.button.Button
import com.google.android.things.contrib.driver.pwmservo.Servo
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch

class DriversActivity : Activity() {
    private lateinit var button: Button
    private lateinit var servo: Servo
    private lateinit var servoJob: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate")

        button = Button(buttonPinName,
                Button.LogicState.PRESSED_WHEN_HIGH
        )
        servo = Servo(pwmPinName)

        servo.setPulseDurationRange(SERVO_PULSE_MIN_DURATION, SERVO_PULSE_MAX_DURATION)
        servo.setAngleRange(SERVO_MIN_ANGLE, SERVO_MAX_ANGLE)
        servo.setEnabled(true)

        servoJob = launch(CommonPool) {
            servoMove()
        }

        button.setOnButtonEventListener({ _, pressed ->
                Log.i(TAG, "GPIO changed, button pressed: " + pressed)
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy")
        servoJob.cancel()
        button.close()
        servo.close()
    }

    suspend private fun servoMove() {
        if (servo.angle >= servo.maximumAngle) {
            servo.angle = servo.minimumAngle
        } else {
            servo.angle += ANGLE_INCREASE_PER_STEP
        }

        Log.i(TAG, "Angle: " + servo.angle)
        delay(INTERVAL_BETWEEN_STEPS_MS)
        servoMove()
    }

}
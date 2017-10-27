package com.bitandik.labs.kotlinbots.basics

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import com.google.android.things.contrib.driver.bmx280.Bmx280
import com.google.android.things.contrib.driver.bmx280.Bmx280SensorDriver
import com.google.android.things.contrib.driver.button.Button
import com.google.android.things.contrib.driver.pwmservo.Servo
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat
import com.google.android.things.pio.Gpio
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch

class RainbowHatActivity : Activity(), SensorEventListener {

    private lateinit var ledJob: Job
    private lateinit var servoJob: Job
    //private lateinit var sensorJob: Job

    private lateinit var led: Gpio
    private lateinit var servo: Servo
    private lateinit var button: Button
    private lateinit var tempSensor: Bmx280
    private lateinit var sensorManager: SensorManager
    private lateinit var sensorDriver: Bmx280SensorDriver

    private var listener: SensorEventListener = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate")
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.registerDynamicSensorCallback(object : SensorManager.DynamicSensorCallback() {
            override fun onDynamicSensorConnected(sensor: Sensor) {
                if (sensor.type === Sensor.TYPE_AMBIENT_TEMPERATURE) {
                    sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
                }
            }
        })

        led = RainbowHat.openLedRed()
        servo = RainbowHat.openServo()
        button = RainbowHat.openButtonA()
        //tempSensor = RainbowHat.openSensor()

        sensorDriver = RainbowHat.createSensorDriver()
        sensorDriver.registerTemperatureSensor()

        ledJob = launch(CommonPool) {
            ledBlink()
        }

        button.setOnButtonEventListener({ _, pressed ->
            Log.i(TAG, "GPIO changed, button pressed: " + pressed)
        })


        servo.setPulseDurationRange(SERVO_PULSE_MIN_DURATION, SERVO_PULSE_MAX_DURATION)
        servo.setAngleRange(SERVO_MIN_ANGLE, SERVO_MAX_ANGLE)
        servo.setEnabled(true)
        servoJob = launch(CommonPool) {
            servoMove()
        }
        /*
        tempSensor.setTemperatureOversampling(Bmx280.OVERSAMPLING_1X);
        sensorJob = launch(CommonPool) {
            readTemp()
        }
        */
    }

    /*
    suspend private fun readTemp() {
        Log.i(TAG, "temperature:" + tempSensor.readTemperature());
        delay(INTERVAL_BETWEEN_STEPS_MS)
        readTemp()
    }
    */

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy")
        ledJob.cancel()
        servoJob.cancel()

        led.close()
        button.close()
        servo.close()
        tempSensor.close()
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

    suspend private fun ledBlink() {
        led.value = !led.value
        Log.i(TAG, "Status: " + led.value)
        delay(INTERVAL_BETWEEN_BLINKS_MS)
        ledBlink()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.i(TAG, "accuracy changed: " + accuracy);
    }

    override fun onSensorChanged(event: SensorEvent) {
        Log.i(TAG, "sensor changed: " + event.values[0]);
    }

}
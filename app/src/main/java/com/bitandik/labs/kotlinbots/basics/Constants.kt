package com.bitandik.labs.kotlinbots.basics


const val TAG = "kotlinbots"

//pin names for i.MX7D
const val ledPinName = "GPIO_34"
const val buttonPinName = "GPIO_174"
const val pwmPinName = "PWM1"
const val pirSensorPinName = "GPIO_39"
const val gpioAPinName = "GPIO_35"
const val gpioBPinName = "GPIO_10"


//BasicsActivity
//LED
const val INTERVAL_BETWEEN_BLINKS_MS: Long = 1000
//Servo
const val INTERVAL_BETWEEN_STEPS_MS: Long = 1000
const val MIN_ACTIVE_PULSE_DURATION_MS = 1.0
const val MAX_ACTIVE_PULSE_DURATION_MS = 2.0
const val PULSE_PERIOD_MS = 20.0  //50Hz freq
const val PULSE_CHANGE_PER_STEP_MS = 0.2

//DriversActivity
//Servo
const val SERVO_MIN_ANGLE = 0.0
const val SERVO_MAX_ANGLE = 180.0
const val SERVO_PULSE_MIN_DURATION = 1.0
const val SERVO_PULSE_MAX_DURATION = 2.0
const val ANGLE_INCREASE_PER_STEP = 45.0

//DCMotorActivity
const val DELAY_BETWEEN_DIRECTION_CHANGE: Long = 3000
const val FWD = 0
const val BACK = 1
const val STOP = 2


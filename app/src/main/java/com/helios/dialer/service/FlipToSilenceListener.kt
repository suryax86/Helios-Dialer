package com.helios.dialer.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.telecom.TelecomManager

class FlipToSilenceListener(private val context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager

    private var wasFaceUp = false
    private var isSilenced = false

    fun start() {
        wasFaceUp = false
        isSilenced = false
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || isSilenced) return
        val z = event.values[2]

        if (z > 5.0) {
            wasFaceUp = true
        } else if (z < -8.5 && wasFaceUp) {
            try {
                telecomManager.silenceRinger()
                isSilenced = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

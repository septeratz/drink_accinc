package com.example.myapplication

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.core.app.NotificationCompat

class SensorService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var gyroscope: Sensor
    private lateinit var vibrator: Vibrator

    private val TILT_THRESHOLD = 5.0  // 기울기 임계값 (도 단위)
    private val ACCEL_THRESHOLD = 15.0  // 가속도 임계값 (m/s^2)

    override fun onCreate() {
        super.onCreate()

        startForegroundService()

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_UI)
    }

    private fun startForegroundService() {
        val channelId = "sensor_service_channel"
        val channelName = "Sensor Service"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("센서 서비스 실행 중")
            .setContentText("화면이 꺼져도 센서를 모니터링합니다.")
            .setSmallIcon(R.drawable.ic_notification)
            .build()

        startForeground(1, notification)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> handleAccelerometerData(event.values)
            Sensor.TYPE_GYROSCOPE -> handleGyroscopeData(event.values)
        }
    }

    private fun handleAccelerometerData(values: FloatArray) {
        val accelMagnitude = Math.sqrt(
            (values[0] * values[0] + values[1] * values[1] + values[2] * values[2]).toDouble()
        )

        if (accelMagnitude > ACCEL_THRESHOLD) {
            sendUpdateBroadcast("ACCELEROMETER")
            triggerHapticAlert()
        }
    }

    private fun handleGyroscopeData(values: FloatArray) {
        val tiltMagnitude = Math.sqrt(
            (values[0] * values[0] + values[1] * values[1] + values[2] * values[2]).toDouble()
        )

        if (tiltMagnitude > TILT_THRESHOLD) {
            sendUpdateBroadcast("GYROSCOPE")
            triggerHapticAlert()
        }
    }

    private fun sendUpdateBroadcast(sensorType: String) {
        val intent = Intent("com.example.myapplication.SENSOR_UPDATE")
        intent.putExtra("SENSOR_TYPE", sensorType)
        sendBroadcast(intent)
    }

    private fun triggerHapticAlert() {
        vibrator.vibrate(
            VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}

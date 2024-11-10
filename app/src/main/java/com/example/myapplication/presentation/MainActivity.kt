package com.example.myapplication.presentation

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.os.Vibrator
import android.os.VibrationEffect
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.TextView
import androidx.core.content.getSystemService
import com.example.myapplication.R

class MainActivity : Activity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var gyroscope: Sensor
    private lateinit var tiltTextView: TextView
    private lateinit var accelTextView: TextView
    private lateinit var vibrator: Vibrator

    private val TILT_THRESHOLD = 30.0  // 기울기 임계값 (도 단위)
    private val ACCEL_THRESHOLD = 15.0  // 가속도 임계값 (m/s^2)

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tiltTextView = findViewById(R.id.tiltTextView)
        accelTextView = findViewById(R.id.accelTextView)

        // 센서 매니저와 햅틱 설정
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        vibrator = getSystemService()!!

        // 센서 리스너 등록
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onResume() {
        super.onResume()
        // 센서 리스너 등록
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        super.onPause()
        // 센서 리스너 해제
        sensorManager.unregisterListener(this)
    }
    private var isInitialized = false

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (!isInitialized) {
            // 초기 센서 데이터를 무시하여 안정화 시간 확보
            isInitialized = true
            return
        }

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> handleAccelerometerData(event.values)
            Sensor.TYPE_GYROSCOPE -> handleGyroscopeData(event.values)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 필요시 구현
    }
    private fun triggerHapticAlert() {
        vibrator.vibrate(
            VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
        )
    }

    private val ALPHA = 0.01f // 필터 강도 조절 (0.0 ~ 1.0)
    private val filteredAccelData = FloatArray(3)
    private val filteredGyroData = FloatArray(3)

    private fun handleAccelerometerData(values: FloatArray) {
        // 저역 필터 적용
        for (i in values.indices) {
            filteredAccelData[i] = ALPHA * filteredAccelData[i] + (1 - ALPHA) * values[i]
        }
        displayAccelerometerData(filteredAccelData)
    }

    private fun handleGyroscopeData(values: FloatArray) {
        // 저역 필터 적용
        for (i in values.indices) {
            filteredGyroData[i] = ALPHA * filteredGyroData[i] + (1 - ALPHA) * values[i]
        }
        displayGyroscopeData(filteredGyroData)
    }

    private fun displayAccelerometerData(filteredData: FloatArray) {
        accelTextView.text = "가속도: x=%.5f, y=%.5f, z=%.5f".format(
            filteredData[0], filteredData[1], filteredData[2]
        )
    }

    private fun displayGyroscopeData(filteredData: FloatArray) {
        tiltTextView.text = "기울기: x=%.5f, y=%.5f, z=%.5f".format(
            filteredData[0], filteredData[1], filteredData[2]
        )
    }
}

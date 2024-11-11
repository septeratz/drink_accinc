package com.example.myapplication.presentation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.SensorService
import android.graphics.Color
import android.widget.TextView
import android.view.View
import com.example.myapplication.R

class MainActivity : AppCompatActivity() {

    private lateinit var tiltTextView: TextView
    private lateinit var accelTextView: TextView
    private lateinit var mainView: View

    private val sensorUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val sensorType = intent?.getStringExtra("SENSOR_TYPE")
            if (sensorType == "ACCELEROMETER") {
                mainView.setBackgroundColor(Color.BLUE)
                accelTextView.text = "가속도 임계값 초과!"
            } else if (sensorType == "GYROSCOPE") {
                mainView.setBackgroundColor(Color.RED)
                tiltTextView.text = "기울기 임계값 초과!"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainView = findViewById(R.id.mainView)
        tiltTextView = findViewById(R.id.tiltTextView)
        accelTextView = findViewById(R.id.accelTextView)

        // BroadcastReceiver 등록
        val filter = IntentFilter("com.example.myapplication.SENSOR_UPDATE")
        registerReceiver(sensorUpdateReceiver, filter)

        // Foreground Service 시작
        val intent = Intent(this, SensorService::class.java)
        startForegroundService(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        // BroadcastReceiver 해제
        unregisterReceiver(sensorUpdateReceiver)

        // Foreground Service 중지
        val intent = Intent(this, SensorService::class.java)
        stopService(intent)
    }
}

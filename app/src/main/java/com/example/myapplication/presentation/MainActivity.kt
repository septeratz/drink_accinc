package com.example.myapplication.presentation

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.SensorService

class MainActivity : AppCompatActivity() {  // AppCompatActivity로 변경

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Foreground Service 시작
        val intent = Intent(this, SensorService::class.java)
        startForegroundService(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Foreground Service 중지
        val intent = Intent(this, SensorService::class.java)
        stopService(intent)
    }
}

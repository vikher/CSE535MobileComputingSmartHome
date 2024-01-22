package com.android.example.smarthome

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val gestureSpinner: Spinner = findViewById(R.id.gestureSpinner)
        val nextButton: Button = findViewById(R.id.nextButton)

        // Populate the spinner with the gesture list
        val gestureAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.gesture_list,
            android.R.layout.simple_spinner_item
        )
        gestureAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        gestureSpinner.adapter = gestureAdapter

        // Handle Next button click
        nextButton.setOnClickListener {
            CounterSingleton.counter++
            val selectedGesture = gestureSpinner.selectedItem.toString()

            // Pass the selected gesture to Screen2
            val intent = Intent(this, Screen2Activity::class.java)
            intent.putExtra("selectedGesture", selectedGesture)
            intent.putExtra("counterValue", CounterSingleton.counter)
            startActivity(intent)
        }
    }
}

class CounterSingleton {
    companion object {
        var counter: Int = 0
    }
}
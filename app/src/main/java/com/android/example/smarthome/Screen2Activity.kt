package com.android.example.smarthome

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

class Screen2Activity : AppCompatActivity() {
    private var replayCount = 0
    private val maxReplayCount = 3
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen2)

        val expertVideoView: VideoView = findViewById(R.id.expertVideoView)
        val practiceButton: Button = findViewById(R.id.practiceButton)
        val replayButton: Button = findViewById(R.id.replayButton)

        // Receive the selected gesture from Screen1
        val selectedGesture = intent.getStringExtra("selectedGesture")

        val videoPath = getVideoPathForGesture(selectedGesture)
        expertVideoView.setVideoURI(Uri.parse(videoPath))
        expertVideoView.start()

        // Handle REPLAY button click
        replayButton.setOnClickListener {
            // Check if replayCount is less than the maxReplayCount
            if (replayCount < maxReplayCount) {
                expertVideoView.seekTo(0)
                expertVideoView.start()
                replayCount++

                showToast("Replay Count: $replayCount")
                if (replayCount == maxReplayCount) {
                    replayButton.isEnabled = false
                }
            } else {
                showToast("Maximum Replay Count Reached")
            }
        }

        // Handle PRACTICE button click
        practiceButton.setOnClickListener {
            val intent = Intent(this, Screen3Activity::class.java)
            intent.putExtra("selectedGesture", selectedGesture)
            intent.putExtra("counterValue", CounterSingleton.counter)
            startActivity(intent)
        }
    }

    private fun getVideoPathForGesture(gesture: String?): String {
        // Map gestures to video resource IDs
        val videoResourceId = when (gesture) {
            "Turn on lights" -> R.raw.lighton
            "Turn off lights" -> R.raw.lightoff
            "Turn on fan" -> R.raw.fanon
            "Turn off fan" -> R.raw.fanoff
            "Increase fan speed" -> R.raw.increasefanspeed
            "Decrease fan speed" -> R.raw.decreasefanspeed
            "Set Thermostat to specified temperature" -> R.raw.setthermo
            "Gesture for digit 0" -> R.raw.zero
            "Gesture for digit 1" -> R.raw.one
            "Gesture for digit 2" -> R.raw.two
            "Gesture for digit 3" -> R.raw.three
            "Gesture for digit 4" -> R.raw.four
            "Gesture for digit 5" -> R.raw.five
            "Gesture for digit 6" -> R.raw.six
            "Gesture for digit 7" -> R.raw.seven
            "Gesture for digit 8" -> R.raw.eight
            "Gesture for digit 9" -> R.raw.nine
            else -> R.raw.zero// Default video resource ID
        }

        return "android.resource://" + packageName + "/" + videoResourceId
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
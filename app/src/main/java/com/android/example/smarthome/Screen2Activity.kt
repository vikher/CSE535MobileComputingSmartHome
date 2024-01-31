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
            "Turn_On_Light" -> R.raw.lighton
            "Turn_Off_Light" -> R.raw.lightoff
            "Turn_On_Fan" -> R.raw.fanon
            "Turn_Off_Fan" -> R.raw.fanoff
            "Increase_Fan_Speed" -> R.raw.increasefanspeed
            "Decrease_Fan_Speed" -> R.raw.decreasefanspeed
            "Set_Thermostat_To_Specified_Temperature" -> R.raw.setthermo
            "Num0" -> R.raw.zero
            "Num1" -> R.raw.one
            "Num2" -> R.raw.two
            "Num3" -> R.raw.three
            "Num4" -> R.raw.four
            "Num5" -> R.raw.five
            "Num6" -> R.raw.six
            "Num7" -> R.raw.seven
            "Num8" -> R.raw.eight
            "Num9" -> R.raw.nine
            else -> R.raw.zero// Default video resource ID
        }

        return "android.resource://" + packageName + "/" + videoResourceId
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
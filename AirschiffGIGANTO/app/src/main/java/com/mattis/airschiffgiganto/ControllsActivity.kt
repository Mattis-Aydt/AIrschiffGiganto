package com.mattis.airschiffgiganto


import android.os.Bundle
import android.util.Log

import android.widget.SeekBar
import android.widget.ToggleButton
import androidx.activity.ComponentActivity
import java.io.IOException
import java.io.OutputStream
import java.util.Timer
import kotlin.concurrent.scheduleAtFixedRate




class ControllsActivity  : ComponentActivity() {

    private val timer = Timer()
    private lateinit var speedBar: SeekBar
    private lateinit var onButton: ToggleButton
    private lateinit var lightsButton: ToggleButton
    private var message_counter = 0

    private var outputStream: OutputStream? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.controlls_activity)

        outputStream = BluetoothConnection.outputStream
        speedBar = findViewById(R.id.speedBar)
        onButton = findViewById(R.id.onButton)
        lightsButton = findViewById(R.id.lightsButton)


        // Schedule a task to update text every second
        timer.scheduleAtFixedRate(0, 100) {
            val flags = ((if (onButton.isChecked) 1 else 0) shl 0) or  // Set lightsOn in bit 0
                    ((if (lightsButton.isChecked) 1 else 0) shl 1)
            val buffer = ByteArray(3)
            buffer[0] = speedBar.progress.toByte() // Speed (1 byte)
            buffer[1] = message_counter.toByte() // Message counter (1 byte)
            buffer[2] = flags.toByte() // Combined lightsOn and on/off (1 byte)
            println(message_counter)
            println(speedBar.progress)
            println(flags)
            message_counter += 1
            sendDataToESP32(buffer)

        }
    }



    private fun sendDataToESP32(data: ByteArray) {
        Log.i("System.out", "trying to send data")
        if (outputStream != null) {
            try {
                outputStream?.write(data)
                Log.i("System.out", "Sent data to ESP32: $data")
            } catch (e: IOException) {
                Log.i("System.out", "Error sending data: ${e.message}")
                e.printStackTrace()
            }
        } else {
            Log.i("System.out", "not connected to ESP32")
        }
    }
}
package com.mattis.airschiffgiganto

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.util.UUID

class MainActivity : ComponentActivity() {


    private lateinit var connectionErrorText: TextView
    private lateinit var connectButton: Button

    private var bluetoothSocket: BluetoothSocket? = null


    private val esp32DeviceName = "ESP32-BT-Slave"
    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Standard SPP UUID

    private val bluetoothPermissions = arrayOf(
        android.Manifest.permission.BLUETOOTH,
        android.Manifest.permission.BLUETOOTH_ADMIN,
        android.Manifest.permission.BLUETOOTH_CONNECT
    )
    private val bluetoothScanPermission = android.Manifest.permission.BLUETOOTH_SCAN

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        connectButton = findViewById(R.id.connectButton)
        connectionErrorText = findViewById(R.id.connectionErrorText)


        connectButton.setOnClickListener {
            val connected = connectToESP32()
            if (connected) {
                val intent = Intent(this, ControllsActivity::class.java)
                startActivity(intent)

            }
        }


    }

    private fun requestPermissions() {
        println("Requesting permissions")
        // Request Bluetooth permissions if they aren't granted
        ActivityCompat.requestPermissions(
            this,
            bluetoothPermissions + bluetoothScanPermission,
            1
        )
    }




    private fun connectToESP32(): Boolean {
        println("Connecting to ESP32")
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            val resultString = "Bluetooth connect permission is not granted."
            println(resultString)
            connectionErrorText.text = resultString
            requestPermissions()
            return false
        }
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

        if (bluetoothAdapter == null) {
            val resultString = "Bluetooth is not supported on this device"
            println(resultString)
            connectionErrorText.text = resultString
            return false
        }

        if (!bluetoothAdapter.isEnabled) {
            val resultString = "Bluetooth is not enabled"
            println(resultString)
            connectionErrorText.text = resultString
            return false
        }

        val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices
        val device = pairedDevices.find { it.name == esp32DeviceName }

        if (device == null) {
            val resultString = "ESP32 device not found. Need to pair with ESP32 first."
            println(resultString)
            connectionErrorText.text = resultString
            return false
        }

        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
            bluetoothSocket?.connect()
            BluetoothConnection.outputStream = bluetoothSocket?.outputStream
            val resultString = "Connected to ESP32"
            println(resultString)
            connectionErrorText.text = resultString
            return true

        } catch (e: IOException) {
            val resultString = "Error connecting to ESP32. Is esp turned on?"
            println(resultString)
            connectionErrorText.text = resultString
            return false
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        // Cancel the timer and close the Bluetooth connection
        bluetoothSocket?.close()
    }
}


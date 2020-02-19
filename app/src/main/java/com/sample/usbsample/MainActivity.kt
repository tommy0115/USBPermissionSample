package com.sample.usbsample

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    companion object {
        const val REQ_PERMISSION_CAMERA = 18
    }

    var manager : UsbManager? = null

    private val receiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                when (intent.action) {
                    UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                        Log.d(USBService.TAG, "ACTION_USB_DEVICE_ATTACHED")

                        manager?.deviceList?.forEach { (t, u) ->
                            Log.d(USBService.TAG, "device : $u")
                        }

                        val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)

                        manager?.hasPermission(device)?:return

                        if (!manager!!.hasPermission(device)) {
                            val permissionIntent = PendingIntent.getForegroundService(
                                context,
                                16,
                                Intent(
                                    context,
                                    USBService::class.java
                                ).setAction(USBService.USB_PERMISSION_RESULT),
                                0
                            )

                            if (checkCallingOrSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                Log.i(USBService.TAG, "camera permission GRANTED")
                            } else {
                                Log.e(USBService.TAG, "camera permission DENIED")
                            }

                            manager?.requestPermission(device, permissionIntent)
                            Log.d(USBService.TAG, "usb requestPermission")
                        } else {
                            Log.e(USBService.TAG, "usb hasPermission true")
                        }
                    }

                    UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                        Log.e(USBService.TAG, "ACTION_USB_DEVICE_DETACHED")
                    }
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        manager = getSystemService(Context.USB_SERVICE) as UsbManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val filter = IntentFilter()
            filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
            registerReceiver(receiver, filter)

            startForegroundService(Intent(this, USBService::class.java))
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (checkCallingOrSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    REQ_PERMISSION_CAMERA
                )
            } else {
                Log.e(USBService.TAG, "denied camera permission")
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(USBService.TAG, "onRequestPermissionsResult")
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, USBService::class.java))

        try {
            unregisterReceiver(receiver)
        } catch (e: Exception) {

        }

    }

}

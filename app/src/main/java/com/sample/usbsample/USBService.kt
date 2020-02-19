package com.sample.usbsample

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.IBinder
import android.text.TextUtils
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

class USBService : Service() {

    companion object{
        const val TAG = "USBService"
        const val USB_PERMISSION_RESULT = "com.usb.permission.result"
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val remoteViews = RemoteViews(packageName, R.layout.notification_service)

        val builder: NotificationCompat.Builder = if (Build.VERSION.SDK_INT >= 26) {
            val channelId = "snwodeer_service_channel"
            val channel = NotificationChannel(
                channelId,
                "SnowDeer Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)

            NotificationCompat.Builder(this, channelId)

        } else {
            NotificationCompat.Builder(this)
        }

        builder.setSmallIcon(R.mipmap.ic_launcher)
            .setContent(remoteViews)
            .setContentIntent(pendingIntent)

        startForeground(1, builder.build())

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            when (intent.action){
                USB_PERMISSION_RESULT -> {
                    val manager = getSystemService(Context.USB_SERVICE) as UsbManager
                    val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)

                    Log.i(TAG,"USB_PERMISSION_RESULT "+"${deviceName(device)} hasPermission=${manager.hasPermission(device)} ")
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
    }

    private fun deviceName(device: UsbDevice?): String {
        var result = "device is null"
        if (device != null) {
            result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (!TextUtils.isEmpty(device.productName)) device.productName!! else device.deviceName
            } else {
                device.deviceName
            }
        }
        return result
    }
}
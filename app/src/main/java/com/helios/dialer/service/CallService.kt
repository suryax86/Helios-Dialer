package com.helios.dialer.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import androidx.core.app.NotificationCompat
import com.helios.dialer.MainActivity
import com.helios.dialer.R

class CallService : InCallService() {
    private var flipListener: FlipToSilenceListener? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        flipListener = FlipToSilenceListener(this)
        createIncomingChannel()
    }

    override fun onDestroy() {
        flipListener?.stop()
        flipListener = null
        instance = null
        super.onDestroy()
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        CallManager.addCall(call)

        if (call.state == Call.STATE_RINGING) {
            flipListener?.start()
            showIncomingCallNotification(call)
        }
    }

    override fun onCallRemoved(call: Call) {
        cancelIncomingCallNotification()
        flipListener?.stop()
        CallManager.removeCall(call)
        super.onCallRemoved(call)
    }

    override fun onCallAudioStateChanged(audioState: CallAudioState) {
        CallManager.updateAudioState(audioState)
        super.onCallAudioStateChanged(audioState)
    }

    fun setMuteState(muted: Boolean) {
        setMuted(muted)
    }

    fun setAudioRouteState(route: Int) {
        setAudioRoute(route)
    }

    private fun createIncomingChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java)
        val ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        val channel = NotificationChannel(
            INCOMING_CHANNEL,
            "Incoming calls",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Incoming Helios calls"
            setSound(
                ringtone,
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
    }

    private fun showIncomingCallNotification(call: Call) {
        val number = call.details.handle?.schemeSpecificPart ?: "Unknown number"
        val intent = Intent(this, MainActivity::class.java).apply {
            action = MainActivity.ACTION_SHOW_INCOMING_CALL
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            901,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, INCOMING_CHANNEL)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Incoming call")
            .setContentText(number)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(pendingIntent, true)
            .setContentIntent(pendingIntent)
            .build()

        getSystemService(NotificationManager::class.java).notify(INCOMING_NOTIFICATION_ID, notification)
    }

    private fun cancelIncomingCallNotification() {
        getSystemService(NotificationManager::class.java).cancel(INCOMING_NOTIFICATION_ID)
    }

    companion object {
        private const val INCOMING_CHANNEL = "incoming_calls"
        private const val INCOMING_NOTIFICATION_ID = 901
        var instance: CallService? = null
            private set
    }
}

package com.helios.dialer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.helios.dialer.IncomingCallActivity
import com.helios.dialer.R

class CallService : InCallService() {

    private var flipListener: FlipToSilenceListener? = null

    override fun onCreate() {
        super.onCreate()

        instance = this

        flipListener =
            FlipToSilenceListener(this)

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
            launchIncomingCallActivity()
        }
    }

    override fun onCallRemoved(call: Call) {
        cancelIncomingCallNotification()

        flipListener?.stop()

        CallManager.removeCall(call)

        super.onCallRemoved(call)
    }

    override fun onCallAudioStateChanged(
        audioState: CallAudioState
    ) {
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val ringtone =
            RingtoneManager.getDefaultUri(
                RingtoneManager.TYPE_RINGTONE
            )

        val channel = NotificationChannel(
            INCOMING_CHANNEL,
            "Incoming calls",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Incoming Helios calls"

            setSound(
                ringtone,
                AudioAttributes.Builder()
                    .setUsage(
                        AudioAttributes.USAGE_NOTIFICATION_RINGTONE
                    )
                    .setContentType(
                        AudioAttributes.CONTENT_TYPE_SONIFICATION
                    )
                    .build()
            )

            enableVibration(true)
        }

        getSystemService(
            NotificationManager::class.java
        ).createNotificationChannel(channel)
    }

    private fun showIncomingCallNotification(
        call: Call
    ) {
        val number =
            call.details.handle
                ?.schemeSpecificPart
                ?: "Unknown number"

        val fullScreenIntent =
            Intent(
                this,
                IncomingCallActivity::class.java
            ).apply {
                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

        val fullScreenPendingIntent =
            PendingIntent.getActivity(
                this,
                REQUEST_FULLSCREEN,
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
            )

        val answerIntent =
            Intent(
                this,
                CallActionReceiver::class.java
            ).apply {
                action =
                    CallActionReceiver.ACTION_ANSWER
            }

        val declineIntent =
            Intent(
                this,
                CallActionReceiver::class.java
            ).apply {
                action =
                    CallActionReceiver.ACTION_DECLINE
            }

        val answerPendingIntent =
            PendingIntent.getBroadcast(
                this,
                REQUEST_ANSWER,
                answerIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
            )

        val declinePendingIntent =
            PendingIntent.getBroadcast(
                this,
                REQUEST_DECLINE,
                declineIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
            )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val person =
                android.app.Person.Builder()
                    .setName(number)
                    .build()

            val notification =
                Notification.CallStyle
                    .forIncomingCall(
                        person,
                        declinePendingIntent,
                        answerPendingIntent
                    )
                    .setContentIntent(
                        fullScreenPendingIntent
                    )
                    .setFullScreenIntent(
                        fullScreenPendingIntent,
                        true
                    )
                    .build()

            getSystemService(
                NotificationManager::class.java
            ).notify(
                INCOMING_NOTIFICATION_ID,
                notification
            )
        } else {
            val notification =
                NotificationCompat.Builder(
                    this,
                    INCOMING_CHANNEL
                )
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Incoming call")
                    .setContentText(number)
                    .setCategory(
                        NotificationCompat.CATEGORY_CALL
                    )
                    .setPriority(
                        NotificationCompat.PRIORITY_MAX
                    )
                    .setOngoing(true)
                    .setAutoCancel(false)
                    .setFullScreenIntent(
                        fullScreenPendingIntent,
                        true
                    )
                    .addAction(
                        NotificationCompat.Action(
                            0,
                            "Decline",
                            declinePendingIntent
                        )
                    )
                    .addAction(
                        NotificationCompat.Action(
                            0,
                            "Answer",
                            answerPendingIntent
                        )
                    )
                    .build()

            NotificationManagerCompat.from(this)
                .notify(
                    INCOMING_NOTIFICATION_ID,
                    notification
                )
        }
    }

    private fun launchIncomingCallActivity() {
        val intent =
            Intent(
                this,
                IncomingCallActivity::class.java
            ).apply {
                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            }

        startActivity(intent)
    }

    private fun cancelIncomingCallNotification() {
        NotificationManagerCompat.from(this)
            .cancel(INCOMING_NOTIFICATION_ID)
    }

    companion object {
        private const val INCOMING_CHANNEL =
            "incoming_calls"

        private const val INCOMING_NOTIFICATION_ID =
            901

        private const val REQUEST_FULLSCREEN =
            902

        private const val REQUEST_ANSWER =
            903

        private const val REQUEST_DECLINE =
            904

        var instance: CallService? = null
            private set
    }
}

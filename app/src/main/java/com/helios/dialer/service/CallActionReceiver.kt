package com.helios.dialer.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class CallActionReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent?
    ) {
        when (intent?.action) {
            ACTION_ANSWER -> CallManager.answer()
            ACTION_DECLINE -> CallManager.reject()
        }
    }

    companion object {
        const val ACTION_ANSWER =
            "com.helios.dialer.action.ANSWER_CALL"

        const val ACTION_DECLINE =
            "com.helios.dialer.action.DECLINE_CALL"
    }
}

package com.helios.dialer.data.repository

import android.content.Context
import android.provider.CallLog
import com.helios.dialer.data.model.CallLogEntry

class RecentsRepository(private val context: Context) {
    fun getRecents(): List<CallLogEntry> = fetchCallLogs()

    fun fetchCallLogs(): List<CallLogEntry> {
        val logs = mutableListOf<CallLogEntry>()
        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION
        )

        context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            null,
            null,
            "${CallLog.Calls.DATE} DESC"
        )?.use { cursor ->
            val idIdx = cursor.getColumnIndex(CallLog.Calls._ID)
            val numberIdx = cursor.getColumnIndex(CallLog.Calls.NUMBER)
            val nameIdx = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val typeIdx = cursor.getColumnIndex(CallLog.Calls.TYPE)
            val dateIdx = cursor.getColumnIndex(CallLog.Calls.DATE)
            val durationIdx = cursor.getColumnIndex(CallLog.Calls.DURATION)

            while (cursor.moveToNext()) {
                val number = if (numberIdx >= 0) cursor.getString(numberIdx).orEmpty() else ""
                if (number.isBlank()) continue
                logs += CallLogEntry(
                    id = if (idIdx >= 0) cursor.getString(idIdx) else "${dateIdx}:${number}",
                    number = number,
                    name = if (nameIdx >= 0) cursor.getString(nameIdx) else null,
                    type = if (typeIdx >= 0) cursor.getInt(typeIdx) else CallLog.Calls.INCOMING_TYPE,
                    timestamp = if (dateIdx >= 0) cursor.getLong(dateIdx) else 0L,
                    duration = if (durationIdx >= 0) cursor.getLong(durationIdx) else 0L
                )
                if (logs.size >= 100) break
            }
        }
        return logs
    }
}

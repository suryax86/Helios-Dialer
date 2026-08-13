package com.helios.dialer.ui.screens.recents

import android.provider.CallLog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.PhoneForwarded
import androidx.compose.material.icons.filled.PhoneMissed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.helios.dialer.data.model.CallLogEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecentsScreen(
    recents: List<CallLogEntry>,
    onCall: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Recents", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                Text("Your latest calls", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            }
        }

        if (recents.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.History, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.size(12.dp))
                    Text("No recent calls", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                items(recents, key = { it.id }) { log ->
                    RecentRow(log, onCall)
                }
            }
        }
    }
}

@Composable
private fun RecentRow(log: CallLogEntry, onCall: (String) -> Unit) {
    val type = when (log.type) {
        CallLog.Calls.MISSED_TYPE -> "Missed"
        CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
        CallLog.Calls.REJECTED_TYPE -> "Declined"
        else -> "Incoming"
    }
    val icon = when (log.type) {
        CallLog.Calls.MISSED_TYPE -> Icons.Filled.PhoneMissed
        CallLog.Calls.OUTGOING_TYPE -> Icons.Filled.PhoneForwarded
        else -> Icons.Filled.PhoneCallback
    }
    val date = remember(log.timestamp) {
        SimpleDateFormat("dd MMM · HH:mm", Locale.getDefault()).format(Date(log.timestamp))
    }
    val title = log.name?.takeIf { it.isNotBlank() } ?: log.number

    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).clickable { onCall(log.number) }.padding(vertical = 11.dp, horizontal = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, type, tint = if (log.type == CallLog.Calls.MISSED_TYPE) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.size(13.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, fontSize = 16.sp)
            Text(
                "$type · ${formatDuration(log.duration)}${if (log.name != null) " · ${log.number}" else ""}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(date, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            IconButton(onClick = { onCall(log.number) }) {
                Icon(Icons.Filled.Call, "Call $title", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

private fun formatDuration(seconds: Long): String {
    if (seconds <= 0) return ""
    val minutes = seconds / 60
    val remaining = seconds % 60
    return if (minutes > 0) "${minutes}m ${remaining}s" else "${remaining}s"
}

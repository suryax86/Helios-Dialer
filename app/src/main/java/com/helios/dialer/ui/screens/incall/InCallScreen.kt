package com.helios.dialer.ui.screens.incall

import android.telecom.Call
import android.telecom.CallAudioState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.helios.dialer.data.repository.ContactsRepository
import com.helios.dialer.service.CallManager
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun InCallScreen(call: Call?) {
    if (call == null) return

    val state by CallManager.callState.collectAsState()
    val muted by CallManager.isMuted.collectAsState()
    val route by CallManager.audioRoute.collectAsState()
    var showKeypad by remember { mutableStateOf(false) }
    var elapsed by remember { mutableLongStateOf(0L) }

    LaunchedEffect(call, state) {
        while (true) {
            val connected = call.details.connectTimeMillis
            elapsed = if (connected > 0L) System.currentTimeMillis() - connected else 0L
            delay(1000)
        }
    }

    val number = call.details.handle?.schemeSpecificPart ?: "Unknown number"
    val context = LocalContext.current
    var contactName by remember(number) { mutableStateOf<String?>(null) }

    LaunchedEffect(number) {
        contactName = withContext(Dispatchers.IO) {
            runCatching { ContactsRepository(context).findName(number) }.getOrNull()
        }
    }

    val title = contactName ?: "Unknown caller"
    val isRinging = state == Call.STATE_RINGING
    val isActive = state == Call.STATE_ACTIVE
    val status = when (state) {
        Call.STATE_RINGING -> "Incoming call"
        Call.STATE_DIALING -> "Calling"
        Call.STATE_CONNECTING -> "Connecting"
        Call.STATE_ACTIVE -> formatElapsed(elapsed)
        Call.STATE_HOLDING -> "On hold"
        else -> "Call"
    }

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 26.dp, vertical = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(0.7f))

            Box(
                Modifier.size(112.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    title.first().toString(),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(Modifier.size(18.dp))
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Text(number, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
            Spacer(Modifier.size(8.dp))
            Text(status, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)

            Spacer(Modifier.weight(1f))

            AnimatedVisibility(visible = showKeypad) {
                DtmfPad()
            }

            if (isRinging) {
                Row(horizontalArrangement = Arrangement.spacedBy(34.dp), verticalAlignment = Alignment.CenterVertically) {
                    CallActionButton(
                        icon = Icons.Filled.CallEnd,
                        label = "Decline",
                        tint = MaterialTheme.colorScheme.error,
                        onClick = { CallManager.reject() }
                    )
                    CallActionButton(
                        icon = Icons.Filled.Mic,
                        label = "Answer",
                        tint = MaterialTheme.colorScheme.primary,
                        onClick = { CallManager.answer() }
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CallToggleButton(Icons.Filled.MicOff, if (muted) "Unmute" else "Mute", muted) {
                        CallManager.setMuted(!muted)
                    }
                    CallToggleButton(Icons.Filled.VolumeUp, if (route == CallAudioState.ROUTE_SPEAKER) "Earpiece" else "Speaker", route == CallAudioState.ROUTE_SPEAKER) {
                        CallManager.setAudioRoute(
                            if (route == CallAudioState.ROUTE_SPEAKER) CallAudioState.ROUTE_EARPIECE else CallAudioState.ROUTE_SPEAKER
                        )
                    }
                    CallToggleButton(Icons.Filled.Dialpad, "Keypad", showKeypad) {
                        showKeypad = !showKeypad
                    }
                }

                Spacer(Modifier.size(28.dp))

                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.error,
                    onClick = { CallManager.disconnect() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.CallEnd, "End call", tint = MaterialTheme.colorScheme.onError, modifier = Modifier.size(30.dp))
                    }
                }
            }

            Spacer(Modifier.weight(0.55f))
        }
    }
}

@Composable
private fun CallToggleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(62.dp),
            shape = CircleShape,
            color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
            onClick = onClick
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, label, tint = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
            }
        }
        Spacer(Modifier.size(6.dp))
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CallActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(modifier = Modifier.size(76.dp), shape = CircleShape, color = tint, onClick = onClick) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, label, tint = MaterialTheme.colorScheme.surface, modifier = Modifier.size(31.dp))
            }
        }
        Spacer(Modifier.size(7.dp))
        Text(label, fontSize = 13.sp)
    }
}

@Composable
private fun DtmfPad() {
    val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "0", "#")
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(MaterialTheme.colorScheme.surfaceContainerLow).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        keys.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                row.forEach { digit ->
                    Surface(
                        modifier = Modifier.size(52.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        onClick = { CallManager.sendDtmf(digit.first()) }
                    ) {
                        Box(contentAlignment = Alignment.Center) { Text(digit, fontSize = 18.sp) }
                    }
                }
            }
        }
        Spacer(Modifier.size(2.dp))
    }
}

private fun formatElapsed(milliseconds: Long): String {
    val totalSeconds = (milliseconds / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}

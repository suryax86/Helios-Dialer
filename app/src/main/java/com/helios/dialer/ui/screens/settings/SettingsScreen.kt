package com.helios.dialer.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.helios.dialer.data.SettingsManager
import androidx.compose.ui.platform.LocalContext

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    defaultDialer: Boolean,
    onSetDefaultDialer: () -> Unit
) {
    val context = LocalContext.current
    var flipToSilence by remember { mutableStateOf(SettingsManager.getBoolean(context, "flipToSilenceEnabled", true)) }
    var inCallNotes by remember { mutableStateOf(SettingsManager.getBoolean(context, "inCallNotesEnabled", false)) }

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") }
            Text("Settings", style = MaterialTheme.typography.headlineSmall)
        }

        Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Phone, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Default phone app")
                Text(
                    if (defaultDialer) "Helios handles your calls" else "Helios is not the default dialer",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!defaultDialer) Button(onClick = onSetDefaultDialer) { Text("Set") }
        }

        Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Security, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Flip to silence")
                Text("Silence a ringing call by turning the phone face down", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = flipToSilence, onCheckedChange = {
                flipToSilence = it
                SettingsManager.setBoolean(context, "flipToSilenceEnabled", it)
            })
        }

        Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.size(14.dp))
            Column(Modifier.weight(1f)) {
                Text("In-call quick notes")
                Text("Keep the option available for future call notes", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = inCallNotes, onCheckedChange = {
                inCallNotes = it
                SettingsManager.setBoolean(context, "inCallNotesEnabled", it)
            })
        }

        Spacer(Modifier.size(18.dp))
        Text("Helios Dialer", style = MaterialTheme.typography.titleMedium)
        Text("A local-first phone experience built around Android Telecom.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

package com.helios.dialer.ui.screens.contacts

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.helios.dialer.data.model.Contact
import com.helios.dialer.domain.T9SearchEngine

@Composable
fun ContactsScreen(
    allContacts: List<Contact>,
    onCall: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(allContacts, query) {
        T9SearchEngine.filter(allContacts, query)
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(horizontal = 18.dp)
    ) {
        Text(
            "Contacts",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 18.dp, bottom = 14.dp)
        )

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            placeholder = { Text("Search contacts or numbers") },
            leadingIcon = { Icon(Icons.Filled.Search, null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Filled.Close, "Clear search")
                    }
                }
            }
        )

        Spacer(Modifier.size(12.dp))

        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Contacts, null, modifier = Modifier.size(46.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.size(12.dp))
                    Text(
                        if (allContacts.isEmpty()) "No contacts found" else "No matches",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    )
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                items(filtered, key = { "${it.id}:${it.number}" }) { contact ->
                    ContactRow(contact, onCall)
                }
            }
        }
    }
}

@Composable
private fun ContactRow(contact: Contact, onCall: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).clickable { onCall(contact.number) }.padding(vertical = 11.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val initial = contact.name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(initial, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.size(13.dp))
        Column(Modifier.weight(1f)) {
            Text(contact.name, fontWeight = FontWeight.Medium, fontSize = 16.sp)
            Text(contact.number, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }
        IconButton(onClick = { onCall(contact.number) }) {
            Icon(Icons.Filled.Call, "Call ${contact.name}", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

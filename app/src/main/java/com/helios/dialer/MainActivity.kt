package com.helios.dialer

import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telecom.TelecomManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Keypad
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.helios.dialer.data.model.CallLogEntry
import com.helios.dialer.data.model.Contact
import com.helios.dialer.data.repository.ContactsRepository
import com.helios.dialer.data.repository.RecentsRepository
import com.helios.dialer.service.CallManager
import com.helios.dialer.ui.screens.contacts.ContactsScreen
import com.helios.dialer.ui.screens.incall.InCallScreen
import com.helios.dialer.ui.screens.keypad.KeypadScreen
import com.helios.dialer.ui.screens.recents.RecentsScreen
import com.helios.dialer.ui.screens.settings.SettingsScreen
import com.helios.dialer.ui.theme.HeliosTheme

class MainActivity : ComponentActivity() {
    private var incomingCallRequested = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        incomingCallRequested = savedInstanceState?.getBoolean(KEY_INCOMING, false) == true
        setContent {
            HeliosTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HeliosRoot(
                        initialNumber = intent?.data?.schemeSpecificPart.orEmpty(),
                        incomingCallRequested = incomingCallRequested,
                        onRequestDefaultDialer = { requestDefaultDialer() }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(KEY_INCOMING, incomingCallRequested)
        super.onSaveInstanceState(outState)
    }

    private fun requestDefaultDialer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            if (roleManager.isRoleAvailable(RoleManager.ROLE_DIALER) &&
                !roleManager.isRoleHeld(RoleManager.ROLE_DIALER)
            ) {
                startActivityForResult(roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER), ROLE_REQUEST)
            }
        } else {
            val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
            }
            startActivity(intent)
        }
    }

    companion object {
        const val ACTION_SHOW_INCOMING_CALL = "com.helios.dialer.SHOW_INCOMING_CALL"
        private const val ROLE_REQUEST = 410
        private const val KEY_INCOMING = "incoming_call_requested"
    }
}

@Composable
private fun HeliosRoot(
    initialNumber: String,
    incomingCallRequested: Boolean,
    onRequestDefaultDialer: () -> Unit
) {
    val context = LocalContext.current
    val activeCall by CallManager.currentCall.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var contacts by remember { mutableStateOf(emptyList<Contact>()) }
    var recents by remember { mutableStateOf(emptyList<CallLogEntry>()) }
    var defaultDialer by remember { mutableStateOf(isDefaultDialer(context)) }
    var pendingNumber by remember { mutableStateOf<String?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }

    val callPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val number = pendingNumber
        pendingNumber = null
        if (granted && !number.isNullOrBlank()) {
            placeCall(context, number)
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(defaultDialer) {
        if (!defaultDialer) onRequestDefaultDialer()
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(selectedTab) {
        defaultDialer = isDefaultDialer(context)
        when (selectedTab) {
            1 -> recents = runCatching { RecentsRepository(context).getRecents() }.getOrDefault(emptyList())
            2 -> contacts = runCatching { ContactsRepository(context).getContacts() }.getOrDefault(emptyList())
        }
    }

    LaunchedEffect(activeCall) {
        if (activeCall == null && selectedTab == 1) {
            recents = runCatching { RecentsRepository(context).getRecents() }.getOrDefault(emptyList())
        }
    }

    if (activeCall != null) {
        InCallScreen(call = activeCall)
        return
    }

    val callNumber: (String) -> Unit = { number ->
        defaultDialer = isDefaultDialer(context)
        if (!defaultDialer) {
            onRequestDefaultDialer()
        } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            pendingNumber = number
            callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
        } else {
            placeCall(context, number)
        }
    }

    Scaffold(
        topBar = {
            if (selectedTab == 0) {
                androidx.compose.material3.SmallTopAppBar(
                    title = { Text("Helios Dialer") },
                    actions = {
                        if (!defaultDialer) {
                            androidx.compose.material3.IconButton(onClick = onRequestDefaultDialer) {
                                Icon(Icons.Filled.Settings, contentDescription = "Set as default phone app")
                            }
                        }
                        androidx.compose.material3.IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                onClick = { menuExpanded = false; selectedTab = 3 }
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (selectedTab != 3) {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Filled.Keypad, contentDescription = "Keypad") },
                        label = { Text("Keypad") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Filled.History, contentDescription = "Recents") },
                        label = { Text("Recents") }
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Filled.Contacts, contentDescription = "Contacts") },
                        label = { Text("Contacts") }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (selectedTab) {
                0 -> KeypadScreen(initialNumber = initialNumber, onCall = callNumber)
                1 -> RecentsScreen(recents = recents, onCall = callNumber)
                2 -> ContactsScreen(allContacts = contacts, onCall = callNumber)
                3 -> SettingsScreen(onBack = { selectedTab = 0 }, defaultDialer = defaultDialer, onSetDefaultDialer = onRequestDefaultDialer)
            }
        }
    }
}

private fun isDefaultDialer(context: android.content.Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val roleManager = context.getSystemService(RoleManager::class.java)
        roleManager.isRoleHeld(RoleManager.ROLE_DIALER)
    } else {
        val telecom = context.getSystemService(TelecomManager::class.java)
        telecom.defaultDialerPackage == context.packageName
    }
}

private fun placeCall(context: android.content.Context, number: String) {
    val telecomManager = context.getSystemService(TelecomManager::class.java)
    runCatching {
        telecomManager.placeCall(Uri.parse("tel:${Uri.encode(number)}"), Bundle())
    }
}

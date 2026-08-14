package com.helios.dialer.ui.screens.keypad

import android.provider.CallLog
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PhoneMissed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.helios.dialer.data.model.CallLogEntry
import com.helios.dialer.data.model.Contact
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class DialKey(
    val main: String,
    val letters: String = ""
)

private val keys = listOf(
    DialKey("1"),
    DialKey("2", "ABC"),
    DialKey("3", "DEF"),
    DialKey("4", "GHI"),
    DialKey("5", "JKL"),
    DialKey("6", "MNO"),
    DialKey("7", "PQRS"),
    DialKey("8", "TUV"),
    DialKey("9", "WXYZ"),
    DialKey("*"),
    DialKey("0", "+"),
    DialKey("#")
)

@Composable
fun KeypadScreen(
    initialNumber: String = "",
    recents: List<CallLogEntry> = emptyList(),
    contacts: List<Contact> = emptyList(),
    onCall: (String) -> Unit = {}
) {
    var number by remember { mutableStateOf(initialNumber) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(initialNumber) {
        if (initialNumber.isNotBlank()) {
            number = initialNumber
        }
    }

    val missedCall = remember(recents) {
        recents.firstOrNull { it.type == CallLog.Calls.MISSED_TYPE }
    }

    val searchResults = remember(number, recents) {
        if (number.isBlank()) {
            emptyList()
        } else {
            val query = number.filter { it.isDigit() || it == '+' }

            recents
                .filter { log ->
                    val normalizedNumber = log.number.filter { it.isDigit() || it == '+' }
                    val name = log.name.orEmpty()

                    normalizedNumber.contains(query) ||
                        name.contains(query, ignoreCase = true)
                }
                .take(5)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            HeroHeader(
                number = number,
                missedCall = missedCall,
                onCall = onCall
            )
        }

        if (number.isNotBlank() && searchResults.isNotEmpty()) {
            item {
                Text(
                    "Recent matches",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                )
            }

            items(
                searchResults,
                key = { "search:${it.id}" }
            ) { log ->
                SearchResultRow(
                    log = log,
                    onCall = onCall
                )
            }
        }

        item {
            NumberDisplay(number)
        }

        item {
            Dialpad(
                number = number,
                onNumberChange = { number = it },
                haptic = haptic
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 22.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (number.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                number = number.dropLast(1)
                            }
                        ) {
                            Icon(
                                Icons.Filled.Backspace,
                                contentDescription = "Delete last digit",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    tonalElevation = 4.dp,
                    onClick = {
                        if (number.isNotBlank()) {
                            onCall(number)
                        }
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.Call,
                            contentDescription = "Call $number",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Box(Modifier.size(72.dp))
            }
        }
    }
}

@Composable
private fun HeroHeader(
    number: String,
    missedCall: CallLogEntry?,
    onCall: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp)
    ) {
        AnimatedContent(
            targetState = number.isBlank(),
            label = "hero_title"
        ) { empty ->
            Column {
                Text(
                    text = if (empty) "Helios" else "Dial",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (empty) {
                        "Your calls, without the clutter."
                    } else {
                        "Find a recent call or enter a number."
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        AnimatedVisibility(
            visible = number.isBlank() && missedCall != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            missedCall?.let {
                MissedCallCard(
                    log = it,
                    onCall = onCall
                )
            }
        }
    }
}

@Composable
private fun MissedCallCard(
    log: CallLogEntry,
    onCall: (String) -> Unit
) {
    val transition = rememberInfiniteTransition(label = "missed_gradient")
    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_shift"
    )

    val primary = MaterialTheme.colorScheme.error
    val secondary = MaterialTheme.colorScheme.primary

    val brush = Brush.linearGradient(
        colors = listOf(
            primary.copy(alpha = 0.92f),
            secondary.copy(alpha = 0.82f),
            primary.copy(alpha = 0.72f)
        ),
        start = androidx.compose.ui.geometry.Offset(
            x = shift * 500f,
            y = 0f
        ),
        end = androidx.compose.ui.geometry.Offset(
            x = 500f + shift * 300f,
            y = 500f
        )
    )

    val title = log.name?.takeIf { it.isNotBlank() } ?: log.number
    val date = SimpleDateFormat(
        "dd MMM · HH:mm",
        Locale.getDefault()
    ).format(Date(log.timestamp))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(brush)
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.PhoneMissed,
                        contentDescription = "Missed call",
                        tint = Color.White,
                        modifier = Modifier.size(27.dp)
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "Missed call",
                        color = Color.White.copy(alpha = 0.78f),
                        fontSize = 12.sp
                    )

                    Text(
                        title,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        if (log.name.isNullOrBlank()) {
                            "$date · ${log.number}"
                        } else {
                            "$date · ${log.number}"
                        },
                        color = Color.White.copy(alpha = 0.78f),
                        fontSize = 12.sp
                    )
                }

                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.16f),
                    onClick = { onCall(log.number) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.Call,
                            contentDescription = "Call $title",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NumberDisplay(number: String) {
    AnimatedContent(
        targetState = number,
        label = "number_display"
    ) { value ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (value.isBlank()) 38.dp else 64.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (value.isBlank()) "Enter a number" else value,
                color = if (value.isBlank()) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                fontSize = if (value.isBlank()) 16.sp else 31.sp,
                fontWeight = if (value.isBlank()) {
                    FontWeight.Normal
                } else {
                    FontWeight.Light
                },
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SearchResultRow(
    log: CallLogEntry,
    onCall: (String) -> Unit
) {
    val title = log.name?.takeIf { it.isNotBlank() } ?: log.number
    val date = SimpleDateFormat(
        "dd MMM · HH:mm",
        Locale.getDefault()
    ).format(Date(log.timestamp))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    title,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    "${log.number} · $date",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            IconButton(
                onClick = { onCall(log.number) }
            ) {
                Icon(
                    Icons.Filled.Call,
                    contentDescription = "Call $title",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun Dialpad(
    number: String,
    onNumberChange: (String) -> Unit,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        keys.chunked(3).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                row.forEach { key ->
                    DialKeyButton(
                        key = key,
                        onClick = {
                            onNumberChange(number + key.main)
                            haptic.performHapticFeedback(
                                HapticFeedbackType.TextHandleMove
                            )
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DialKeyButton(
    key: DialKey,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .size(84.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .combinedClickable(
                onClick = onClick,
                onLongClick = if (key.main == "0") {
                    { onClick() }
                } else {
                    null
                }
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            key.main,
            fontSize = 28.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (key.letters.isNotEmpty()) {
            Text(
                key.letters,
                fontSize = 9.sp,
                letterSpacing = 1.2.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

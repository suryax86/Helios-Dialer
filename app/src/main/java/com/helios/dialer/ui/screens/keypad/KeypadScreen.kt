package com.helios.dialer.ui.screens.keypad

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class DialKey(val main: String, val letters: String = "")

private val keys = listOf(
    DialKey("1"), DialKey("2", "ABC"), DialKey("3", "DEF"),
    DialKey("4", "GHI"), DialKey("5", "JKL"), DialKey("6", "MNO"),
    DialKey("7", "PQRS"), DialKey("8", "TUV"), DialKey("9", "WXYZ"),
    DialKey("*"), DialKey("0", "+"), DialKey("#")
)

@Composable
fun KeypadScreen(
    initialNumber: String = "",
    onCall: (String) -> Unit = {}
) {
    var number by remember { mutableStateOf(initialNumber) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(initialNumber) {
        if (initialNumber.isNotBlank()) number = initialNumber
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(12.dp))

        AnimatedContent(
            targetState = number,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "number_display"
        ) { value ->
            Box(
                modifier = Modifier.fillMaxWidth().height(86.dp),
                contentAlignment = Alignment.Center
            ) {
                if (value.isBlank()) {
                    Text(
                        "Enter number",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 20.sp
                    )
                } else {
                    Text(
                        value,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Light,
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        if (number.isNotBlank()) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                shape = RoundedCornerShape(50)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Search, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(7.dp))
                    Text("T9 search", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            keys.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    row.forEach { key ->
                        DialKeyButton(
                            key = key,
                            onClick = {
                                number += key.main
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                if (number.isNotEmpty()) {
                    IconButton(onClick = { number = number.dropLast(1) }) {
                        Icon(Icons.Filled.Backspace, "Delete last digit", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Surface(
                modifier = Modifier.size(72.dp).clip(CircleShape),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                tonalElevation = 4.dp,
                onClick = { if (number.isNotBlank()) onCall(number) }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Call, "Call $number", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(30.dp))
                }
            }

            Box(Modifier.size(72.dp))
        }

        Spacer(Modifier.height(16.dp))
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
                onLongClick = if (key.main == "0") ({ onClick() }) else null
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

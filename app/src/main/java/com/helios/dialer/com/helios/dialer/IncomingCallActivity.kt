package com.helios.dialer

import android.os.Build
import android.os.Bundle
import android.telecom.Call
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.helios.dialer.service.CallManager
import com.helios.dialer.ui.theme.HeliosTheme

class IncomingCallActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        setContent {
            HeliosTheme {
                val call by CallManager.currentCall.collectAsState()

                if (
                    call == null ||
                    call?.state != Call.STATE_RINGING
                ) {
                    finish()
                } else {
                    val number =
                        call?.details?.handle
                            ?.schemeSpecificPart
                            ?: "Unknown number"

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(28.dp),
                        verticalArrangement =
                            Arrangement.Center,
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Incoming call",
                            style =
                                MaterialTheme.typography
                                    .headlineSmall
                        )

                        Text(
                            number,
                            style =
                                MaterialTheme.typography
                                    .headlineMedium,
                            modifier =
                                Modifier.padding(
                                    top = 16.dp,
                                    bottom = 48.dp
                                )
                        )

                        Row(
                            modifier =
                                Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedButton(
                                modifier =
                                    Modifier.weight(1f),
                                onClick = {
                                    CallManager.reject()
                                    finish()
                                }
                            ) {
                                Text("Decline")
                            }

                            Button(
                                modifier =
                                    Modifier.weight(1f),
                                onClick = {
                                    CallManager.answer()
                                }
                            ) {
                                Text("Answer")
                            }
                        }
                    }
                }
            }
        }
    }

    @Deprecated(
        "Use OnBackInvokedDispatcher on newer Android versions"
    )
    override fun onBackPressed() {
        moveTaskToBack(true)
    }
}

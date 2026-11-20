package com.goreecloud.keyboard

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.goreecloud.ui.material3.GoreeAccent
import com.goreecloud.ui.material3.GoreeCloudExpressiveTheme

/** First-run activation is always a user action through system-owned keyboard settings. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settings = remember { KeyboardPreferences(applicationContext) }
            var accent by remember { mutableStateOf(settings.accent) }
            var haptics by remember { mutableStateOf(settings.haptics) }
            GoreeCloudExpressiveTheme(accent = accent) {
                val context = LocalContext.current
                val imm = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                Scaffold { insets ->
                    Column(
                        Modifier.fillMaxSize().padding(insets).verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Text("GoreeCloud Keyboard", style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold)
                        Text("Your words stay on your device.", style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary)
                        Text("A native, offline keyboard. No account, advertisements, keystroke logging or network permission.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        HorizontalDivider()
                        Text("Get started", style = MaterialTheme.typography.titleLarge)
                        Text("1. Enable GoreeCloud Keyboard in Android's keyboard settings. Android warns about all third-party keyboards because they can receive what you type.")
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) }
                        ) { Text("Enable keyboard") }
                        Text("2. Choose GoreeCloud Keyboard as your active input method.")
                        OutlinedButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { imm.showInputMethodPicker() }
                        ) { Text("Choose keyboard") }
                        HorizontalDivider()
                        Text("Appearance", style = MaterialTheme.typography.titleLarge)
                        Text("GoreeCloud UI • Material 3 Expressive", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        GoreeAccent.entries.forEach { candidate ->
                            FilterChip(
                                selected = accent == candidate,
                                onClick = { settings.accent = candidate; accent = candidate },
                                label = { Text(candidate.name) }
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Keypress vibration", style = MaterialTheme.typography.titleMedium)
                                Text("Off by default", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = haptics, onCheckedChange = {
                                settings.haptics = it
                                haptics = it
                            })
                        }
                        HorizontalDivider()
                        Text("Privacy and limitations", style = MaterialTheme.typography.titleLarge)
                        Text("This development build has no Internet permission and does not store typed text. Passwords are not learned. Advanced autocorrect, swipe typing, voice input, multilingual layouts, clipboard history and GIF search are not implemented.")
                        Text("Keyboard access is sensitive. Only activate builds that you trust and verify independently.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

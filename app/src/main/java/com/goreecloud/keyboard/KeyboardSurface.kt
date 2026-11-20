package com.goreecloud.keyboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

sealed interface KeyAction {
    data class Text(val value: String) : KeyAction
    data object Space : KeyAction
    data object Backspace : KeyAction
    data object Enter : KeyAction
    data object SwitchKeyboard : KeyAction
}

private enum class Layer { LETTERS, SYMBOLS, EMOJI }
private data class KeySpec(
    val label: String,
    val value: String = label,
    val weight: Float = 1f,
    val description: String = label,
    val special: Boolean = false,
    val action: KeyAction? = null
)

@Composable
fun KeyboardSurface(
    context: KeyboardContext,
    onKey: (KeyAction) -> Unit,
    onSettings: () -> Unit
) {
    var layer by remember { mutableStateOf(Layer.LETTERS) }
    var upper by remember { mutableStateOf(false) }
    val rows: List<List<KeySpec>> = when {
        context.kind == KeyboardKind.PHONE -> listOf(
            keys("123"), keys("456"), keys("789"),
            listOf(switcher(), KeySpec("*"), KeySpec("0"), KeySpec("#"), backspace(), enter())
        )
        context.kind == KeyboardKind.NUMBERS -> listOf(
            keys("123"), keys("456"), keys("789"),
            listOf(switcher(), KeySpec("-"), KeySpec("0"), KeySpec("."), backspace(), enter())
        )
        layer == Layer.EMOJI -> listOf(
            listOf("😀", "😂", "🥰", "😍", "👍", "❤️").map { KeySpec(it) },
            listOf("🎉", "🔥", "✨", "🙏", "💯", "😊").map { KeySpec(it) },
            listOf("😎", "🤔", "😢", "🙌", "👋", "💚").map { KeySpec(it) },
            listOf(switcher(), KeySpec("ABC", special = true),
                KeySpec("Space", weight = 3f, action = KeyAction.Space), backspace(), enter())
        )
        layer == Layer.SYMBOLS -> listOf(
            keys("1234567890"),
            keys("@#$%&*-+()"),
            keys("!?:;/'\"_="),
            listOf(switcher(), KeySpec("ABC", special = true), KeySpec(",", special = true),
                KeySpec("Space", weight = 4f, action = KeyAction.Space), KeySpec(".", special = true),
                backspace(), enter())
        )
        else -> listOf(
            keys("qwertyuiop", upper),
            keys("asdfghjkl", upper),
            listOf(KeySpec("⇧", description = "Shift", special = true)) +
                keys("zxcvbnm", upper) + backspace(),
            listOf(switcher(), KeySpec("?123", special = true),
                KeySpec(if (context.kind == KeyboardKind.EMAIL) "@" else if (context.kind == KeyboardKind.URL) "/" else ","),
                KeySpec("Space", weight = 4f, description = "Space", special = true, action = KeyAction.Space),
                KeySpec(if (context.kind == KeyboardKind.EMAIL) ".com" else ".", special = true),
                enter())
        )
    }

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(36.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "GoreeCloud", modifier = Modifier.weight(1f).padding(start = 10.dp),
                    style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text("Offline", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                TextButton(onClick = {
                    if (layer == Layer.EMOJI) layer = Layer.LETTERS
                    else if (context.kind != KeyboardKind.NUMBERS && context.kind != KeyboardKind.PHONE) layer = Layer.EMOJI
                }, enabled = context.kind != KeyboardKind.NUMBERS && context.kind != KeyboardKind.PHONE) {
                    Text(if (layer == Layer.EMOJI) "ABC" else "☻")
                }
                TextButton(onClick = onSettings) { Text("⚙", modifier = Modifier.semantics { contentDescription = "Keyboard settings" }) }
            }

            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    row.forEach { key ->
                        KeyboardKey(key = key, onClick = {
                            when (key.label) {
                                "?123" -> layer = Layer.SYMBOLS
                                "ABC" -> layer = Layer.LETTERS
                                "⇧" -> upper = !upper
                                else -> {
                                    val action = key.action ?: KeyAction.Text(key.value)
                                    if (action != KeyAction.Text("")) onKey(action)
                                    if (upper && action is KeyAction.Text && action.value.any { it.isLetter() }) upper = false
                                }
                            }
                        })
                    }
                }
            }
            // Numeric/phone layouts never auto-learn values. All modes offer editor actions.
        }
    }
}

private fun keys(chars: String, upper: Boolean = false): List<KeySpec> =
    chars.map { char ->
        val text = if (upper) char.uppercaseChar().toString() else char.toString()
        KeySpec(text)
    }

private fun backspace() = KeySpec("⌫", description = "Backspace", special = true, action = KeyAction.Backspace)
private fun enter() = KeySpec("↵", description = "Enter", special = true, action = KeyAction.Enter)
private fun switcher() = KeySpec("🌐", description = "Switch keyboard", special = true, action = KeyAction.SwitchKeyboard)

@Composable
private fun RowScope.KeyboardKey(key: KeySpec, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.weight(key.weight).height(49.dp).semantics { contentDescription = key.description },
        color = if (key.special) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 1.dp,
        shadowElevation = 1.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = key.label,
                fontSize = if (key.label.length > 3) 13.sp else 19.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

package com.easytv.launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.easytv.launcher.ui.theme.*

@Composable
fun PinLockDialog(
    correctPin: String,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = CardBackground,
            border = androidx.compose.foundation.BorderStroke(2.dp, CardBorder),
            modifier = Modifier.width(360.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Caregiver PIN",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isError) "Incorrect PIN, try again" else "Enter 4 digits to access settings",
                    color = if (isError) DangerRed else TextSecondary,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // PIN Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    repeat(4) { index ->
                        val filled = index < enteredPin.length
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(
                                    if (filled) HighVisibilityFocusGlow else Color.Transparent,
                                    RoundedCornerShape(10.dp)
                                )
                                .border(2.dp, HighVisibilityFocusGlow, RoundedCornerShape(10.dp))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Number Pad (1-9, Clear, 0)
                val keypad = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "OK")
                )

                for (row in keypad) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        for (digit in row) {
                            KeypadButton(
                                text = digit,
                                onClick = {
                                    when (digit) {
                                        "C" -> {
                                            enteredPin = ""
                                            isError = false
                                        }
                                        "OK" -> {
                                            if (enteredPin == correctPin) {
                                                onSuccess()
                                            } else {
                                                isError = true
                                                enteredPin = ""
                                            }
                                        }
                                        else -> {
                                            if (enteredPin.length < 4) {
                                                val next = enteredPin + digit
                                                enteredPin = next
                                                if (next.length == 4) {
                                                    if (next == correctPin) {
                                                        onSuccess()
                                                    } else {
                                                        isError = true
                                                        enteredPin = ""
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    text: String,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Button(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isFocused) HighVisibilityFocusGlow else DarkBackground,
            contentColor = if (isFocused) DarkBackground else TextPrimary
        ),
        modifier = Modifier
            .size(68.dp, 44.dp)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

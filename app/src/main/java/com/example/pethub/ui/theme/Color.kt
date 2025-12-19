package com.example.pethub.ui.theme

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val CreamBackground = Color(0xFFF8F7E9) // Background
val CreamFair = Color(0xFFEDE4D1) // Container
val CreamLight = Color(0xFFF7F1D4) // Card
val CreamDark = Color(0xFFF6E8B5) // Card
val LightBrown = Color(0xFFA28970) // Border
val Gray = Color(0xFF2C2C2C) // Text (Labels)
val MutedBrown = Color(0xFF998061) // Text (Dropdowns, Links)
val VibrantBrown = Color(0xFFAC7F5E) // Button
val DarkBrown = Color(0xFF322B1B) // Title, Header
val NeutralBrown = Color(0xFF5B3A29)

// Shop Screen
val CreamBg = Color(0xFFFBF9F1)
val SidebarTextNormal = Color(0xFF5D534A)
val SidebarTextSelected = Color(0xFF2C2622)
val DividerColor = Color(0xFF8D6E63)
val CartButtonColor = Color(0xFFFDF1C6)

@Composable
fun getTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = CreamFair,
    unfocusedContainerColor = CreamFair,

    focusedBorderColor = LightBrown,
    unfocusedBorderColor = LightBrown,

    focusedLabelColor = LightBrown,
    unfocusedLabelColor = LightBrown
)

@Composable
fun getStatusColor(status: String): Color {
    return when (status.lowercase()) {
        "confirmed", "completed" -> Color(0xFF4CAF50)
        "pending" -> Color(0xFFFF9800)
        "cancelled" -> Color(0xFFF44336)
        else -> Color.Gray
    }
}
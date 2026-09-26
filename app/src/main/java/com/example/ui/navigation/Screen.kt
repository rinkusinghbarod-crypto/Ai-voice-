package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    STUDIO("studio", "Studio", Icons.AutoMirrored.Filled.MenuBook),
    PLAYER("player", "Player", Icons.Default.PlayCircleFilled),
    HISTORY("history", "History", Icons.Default.History),
    SETTINGS("settings", "Settings", Icons.Default.Settings)
}

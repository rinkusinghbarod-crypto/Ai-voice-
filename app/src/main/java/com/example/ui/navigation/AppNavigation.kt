package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.OldStoryVoiceApp
import com.example.data.model.NarrationEntity
import com.example.ui.components.MiniPlayerBar
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberGoldBright
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCharcoalSurface
import com.example.ui.theme.DarkObsidian
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.HistoryViewModel
import com.example.ui.viewmodel.SettingsViewModel
import com.example.ui.viewmodel.StudioViewModel
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    studioViewModel: StudioViewModel = viewModel(),
    historyViewModel: HistoryViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val app = context.applicationContext as OldStoryVoiceApp
    val audioPlayer = app.audioPlayer
    val repository = app.repository
    val coroutineScope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf(Screen.STUDIO) }
    var activeNarration by remember { mutableStateOf<NarrationEntity?>(null) }

    val isPlaying by audioPlayer.isPlaying.collectAsState()
    val currentPositionMs by audioPlayer.currentPositionMs.collectAsState()
    val durationMs by audioPlayer.durationMs.collectAsState()
    val currentFilePath by audioPlayer.currentFilePath.collectAsState()

    val progress = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f

    // Keep active narration in sync with audioPlayer current file
    LaunchedEffect(currentFilePath) {
        val path = currentFilePath
        if (path != null) {
            coroutineScope.launch {
                val list = repository.getNarrationOnce(activeNarration?.id ?: 0L)
                if (list != null && list.audioFilePath == path) {
                    activeNarration = list
                }
            }
        }
    }

    // Handle back button on secondary screens
    BackHandler(enabled = currentScreen != Screen.STUDIO) {
        currentScreen = Screen.STUDIO
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.navigationBars,
        containerColor = DarkObsidian,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkObsidian)
            ) {
                // Mini Player Bar (shown when playing or audio is loaded and user is not on PLAYER screen)
                if (currentScreen != Screen.PLAYER && activeNarration != null && currentFilePath != null) {
                    MiniPlayerBar(
                        currentNarration = activeNarration,
                        isPlaying = isPlaying,
                        progress = progress,
                        onBarClick = { currentScreen = Screen.PLAYER },
                        onPlayPauseClick = { audioPlayer.togglePlayPause() }
                    )
                }

                // Bottom Navigation Bar
                NavigationBar(
                    containerColor = DarkCharcoalSurface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    Screen.values().forEach { screen ->
                        val isSelected = currentScreen == screen
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentScreen = screen },
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                                    )
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF191102),
                                selectedTextColor = AmberGoldBright,
                                indicatorColor = AmberGold,
                                unselectedIconColor = TextTertiary,
                                unselectedTextColor = TextTertiary
                            ),
                            modifier = Modifier.testTag("nav_item_${screen.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.STUDIO -> {
                    StudioScreen(
                        viewModel = studioViewModel,
                        onNavigateToPlayer = { narrationId ->
                            coroutineScope.launch {
                                activeNarration = repository.getNarrationOnce(narrationId)
                                currentScreen = Screen.PLAYER
                            }
                        },
                        onNavigateToSettings = { currentScreen = Screen.SETTINGS }
                    )
                }

                Screen.PLAYER -> {
                    PlayerScreen(
                        audioPlayer = audioPlayer,
                        currentNarration = activeNarration,
                        onRegenerateInStudio = { narration ->
                            studioViewModel.loadFromHistory(narration)
                            currentScreen = Screen.STUDIO
                        }
                    )
                }

                Screen.HISTORY -> {
                    HistoryScreen(
                        viewModel = historyViewModel,
                        onOpenInPlayer = { narration ->
                            activeNarration = narration
                            if (audioPlayer.currentFilePath.value != narration.audioFilePath) {
                                audioPlayer.loadAudio(narration.audioFilePath, autoPlay = true)
                            }
                            currentScreen = Screen.PLAYER
                        },
                        onRegenerateInStudio = { narration ->
                            studioViewModel.loadFromHistory(narration)
                            currentScreen = Screen.STUDIO
                        },
                        onNavigateToStudio = { currentScreen = Screen.STUDIO }
                    )
                }

                Screen.SETTINGS -> {
                    SettingsScreen(viewModel = settingsViewModel)
                }
            }
        }
    }
}

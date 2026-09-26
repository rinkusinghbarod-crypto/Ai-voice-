package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.settings.AppSettings
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberGoldBright
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCharcoalSurface
import com.example.ui.theme.DarkObsidian
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedProvider by viewModel.selectedProvider.collectAsState()
    val customGeminiKey by viewModel.customGeminiKey.collectAsState()
    val backendUrl by viewModel.backendUrl.collectAsState()
    val backendAuth by viewModel.backendAuth.collectAsState()
    val isKeyConfigured by viewModel.isKeyConfigured.collectAsState()
    val cacheSizeMb by viewModel.cacheSizeMb.collectAsState()

    var isKeyVisible by remember { mutableStateOf(false) }
    var isGuideExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkObsidian)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Settings & Voice Provider",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = "Configure AI voice engine, credentials, and cache",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }
        }

        // Provider Selector Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCharcoalSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Active TTS Engine Provider",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )

                    ProviderOptionItem(
                        id = AppSettings.PROVIDER_GEMINI,
                        title = "Gemini 2.5 Flash TTS (Recommended)",
                        description = "State-of-the-art cinematic Old American Man voice via direct audio modality",
                        icon = Icons.Default.Public,
                        isSelected = selectedProvider == AppSettings.PROVIDER_GEMINI,
                        onSelect = { viewModel.selectProvider(AppSettings.PROVIDER_GEMINI) }
                    )

                    ProviderOptionItem(
                        id = AppSettings.PROVIDER_BACKEND,
                        title = "Custom Secure Backend Proxy",
                        description = "Route requests to a private server to keep credentials secure and replace providers",
                        icon = Icons.Default.Public,
                        isSelected = selectedProvider == AppSettings.PROVIDER_BACKEND,
                        onSelect = { viewModel.selectProvider(AppSettings.PROVIDER_BACKEND) }
                    )

                    ProviderOptionItem(
                        id = AppSettings.PROVIDER_SYSTEM,
                        title = "Android Device Speech Engine (Offline)",
                        description = "Built-in device speech synthesizer. Works completely offline with zero API setup.",
                        icon = Icons.Default.PhoneAndroid,
                        isSelected = selectedProvider == AppSettings.PROVIDER_SYSTEM,
                        onSelect = { viewModel.selectProvider(AppSettings.PROVIDER_SYSTEM) }
                    )
                }
            }
        }

        // Credentials section based on selected provider
        if (selectedProvider == AppSettings.PROVIDER_GEMINI) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCharcoalSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = AmberGold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Gemini API Key",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                            }

                            // Status badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isKeyConfigured) AmberContainer else DarkSurfaceVariant)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isKeyConfigured) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (isKeyConfigured) SuccessGreen else AmberGold,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isKeyConfigured) "Configured" else "Key Required",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (isKeyConfigured) TextPrimary else AmberGoldBright,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Enter your API key or configure it in AI Studio Secrets. It will be stored securely on your device.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = customGeminiKey,
                            onValueChange = { viewModel.updateCustomGeminiKey(it) },
                            placeholder = { Text("AIzaSy...", color = TextTertiary) },
                            singleLine = true,
                            visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                    Icon(
                                        imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (isKeyVisible) "Hide" else "Show",
                                        tint = TextSecondary
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("gemini_key_input"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AmberGold,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = DarkSurfaceVariant,
                                unfocusedContainerColor = DarkSurfaceVariant
                            )
                        )
                    }
                }
            }
        } else if (selectedProvider == AppSettings.PROVIDER_BACKEND) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCharcoalSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Secure Backend Endpoint",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )

                        Text(
                            text = "Your private server proxy URL that generates voice audio without storing keys in the mobile app.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )

                        OutlinedTextField(
                            value = backendUrl,
                            onValueChange = { viewModel.updateBackendUrl(it) },
                            placeholder = { Text("https://my-backend.com/api/narrate", color = TextTertiary) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("backend_url_input"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AmberGold,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = DarkSurfaceVariant,
                                unfocusedContainerColor = DarkSurfaceVariant
                            )
                        )

                        Text(
                            text = "Authorization Header (Optional)",
                            style = MaterialTheme.typography.labelMedium.copy(color = TextTertiary)
                        )

                        OutlinedTextField(
                            value = backendAuth,
                            onValueChange = { viewModel.updateBackendAuth(it) },
                            placeholder = { Text("Bearer secret_token_xyz", color = TextTertiary) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AmberGold,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = DarkSurfaceVariant,
                                unfocusedContainerColor = DarkSurfaceVariant
                            )
                        )
                    }
                }
            }
        }

        // Setup Guide & APK Build Instructions
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { isGuideExpanded = !isGuideExpanded },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCharcoalSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null, tint = AmberGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Setup Guide & APK Build Info",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                        }
                        Icon(
                            imageVector = if (isGuideExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    }

                    AnimatedVisibility(visible = isGuideExpanded) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            GuideStep(
                                number = "1",
                                title = "Obtain Gemini API Key",
                                content = "Visit Google AI Studio (aistudio.google.com) and click 'Get API key'. Copy the key."
                            )
                            GuideStep(
                                number = "2",
                                title = "Configure in App or AI Studio",
                                content = "You can paste the key directly in the field above, or configure 'GEMINI_API_KEY' in the AI Studio Secrets panel."
                            )
                            GuideStep(
                                number = "3",
                                title = "Backend Architecture for Production",
                                content = "To keep credentials off mobile devices, point the app to your secure backend URL. The app sends the script and narration parameters via POST JSON and accepts WAV/MP3 responses."
                            )
                            GuideStep(
                                number = "4",
                                title = "Building the Android APK",
                                content = "In terminal or CI/CD run:\n  gradle assembleDebug\nor for signed release:\n  gradle assembleRelease\nThe output APK will be in app/build/outputs/apk/."
                            )
                        }
                    }
                }
            }
        }

        // Storage / Audio Cache Management
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCharcoalSurface),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Audio Cache",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = String.format("%.2f MB of cached narrations", cacheSizeMb),
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.clearAudioCache()
                            Toast.makeText(context, "Audio cache cleared", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = ButtonDefaults.outlinedButtonBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                        )
                    ) {
                        Icon(imageVector = Icons.Default.CleaningServices, contentDescription = null, tint = AmberGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clear", color = TextPrimary, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProviderOptionItem(
    id: String,
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) DarkSurfaceVariant else Color.Transparent)
            .border(
                1.dp,
                if (isSelected) AmberGold else DarkBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onSelect)
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    selectedColor = AmberGold,
                    unselectedColor = TextTertiary
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) TextPrimary else TextSecondary
                    )
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextTertiary,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun GuideStep(
    number: String,
    title: String,
    content: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(AmberContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = AmberGoldBright,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
            )
        }
    }
}

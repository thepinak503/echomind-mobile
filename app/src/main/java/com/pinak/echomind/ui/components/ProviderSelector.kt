package com.pinak.echomind.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pinak.echomind.network.ChatService
import com.pinak.echomind.network.Provider

@Composable
fun ProviderSelector(
    selectedProvider: Provider,
    onProviderSelected: (Provider) -> Unit,
    selectedModel: String,
    onModelSelected: (String) -> Unit,
    onRefreshModels: () -> Unit,
    modifier: Modifier = Modifier
) {
    var providerExpanded by remember { mutableStateOf(false) }
    var modelExpanded by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Provider
            Box(modifier = Modifier.weight(1f)) {
                Column {
                    Text(
                        "Provider",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { providerExpanded = true }
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            selectedProvider.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
                DropdownMenu(
                    expanded = providerExpanded,
                    onDismissRequest = { providerExpanded = false }
                ) {
                    ChatService.providers.forEach { provider ->
                        DropdownMenuItem(
                            text = { Text(provider.name) },
                            onClick = {
                                onProviderSelected(provider)
                                providerExpanded = false
                            }
                        )
                    }
                }
            }

            // Model
            if (selectedProvider.requiresModel) {
                Box(modifier = Modifier.weight(1.2f)) {
                    Column {
                        Text(
                            "Model",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { modelExpanded = true }
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                selectedModel,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                    DropdownMenu(
                        expanded = modelExpanded,
                        onDismissRequest = { modelExpanded = false }
                    ) {
                        selectedProvider.models.forEach { model ->
                            DropdownMenuItem(
                                text = { Text(model) },
                                onClick = {
                                    onModelSelected(model)
                                    modelExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            if (selectedProvider.isLocal) {
                IconButton(onClick = onRefreshModels) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh Models",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

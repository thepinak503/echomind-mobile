package com.pinak.echomind.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pinak.echomind.data.model.Conversation

@Composable
fun DrawerContent(
    conversations: List<Conversation>,
    currentConversation: Conversation?,
    incognitoMode: Boolean,
    onIncognitoChange: (Boolean) -> Unit,
    onConversationClick: (Conversation) -> Unit,
    onNewChatClick: () -> Unit,
    onClearHistoryClick: () -> Unit
) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            Text(
                "Echomind",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Button(
                onClick = onNewChatClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("New Chat")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Recent Conversations",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(conversations) { conversation ->
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.ChatBubbleOutline, contentDescription = null) },
                        label = {
                            Text(
                                conversation.title.ifBlank { "New Chat" },
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        },
                        selected = conversation.id == currentConversation?.id,
                        onClick = { onConversationClick(conversation) },
                        modifier = Modifier.padding(vertical = 2.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(12.dp))
                    Text("Incognito Mode")
                }
                Switch(checked = incognitoMode, onCheckedChange = onIncognitoChange)
            }

            TextButton(
                onClick = onClearHistoryClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Clear History")
            }
        }
    }
}

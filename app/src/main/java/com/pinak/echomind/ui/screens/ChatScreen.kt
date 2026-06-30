package com.pinak.echomind.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pinak.echomind.data.model.Conversation
import com.pinak.echomind.data.model.Message
import com.pinak.echomind.network.Provider
import com.pinak.echomind.ui.components.MessageBubble
import com.pinak.echomind.ui.components.MessageInput
import com.pinak.echomind.ui.components.ProviderSelector
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun ChatScreen(
    conversation: Conversation?,
    selectedProvider: Provider,
    selectedModel: String,
    isTyping: Boolean,
    onProviderSelected: (Provider) -> Unit,
    onModelSelected: (String) -> Unit,
    onRefreshModels: () -> Unit,
    onSendMessage: (String) -> Unit,
    onRetryMessage: (Message) -> Unit,
    modifier: Modifier = Modifier
) {
    var newMessage by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(conversation?.messages?.size) {
        if ((conversation?.messages?.size ?: 0) > 0) {
            listState.animateScrollToItem(0)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        ProviderSelector(
            selectedProvider = selectedProvider,
            onProviderSelected = onProviderSelected,
            selectedModel = selectedModel,
            onModelSelected = onModelSelected,
            onRefreshModels = onRefreshModels
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = listState,
            reverseLayout = true,
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(
                items = conversation?.messages?.asReversed() ?: emptyList(),
                key = { it.id }
            ) { message ->
                MessageBubble(
                    message = message,
                    onRetry = { onRetryMessage(message) }
                )
            }
        }

        MessageInput(
            value = newMessage,
            onValueChange = { newMessage = it },
            onSendMessage = {
                onSendMessage(newMessage)
                newMessage = ""
            },
            isTyping = isTyping
        )
    }
}

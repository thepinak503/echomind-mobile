package com.pinak.echomind

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.pinak.echomind.data.ConversationRepository
import com.pinak.echomind.network.ChatMessage
import com.pinak.echomind.network.ChatService
import com.pinak.echomind.network.Provider
import com.pinak.echomind.ui.theme.EchomindTheme
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.image.coil.CoilImagesPlugin
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import java.util.UUID

@OptIn(InternalSerializationApi::class)
@Serializable
data class Message(val text: String, val author: String)

@OptIn(InternalSerializationApi::class)
@Serializable
data class Conversation(val id: String = UUID.randomUUID().toString(), val title: String, val messages: List<Message>)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EchomindTheme {
                EchomindApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EchomindApp() {
    val context = LocalContext.current
    val conversationRepository = remember { ConversationRepository(context) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val conversations = remember { mutableStateListOf<Conversation>() }
    var currentConversation by remember { mutableStateOf<Conversation?>(null) }
    var incognitoMode by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val savedConversations = conversationRepository.loadConversations()
        if (savedConversations.isNotEmpty()) {
            conversations.addAll(savedConversations)
            currentConversation = conversations.first()
        } else {
            val initialConversation = Conversation(
                title = "Echomind",
                messages = listOf(Message("Hey, how can I help you?", "Bot"))
            )
            conversations.add(initialConversation)
            currentConversation = initialConversation
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                conversations = conversations,
                currentConversation = currentConversation,
                incognitoMode = incognitoMode,
                onIncognitoChange = { incognitoMode = it },
                onConversationClick = {
                    currentConversation = it
                    scope.launch { drawerState.close() }
                },
                onNewChatClick = {
                    val newConversation = Conversation(title = "New Chat", messages = emptyList())
                    conversations.add(0, newConversation)
                    currentConversation = newConversation
                    scope.launch { drawerState.close() }
                },
                onClearHistoryClick = {
                    conversationRepository.clearHistory()
                    conversations.clear()
                    val newConversation = Conversation(title = "New Chat", messages = emptyList())
                    conversations.add(newConversation)
                    currentConversation = newConversation
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentConversation?.title ?: "Echomind") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/thepinak503/echomind"))
                            context.startActivity(intent)
                        }) {
                            // Replace with your custom GitHub icon
                            Icon(Icons.Default.Info, contentDescription = "GitHub")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { paddingValues ->
            ChatScreen(
                modifier = Modifier.padding(paddingValues),
                conversation = currentConversation,
                onConversationUpdate = { updatedConversation ->
                    val index = conversations.indexOfFirst { it.id == updatedConversation.id }
                    if (index != -1) {
                        conversations[index] = updatedConversation
                    }
                    currentConversation = updatedConversation

                    if (!incognitoMode) {
                        conversationRepository.saveConversations(conversations)
                    }
                }
            )
        }
    }
}

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
        Column(modifier = Modifier.padding(16.dp)) {
            Button(onClick = onNewChatClick, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Add, contentDescription = "New Chat")
                Text("New Chat")
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(conversations) { conversation ->
                    NavigationDrawerItem(
                        label = { Text(conversation.title) },
                        selected = conversation.id == currentConversation?.id,
                        onClick = { onConversationClick(conversation) },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Incognito Mode")
                Switch(checked = incognitoMode, onCheckedChange = onIncognitoChange)
            }
            Button(onClick = onClearHistoryClick, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                Icon(Icons.Default.Delete, contentDescription = "Clear History")
                Text("Clear History")
            }
        }
    }
}

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    conversation: Conversation?,
    onConversationUpdate: (Conversation) -> Unit
) {
    var selectedProvider by remember { mutableStateOf(ChatService.providers.first()) }
    var selectedModel by remember { mutableStateOf(selectedProvider.models.first()) }
    var newMessage by remember { mutableStateOf("") }
    val chatService = remember { ChatService() }
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxSize()) {
        ProviderSelector(
            selectedProvider = selectedProvider,
            onProviderSelected = {
                selectedProvider = it
                selectedModel = it.models.first()
            },
            selectedModel = selectedModel,
            onModelSelected = { selectedModel = it },
            onRefreshModels = {
                scope.launch {
                    val models = chatService.getOllamaModels()
                    if (models.isNotEmpty()) {
                        val ollamaProvider = ChatService.providers.find { it.isLocal }
                        ollamaProvider?.models = models
                    }
                }
            }
        )
        MessageList(
            messages = conversation?.messages ?: emptyList(),
            modifier = Modifier.weight(1f),
            onRetry = { messageToRetry ->
                if (conversation == null) return@MessageList
                scope.launch {
                    val messageIndex = conversation.messages.indexOf(messageToRetry)
                    if (messageIndex == -1) return@launch

                    val history = conversation.messages.subList(0, messageIndex)
                    val updatedConv = conversation.copy(messages = history)
                    onConversationUpdate(updatedConv)

                    val historyForApi = history.map {
                        val role = if (it.author == "Me") "user" else "assistant"
                        ChatMessage(role, it.text)
                    }
                    val botResponse = chatService.sendMessage(selectedProvider, selectedModel, historyForApi)
                    val finalConv = updatedConv.copy(messages = history + Message(botResponse, "Bot"))
                    onConversationUpdate(finalConv)
                }
            }
        )
        MessageInput(
            value = newMessage,
            onValueChange = { newMessage = it },
            onSendMessage = {
                if (newMessage.isNotBlank() && conversation != null) {
                    val userMessage = Message(newMessage, "Me")
                    val newTitle = if (conversation.messages.isEmpty()) userMessage.text.take(25) else conversation.title
                    val updatedConv = conversation.copy(title = newTitle, messages = conversation.messages + userMessage)
                    onConversationUpdate(updatedConv)
                    newMessage = ""
                    scope.launch {
                        val history = updatedConv.messages.map {
                            val role = if (it.author == "Me") "user" else "assistant"
                            ChatMessage(role = role, content = it.text)
                        }
                        val botResponse = chatService.sendMessage(selectedProvider, selectedModel, history)
                        val finalConv = updatedConv.copy(messages = updatedConv.messages + Message(botResponse, "Bot"))
                        onConversationUpdate(finalConv)
                    }
                }
            }
        )
    }
}

@Composable
fun ProviderSelector(
    selectedProvider: Provider,
    onProviderSelected: (Provider) -> Unit,
    selectedModel: String,
    onModelSelected: (String) -> Unit,
    onRefreshModels: () -> Unit
) {
    var providerExpanded by remember { mutableStateOf(false) }
    var modelExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Box {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { providerExpanded = true }) {
                Text("Provider: ${selectedProvider.name}")
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Select provider")
            }
            DropdownMenu(expanded = providerExpanded, onDismissRequest = { providerExpanded = false }) {
                ChatService.providers.forEach { provider ->
                    DropdownMenuItem(text = { Text(provider.name) }, onClick = {
                        onProviderSelected(provider)
                        providerExpanded = false
                    })
                }
            }
        }

        if (selectedProvider.requiresModel) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { modelExpanded = true }) {
                        Text("Model: $selectedModel")
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select model")
                    }
                    DropdownMenu(expanded = modelExpanded, onDismissRequest = { modelExpanded = false }) {
                        selectedProvider.models.forEach { model ->
                            DropdownMenuItem(text = { Text(model) }, onClick = {
                                onModelSelected(model)
                                modelExpanded = false
                            })
                        }
                    }
                }
                if (selectedProvider.isLocal) {
                    IconButton(onClick = onRefreshModels, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Models")
                    }
                }
            }
        }
    }
}

@Composable
fun MessageList(messages: List<Message>, modifier: Modifier = Modifier, onRetry: (Message) -> Unit) {
    LazyColumn(modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp), reverseLayout = true) {
        items(messages.asReversed()) { message ->
            MessageBubble(message = message, onRetry = { onRetry(message) })
        }
    }
}

@Composable
fun MessageBubble(message: Message, onRetry: () -> Unit) {
    val isUserMessage = message.author == "Me"
    val bubbleColor = if (isUserMessage) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (isUserMessage) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val markwon = remember {
        Markwon.builder(context)
            .usePlugin(CoilImagesPlugin.create(context))
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TablePlugin.create(context))
            .usePlugin(TaskListPlugin.create(context))
            .build()
    }
    val codeRegex = """```([\s\S]*?)```""".toRegex()
    val codeBlocks = remember(message.text) {
        codeRegex.findAll(message.text).map { it.groupValues[1].trim() }.toList()
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalAlignment = if (isUserMessage) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .clip(RoundedCornerShape(16.dp))
                .background(bubbleColor)
        ) {
            AndroidView(
                factory = {
                    TextView(it).apply {
                        setTextIsSelectable(true)
                        movementMethod = LinkMovementMethod.getInstance()
                    }
                },
                modifier = Modifier.padding(12.dp),
                update = {
                    it.setTextColor(textColor.toArgb())
                    it.setLinkTextColor(textColor.toArgb())
                    markwon.setMarkdown(it, message.text)
                }
            )
        }
        Column(modifier = Modifier.padding(top = 4.dp)) {
            codeBlocks.forEachIndexed { index, code ->
                TextButton(onClick = { clipboardManager.setText(AnnotatedString(code)) }) {
                    Text("Copy Code #${index + 1}")
                }
            }
        }

        Row(
            modifier = Modifier.padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Copy Button
            IconButton(onClick = { clipboardManager.setText(AnnotatedString(message.text)) }, modifier = Modifier.size(20.dp)) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy Message", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            // Share Button
            IconButton(
                onClick = {
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, message.text)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, null)
                    context.startActivity(shareIntent)
                },
                modifier = Modifier.size(20.dp)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = "Share Message", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // Retry button (only for bot messages)
            if (!isUserMessage) {
                IconButton(onClick = onRetry, modifier = Modifier.size(20.dp)) {
                    Icon(imageVector = Icons.Default.Replay, contentDescription = "Retry", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun MessageInput(value: String, onValueChange: (String) -> Unit, onSendMessage: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Type a message...") }
        )
        IconButton(onClick = onSendMessage, modifier = Modifier.padding(start = 8.dp)) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send message")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EchomindAppPreview() {
    EchomindTheme {
        EchomindApp()
    }
}

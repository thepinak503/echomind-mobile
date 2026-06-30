package com.pinak.echomind

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pinak.echomind.data.ConversationRepository
import com.pinak.echomind.ui.MainViewModel
import com.pinak.echomind.ui.MainViewModelFactory
import com.pinak.echomind.ui.components.DrawerContent
import com.pinak.echomind.ui.screens.ChatScreen
import com.pinak.echomind.ui.theme.EchomindTheme
import kotlinx.coroutines.launch

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
    val repository = remember { ConversationRepository(context) }
    val viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(repository)
    )

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    val currentConversation by viewModel.currentConversation.collectAsState()
    val incognitoMode by viewModel.incognitoMode.collectAsState()
    val selectedProvider by viewModel.selectedProvider.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val isTyping by viewModel.isTyping.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                conversations = viewModel.conversations,
                currentConversation = currentConversation,
                incognitoMode = incognitoMode,
                onIncognitoChange = { viewModel.toggleIncognito(it) },
                onConversationClick = {
                    viewModel.selectConversation(it)
                    scope.launch { drawerState.close() }
                },
                onNewChatClick = {
                    viewModel.createNewChat()
                    scope.launch { drawerState.close() }
                },
                onClearHistoryClick = {
                    viewModel.clearHistory()
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
                selectedProvider = selectedProvider,
                selectedModel = selectedModel,
                isTyping = isTyping,
                onProviderSelected = { viewModel.setProvider(it) },
                onModelSelected = { viewModel.setModel(it) },
                onRefreshModels = { viewModel.refreshModels() },
                onSendMessage = { viewModel.sendMessage(it) },
                onRetryMessage = { viewModel.retryMessage(it) }
            )
        }
    }
}

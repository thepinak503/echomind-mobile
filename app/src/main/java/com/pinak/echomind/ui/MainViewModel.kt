package com.pinak.echomind.ui

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pinak.echomind.data.ConversationRepository
import com.pinak.echomind.data.model.Conversation
import com.pinak.echomind.data.model.Message
import com.pinak.echomind.network.ChatMessage
import com.pinak.echomind.network.ChatService
import com.pinak.echomind.network.Provider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val repository: ConversationRepository) : ViewModel() {

    private val chatService = ChatService()

    private val _conversations = mutableStateListOf<Conversation>()
    val conversations: List<Conversation> get() = _conversations

    private val _currentConversation = MutableStateFlow<Conversation?>(null)
    val currentConversation: StateFlow<Conversation?> = _currentConversation.asStateFlow()

    private val _incognitoMode = MutableStateFlow(false)
    val incognitoMode: StateFlow<Boolean> = _incognitoMode.asStateFlow()

    private val _selectedProvider = MutableStateFlow(ChatService.providers.first())
    val selectedProvider: StateFlow<Provider> = _selectedProvider.asStateFlow()

    private val _selectedModel = MutableStateFlow(_selectedProvider.value.models.first())
    val selectedModel: StateFlow<String> = _selectedModel.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    init {
        loadConversations()
    }

    private fun loadConversations() {
        viewModelScope.launch {
            val savedConversations = repository.loadConversations()
            if (savedConversations.isNotEmpty()) {
                _conversations.addAll(savedConversations)
                _currentConversation.value = _conversations.first()
            } else {
                createNewChat()
            }
        }
    }

    fun createNewChat() {
        val initialConversation = Conversation(
            title = "New Chat",
            messages = emptyList()
        )
        _conversations.add(0, initialConversation)
        _currentConversation.value = initialConversation
    }

    fun selectConversation(conversation: Conversation) {
        _currentConversation.value = conversation
    }

    fun toggleIncognito(enabled: Boolean) {
        _incognitoMode.value = enabled
    }

    fun setProvider(provider: Provider) {
        _selectedProvider.value = provider
        _selectedModel.value = provider.models.first()
    }

    fun setModel(model: String) {
        _selectedModel.value = model
    }

    fun refreshModels() {
        viewModelScope.launch {
            val models = chatService.getOllamaModels()
            if (models.isNotEmpty()) {
                val ollamaProvider = ChatService.providers.find { it.isLocal }
                ollamaProvider?.models = models
                if (_selectedProvider.value.isLocal) {
                    _selectedProvider.value = ollamaProvider!!
                }
            }
        }
    }

    fun sendMessage(text: String) {
        val conversation = _currentConversation.value ?: return
        if (text.isBlank()) return

        viewModelScope.launch {
            val userMessage = Message(text = text, author = "Me")
            val newTitle = if (conversation.messages.isEmpty()) text.take(25) else conversation.title
            
            val updatedConv = conversation.copy(
                title = newTitle,
                messages = conversation.messages + userMessage
            )
            updateConversation(updatedConv)

            _isTyping.value = true
            val history = updatedConv.messages.map {
                val role = if (it.author == "Me") "user" else "assistant"
                ChatMessage(role = role, content = it.text)
            }
            
            val botResponse = chatService.sendMessage(_selectedProvider.value, _selectedModel.value, history)
            _isTyping.value = false
            
            val finalConv = updatedConv.copy(
                messages = updatedConv.messages + Message(text = botResponse, author = "Bot")
            )
            updateConversation(finalConv)
        }
    }

    fun retryMessage(messageToRetry: Message) {
        val conversation = _currentConversation.value ?: return
        viewModelScope.launch {
            val messageIndex = conversation.messages.indexOf(messageToRetry)
            if (messageIndex == -1) return@launch

            val history = conversation.messages.subList(0, messageIndex)
            val updatedConv = conversation.copy(messages = history)
            updateConversation(updatedConv)

            _isTyping.value = true
            val historyForApi = history.map {
                val role = if (it.author == "Me") "user" else "assistant"
                ChatMessage(role, it.text)
            }
            val botResponse = chatService.sendMessage(_selectedProvider.value, _selectedModel.value, historyForApi)
            _isTyping.value = false
            
            val finalConv = updatedConv.copy(
                messages = history + Message(text = botResponse, author = "Bot")
            )
            updateConversation(finalConv)
        }
    }

    fun clearHistory() {
        repository.clearHistory()
        _conversations.clear()
        createNewChat()
    }

    private fun updateConversation(updatedConversation: Conversation) {
        val index = _conversations.indexOfFirst { it.id == updatedConversation.id }
        if (index != -1) {
            _conversations[index] = updatedConversation
        }
        _currentConversation.value = updatedConversation

        if (!_incognitoMode.value) {
            repository.saveConversations(_conversations.toList())
        }
    }
}

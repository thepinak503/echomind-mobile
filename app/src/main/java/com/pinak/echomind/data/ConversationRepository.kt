package com.pinak.echomind.data

import android.content.Context
import com.pinak.echomind.Conversation
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

class ConversationRepository(context: Context) {

    private val sharedPreferences = context.getSharedPreferences("chat_history", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true; prettyPrint = true }

    fun saveConversations(conversations: List<Conversation>) {
        val data = json.encodeToString(conversations)
        sharedPreferences.edit().putString("conversations_json", data).apply()
    }

    fun loadConversations(): List<Conversation> {
        val data = sharedPreferences.getString("conversations_json", null)
        return if (data != null) {
            try {
                json.decodeFromString<List<Conversation>>(data)
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    fun clearHistory() {
        sharedPreferences.edit().clear().apply()
    }
}
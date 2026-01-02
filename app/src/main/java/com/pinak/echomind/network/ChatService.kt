package com.pinak.echomind.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ChatRequest(val messages: List<ChatMessage>, val model: String? = null)

@Serializable
data class ChatMessage(val role: String, val content: String)

@Serializable
data class ChatResponse(val choices: List<Choice>? = null, val error: ApiError? = null)

@Serializable
data class Choice(val message: ChatMessage)

@Serializable
data class ApiError(val message: String)

@Serializable
data class OllamaModelsResponse(val models: List<OllamaModel>)

@Serializable
data class OllamaModel(val name: String)

@Serializable
data class OllamaChatRequest(val model: String, val messages: List<ChatMessage>, val stream: Boolean)

@Serializable
data class OllamaChatResponse(val message: ChatMessage? = null, val error: String? = null)

data class Provider(
    val name: String,
    val url: String,
    val apiKey: String? = null,
    var models: List<String>,
    val requiresModel: Boolean = false,
    val isLocal: Boolean = false
)

class ChatService {

    companion object {
        val providers = mutableListOf(
            Provider(
                name = "ChatAnywhere",
                url = "https://api.chatanywhere.tech/v1/chat/completions",
                apiKey = "sk-mMpWQdfonkzfFw9mOWGU3I2e4y24j2GbmnwXmQOVKsF66c0I",
                models = listOf(
                    "gpt-5.1", "gpt-5-chat-latest", "gpt-5", "gpt-5-mini", "gpt-5-nano",
                    "gpt-40", "gpt-40-mini", "04-mini", "03-mini", "03"
                ),
                requiresModel = true
            ),
            Provider(
                name = "ch.at",
                url = "https://ch.at/v1/chat/completions",
                models = listOf("default")
            ),
            Provider(
                name = "Ollama (Local)",
                url = "http://127.0.0.1:11434/chat",
                models = listOf("llama3", "codellama", "mistral", "gemma"),
                requiresModel = true,
                isLocal = true
            )
        )
    }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                encodeDefaults = false // This will omit null values from the JSON
            })
        }
    }

    suspend fun getOllamaModels(): List<String> {
        return try {
            val response: OllamaModelsResponse = client.get("http://127.0.0.1:11434/api/tags").body()
            response.models.map { it.name }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun sendMessage(provider: Provider, model: String, history: List<ChatMessage>): String {
        return try {
            if (provider.isLocal) { // Ollama
                val ollamaRequest = OllamaChatRequest(model = model, messages = history, stream = false)
                val response: OllamaChatResponse = client.post(provider.url + "/chat") {
                    contentType(ContentType.Application.Json)
                    setBody(ollamaRequest)
                }.body()
                response.message?.content ?: response.error ?: "Error: Received an empty or invalid response from Ollama."
            } else { // OpenAI-compatible APIs
                val response: ChatResponse = client.post(provider.url) {
                    provider.apiKey?.let {
                        header(HttpHeaders.Authorization, "Bearer $it")
                    }
                    contentType(ContentType.Application.Json)
                    val requestBody = ChatRequest(
                        messages = history,
                        model = if (provider.requiresModel) model else null
                    )
                    setBody(requestBody)
                }.body()

                response.choices?.firstOrNull()?.message?.content ?: response.error?.message ?: "Error: Received an empty or invalid response."
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Error: ${e.message}"
        }
    }
}

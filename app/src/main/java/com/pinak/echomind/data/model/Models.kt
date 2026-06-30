package com.pinak.echomind.data.model

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import java.util.UUID

@OptIn(InternalSerializationApi::class)
@Serializable
data class Message(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val author: String
)

@OptIn(InternalSerializationApi::class)
@Serializable
data class Conversation(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val messages: List<Message>
)

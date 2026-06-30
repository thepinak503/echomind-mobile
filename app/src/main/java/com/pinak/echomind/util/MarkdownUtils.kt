package com.pinak.echomind.util

private val mermaidRegex = """```mermaid\n([\s\S]*?)\n```""".toRegex()

sealed class MessageSegment {
    data class Text(val content: String) : MessageSegment()
    data class Mermaid(val code: String) : MessageSegment()
}

fun segmentMessage(text: String): List<MessageSegment> {
    val segments = mutableListOf<MessageSegment>()
    var lastIndex = 0
    
    mermaidRegex.findAll(text).forEach { match ->
        if (match.range.first > lastIndex) {
            segments.add(MessageSegment.Text(text.substring(lastIndex, match.range.first)))
        }
        segments.add(MessageSegment.Mermaid(match.groupValues[1].trim()))
        lastIndex = match.range.last + 1
    }
    
    if (lastIndex < text.length) {
        segments.add(MessageSegment.Text(text.substring(lastIndex)))
    }
    
    return if (segments.isEmpty() && text.isNotEmpty()) listOf(MessageSegment.Text(text)) else segments
}

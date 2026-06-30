package com.pinak.echomind.ui.components

import android.content.Intent
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.pinak.echomind.data.model.Message
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.image.coil.CoilImagesPlugin

private val codeRegex = """```([\s\S]*?)```""".toRegex()

@Composable
fun MessageBubble(
    message: Message,
    onRetry: () -> Unit
) {
    val isUserMessage = message.author == "Me"
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
    
    val codeBlocks = remember(message.text) {
        codeRegex.findAll(message.text).map { it.groupValues[1].trim() }.toList()
    }

    val bubbleShape = if (isUserMessage) {
        RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp)
    }

    val bubbleBackground = if (isUserMessage) {
        Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.secondaryContainer,
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.9f)
            )
        )
    }

    val textColor = if (isUserMessage) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        horizontalAlignment = if (isUserMessage) Alignment.End else Alignment.Start
    ) {
        Card(
            shape = bubbleShape,
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Box(
                modifier = Modifier
                    .background(bubbleBackground)
                    .padding(12.dp)
            ) {
                AndroidView(
                    factory = {
                        TextView(it).apply {
                            setTextIsSelectable(true)
                            movementMethod = LinkMovementMethod.getInstance()
                            setLinkTextColor(textColor.toArgb())
                        }
                    },
                    update = {
                        it.setTextColor(textColor.toArgb())
                        markwon.setMarkdown(it, message.text)
                    }
                )
            }
        }

        if (codeBlocks.isNotEmpty()) {
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                codeBlocks.forEachIndexed { index, code ->
                    AssistChip(
                        onClick = { clipboardManager.setText(AnnotatedString(code)) },
                        label = { Text("Code #${index + 1}") },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.padding(top = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = { clipboardManager.setText(AnnotatedString(message.text)) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy Message",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
            IconButton(
                onClick = {
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, message.text)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, null))
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share Message",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
            if (!isUserMessage) {
                IconButton(
                    onClick = onRetry,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = "Retry",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

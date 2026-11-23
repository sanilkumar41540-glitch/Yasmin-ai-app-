package com.yasmin.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class Msg(val fromUser: Boolean, val text: String)

class MainActivity : ComponentActivity() {
    private val engine = YasminEngine(GeminiClient(""))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { YasminScreen(engine) } }
    }
}

@Composable
fun YasminScreen(engine: YasminEngine) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val messages = remember { mutableStateListOf(Msg(false, "Hi, main Yasmin hoon. Kya karna hai?")) }
    var input by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var pending by remember { mutableStateOf<ToolRequest?>(null) }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) { listState.animateScrollToItem(messages.lastIndex) }

    pending?.let { req ->
        AlertDialog(
            onDismissRequest = { pending = null },
            title = { Text("Yasmin wants to:") },
            text = { Text(req.description) },
            confirmButton = {
                TextButton(onClick = {
                    messages += Msg(false, ToolRouter.execute(ctx, req)); pending = null
                }) { Text("Allow") }
            },
            dismissButton = {
                TextButton(onClick = { messages += Msg(false, "Theek hai, cancel kar diya."); pending = null }) { Text("Deny") }
            }
        )
    }

    fun send() {
        val text = input.trim()
        if (text.isEmpty() || busy) return
        input = ""
        messages += Msg(true, text)
        val tool = ToolRouter.parse(text)
        if (tool != null) { pending = tool; return }
        busy = true
        scope.launch {
            val reply = withContext(Dispatchers.IO) { engine.chat(text) }
            messages += Msg(false, reply)
            busy = false
        }
    }

    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding().padding(12.dp)) {
        Text("Yasmin AI", style = MaterialTheme.typography.titleLarge)
        LazyColumn(Modifier.weight(1f).fillMaxWidth(), state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(messages) { m ->
                Row(Modifier.fillMaxWidth(),
                    horizontalArrangement = if (m.fromUser) Arrangement.End else Arrangement.Start) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = if (m.fromUser) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.widthIn(max = 300.dp)
                    ) { Text(m.text, Modifier.padding(10.dp)) }
                }
            }
        }
        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(input, { input = it }, Modifier.weight(1f),
                placeholder = { Text("Yasmin se baat karo...") })
            Spacer(Modifier.width(8.dp))
            Button(onClick = { send() }) { Text("Send") }
        }
    }
}

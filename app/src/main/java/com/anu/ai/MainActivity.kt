package com.anu.ai

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

private val Night = Color(0xFF100817)
private val Panel = Color(0xFF21142B)
private val Pink = Color(0xFFFF78C8)
private val Violet = Color(0xFFAD78FF)
private val SoftText = Color(0xFFEADDEC)

data class ChatMessage(val text: String, val fromUser: Boolean)

class MainActivity : ComponentActivity() {
    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("en", "IN")
                tts?.setPitch(1.08f)
                tts?.setSpeechRate(0.96f)
            }
        }

        setContent {
            AnuApp(
                speak = { text ->
                    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "anu_reply")
                }
            )
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

@Composable
fun AnuApp(speak: (String) -> Unit) {
    var currentTab by remember { mutableStateOf("Home") }
    var input by remember { mutableStateOf("") }
    var listening by remember { mutableStateOf(false) }
    val messages = remember {
        mutableStateListOf(
            ChatMessage("Hello! I'm Anu, your AI companion. What would you like to do today? ✨", false)
        )
    }
    val context = LocalContext.current

    val speechLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val spoken = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
        if (!spoken.isNullOrBlank()) {
            input = spoken
            currentTab = "Chat"
        }
        listening = false
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            listening = true
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Talk to Anu")
            }
            try {
                speechLauncher.launch(intent)
            } catch (_: Exception) {
                listening = false
            }
        }
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Night,
            surface = Panel,
            primary = Pink,
            secondary = Violet,
            onBackground = SoftText,
            onSurface = SoftText
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF29132F), Night, Color(0xFF100D20)))
                )
                .padding(horizontal = 18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("ANU AI", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Black)
                    Text("YOUR PERSONAL AI", color = Pink, fontSize = 10.sp, letterSpacing = 2.sp)
                }
                Surface(color = Color(0x3324D18A), shape = CircleShape) {
                    Text(" ● ONLINE ", color = Color(0xFF72E6B0), fontSize = 11.sp, modifier = Modifier.padding(8.dp))
                }
                IconButton(onClick = { currentTab = "Settings" }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = SoftText)
                }
            }

            when (currentTab) {
                "Home" -> HomeScreen(
                    onChat = { currentTab = "Chat" },
                    onVoice = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                    onCreate = { currentTab = "Create" },
                    onTools = { currentTab = "Tools" }
                )
                "Chat" -> ChatScreen(
                    messages = messages,
                    input = input,
                    onInput = { input = it },
                    onSend = {
                        val question = input.trim()
                        if (question.isNotEmpty()) {
                            messages.add(ChatMessage(question, true))
                            val answer = localReply(question)
                            messages.add(ChatMessage(answer, false))
                            speak(answer)
                            input = ""
                        }
                    },
                    onVoice = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }
                )
                "Create" -> InfoScreen(
                    title = "Creation Studio",
                    subtitle = "Your creative workspace",
                    items = listOf("AI image generation — connect an image API", "Image editing — planned integration", "Document and content creation")
                )
                "Tools" -> InfoScreen(
                    title = "ANU Tools",
                    subtitle = "Useful phone and assistant actions",
                    items = listOf("Voice input and spoken replies", "Open Android apps — next development stage", "Reminders and alarms — next development stage", "Web search — connect a search provider")
                )
                else -> InfoScreen(
                    title = "Settings",
                    subtitle = "Make Anu feel right for you",
                    items = listOf("Personality: friendly, intelligent and playful", "Voice: Android female voice if installed", "Privacy: microphone is used only when you tap the voice button", "AI connection: not configured yet")
                )
            }

            Spacer(Modifier.weight(1f))
            NavigationBar(
                containerColor = Color(0xFF1B1024),
                contentColor = SoftText,
                modifier = Modifier.padding(bottom = 8.dp).clip(RoundedCornerShape(22.dp))
            ) {
                listOf("Home", "Chat", "Voice", "Create", "Tools").forEach { item ->
                    NavigationBarItem(
                        selected = currentTab == item || (item == "Voice" && listening),
                        onClick = {
                            if (item == "Voice") permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            else currentTab = item
                        },
                        icon = {
                            Icon(
                                when (item) {
                                    "Home" -> Icons.Default.Home
                                    "Chat" -> Icons.Default.Chat
                                    "Voice" -> Icons.Default.Mic
                                    "Create" -> Icons.Default.AutoAwesome
                                    else -> Icons.Default.GridView
                                },
                                contentDescription = item
                            )
                        },
                        label = { Text(item, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Pink,
                            selectedTextColor = Pink,
                            indicatorColor = Color(0xFF3B2147),
                            unselectedIconColor = Color.LightGray,
                            unselectedTextColor = Color.LightGray
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    onChat: () -> Unit,
    onVoice: () -> Unit,
    onCreate: () -> Unit,
    onTools: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("HELLO, I'M", color = SoftText, fontSize = 13.sp, letterSpacing = 3.sp)
        Text("Anu ✨", color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Bold)
        Text("Your AI companion", color = Pink, fontSize = 15.sp)
        Spacer(Modifier.height(24.dp))

        Image(
            painter = painterResource(id = R.drawable.anu_character),
            contentDescription = "Anu anime character",
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(220.dp).clip(CircleShape)
        )
        Text("ANU", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
        Text("READY TO HELP", color = SoftText, fontSize = 10.sp, letterSpacing = 2.sp)
        Spacer(Modifier.height(25.dp))
        Text("What can I help you with?", color = SoftText, fontSize = 17.sp)
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onVoice,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = Pink),
            modifier = Modifier.size(82.dp)
        ) {
            Icon(Icons.Default.Mic, contentDescription = "Talk to Anu", tint = Color.White, modifier = Modifier.size(34.dp))
        }
        Text("TAP TO TALK", color = Pink, fontSize = 10.sp, letterSpacing = 2.sp, modifier = Modifier.padding(top = 8.dp))
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickTile("Chat", Icons.Default.Chat, onChat, Modifier.weight(1f))
            QuickTile("Create", Icons.Default.AutoAwesome, onCreate, Modifier.weight(1f))
            QuickTile("Tools", Icons.Default.GridView, onTools, Modifier.weight(1f))
        }
    }
}

@Composable
fun QuickTile(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(82.dp).clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = Panel,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF54365F))
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, contentDescription = label, tint = Pink)
            Spacer(Modifier.height(5.dp))
            Text(label, color = SoftText, fontSize = 12.sp)
        }
    }
}

@Composable
fun ChatScreen(
    messages: List<ChatMessage>,
    input: String,
    onInput: (String) -> Unit,
    onSend: () -> Unit,
    onVoice: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Text("Talk with Anu", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Local demo mode • AI cloud connection not added yet", color = Pink, fontSize = 11.sp)
        Spacer(Modifier.height(12.dp))
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { message ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (message.fromUser) Arrangement.End else Arrangement.Start
                ) {
                    Surface(
                        color = if (message.fromUser) Color(0xFF633A78) else Panel,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.widthIn(max = 300.dp)
                    ) {
                        Text(message.text, color = Color.White, modifier = Modifier.padding(13.dp), fontSize = 14.sp)
                    }
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onVoice) {
                Icon(Icons.Default.Mic, contentDescription = "Voice input", tint = Pink)
            }
            OutlinedTextField(
                value = input,
                onValueChange = onInput,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Message Anu…", color = Color.LightGray) },
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Pink,
                    unfocusedBorderColor = Color(0xFF60436A),
                    cursorColor = Pink
                )
            )
            IconButton(onClick = onSend) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Pink)
            }
        }
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
fun InfoScreen(title: String, subtitle: String, items: List<String>) {
    Column(Modifier.fillMaxWidth().padding(top = 35.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(title, color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = Pink, fontSize = 14.sp)
        items.forEach { item ->
            Surface(color = Panel, shape = RoundedCornerShape(15.dp), modifier = Modifier.fillMaxWidth()) {
                Text(item, color = SoftText, modifier = Modifier.padding(17.dp), fontSize = 14.sp)
            }
        }
    }
}

fun localReply(question: String): String {
    val q = question.lowercase()
    return when {
        q.contains("hello") || q.contains("hi") -> "Hey! It's lovely to hear from you. What shall we do today? 😊"
        q.contains("your name") -> "I'm Anu, your personal AI companion."
        q.contains("who are you") -> "I'm Anu: friendly, curious, a little playful, and ready to help."
        q.contains("embedded system") -> "An embedded system is a computer designed to perform a specific task inside a larger device. Examples include washing machines, cars and microwave ovens."
        q.contains("time") -> "I can't read the live clock in this demo yet. We can add a proper device-time tool next."
        q.contains("help") -> "Of course! I can chat, answer a few built-in demo questions and speak my replies. Internet AI, memory and phone actions are the next integrations."
        else -> "I understood your message: \"$question\". I'm currently running in offline demo mode. Connect a secure AI backend to give me full JARVIS-style intelligence."
    }
}

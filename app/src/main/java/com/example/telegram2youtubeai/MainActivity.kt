
package com.example.telegram2youtubeai

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TelegramYouTubeApp() }
    }
}

private enum class Screen(val label: String) {
    Home("Home"), Channels("Channels"), Approvals("Approvals"),
    Activity("Activity"), Settings("Settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TelegramYouTubeApp() {
    val context = LocalContext.current
    var screen by remember { mutableStateOf(Screen.Home) }
    var monitoring by remember { mutableStateOf(false) }
    var channelInput by remember { mutableStateOf("") }
    val channels = remember { mutableStateListOf<String>() }
    val activity = remember { mutableStateListOf("Starter app opened") }
    var pending by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Telegram2YouTube AI") })
        },
        bottomBar = {
            NavigationBar {
                Screen.values().forEach { item ->
                    val icon = when (item) {
                        Screen.Home -> Icons.Default.Home
                        Screen.Channels -> Icons.Default.List
                        Screen.Approvals -> Icons.Default.CheckCircle
                        Screen.Activity -> Icons.Default.History
                        Screen.Settings -> Icons.Default.Settings
                    }
                    NavigationBarItem(
                        selected = screen == item,
                        onClick = { screen = item },
                        icon = { Icon(icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (screen) {
                Screen.Home -> {
                    Text("Dashboard",
                        style = MaterialTheme.typography.headlineMedium)
                    Card {
                        Column(Modifier.padding(16.dp)) {
                            Text("Monitoring")
                            Text(if (monitoring) "Demo monitoring is ON"
                                else "Demo monitoring is OFF")
                            Switch(
                                checked = monitoring,
                                onCheckedChange = {
                                    monitoring = it
                                    activity.add(
                                        if (it) "Demo monitoring enabled"
                                        else "Demo monitoring disabled"
                                    )
                                }
                            )
                        }
                    }
                    SummaryCard("Channels added", channels.size.toString())
                    SummaryCard("Videos awaiting approval",
                        if (pending) "1 demo item" else "0")
                    Text(
                        "Demo only: Telegram monitoring and YouTube uploads are not connected.",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Screen.Channels -> {
                    Text("Authorized channels",
                        style = MaterialTheme.typography.headlineMedium)

                    OutlinedTextField(
                        value = channelInput,
                        onValueChange = { channelInput = it },
                        label = { Text("Public channel username or link") },
                        placeholder = { Text("@channelname or https://t.me/channelname") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            val input = channelInput.trim()
                            val username = input
                                .removePrefix("https://t.me/")
                                .removePrefix("http://t.me/")
                                .removePrefix("t.me/")
                                .removePrefix("@")
                                .substringBefore("/")
                                .substringBefore("?")
                                .trim()

                            when {
                                username.isEmpty() -> {
                                    Toast.makeText(context,
                                        "Please enter a channel username or link",
                                        Toast.LENGTH_SHORT).show()
                                }

                                channels.any {
                                    it.equals(username, ignoreCase = true)
                                } -> {
                                    Toast.makeText(context,
                                        "Channel already added",
                                        Toast.LENGTH_SHORT).show()
                                }

                                else -> {
                                    channels.add(username)
                                    activity.add("Channel added: @$username")
                                    channelInput = ""
                                    Toast.makeText(context,
                                        "Channel added successfully",
                                        Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add channel")
                    }

                    if (channels.isEmpty()) {
                        Text("No channels added yet.")
                    }

                    LazyColumn {
                        items(channels) { channel ->
                            ListItem(
                                headlineContent = { Text("@$channel") },
                                trailingContent = {
                                    IconButton(onClick = {
                                        channels.remove(channel)
                                        activity.add("Channel removed: @$channel")
                                    }) {
                                        Icon(Icons.Default.Delete,
                                            contentDescription = "Remove")
                                    }
                                }
                            )
                        }
                    }
                }

                Screen.Approvals -> {
                    Text("Video approvals",
                        style = MaterialTheme.typography.headlineMedium)
                    if (pending) {
                        Card {
                            Column(
                                Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Sample detected video",
                                    style = MaterialTheme.typography.titleMedium)
                                Text("Source: Demo channel")
                                Text("Placeholder only. No real video detected.")
                                Row(horizontalArrangement =
                                    Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = {
                                        pending = false
                                        activity.add("Demo item approved — no upload performed")
                                    }) {
                                        Text("Approve (demo)")
                                    }
                                    OutlinedButton(onClick = {
                                        pending = false
                                        activity.add("Demo item rejected")
                                    }) {
                                        Text("Reject")
                                    }
                                }
                            }
                        }
                    } else {
                        Text("No pending demo items.")
                    }
                }

                Screen.Activity -> {
                    Text("Activity history",
                        style = MaterialTheme.typography.headlineMedium)
                    LazyColumn {
                        items(activity.reversed()) { event ->
                            ListItem(headlineContent = { Text(event) })
                        }
                    }
                }

                Screen.Settings -> {
                    Text("Settings",
                        style = MaterialTheme.typography.headlineMedium)
                    ListItem(
                        headlineContent = { Text("YouTube account") },
                        supportingContent = {
                            Text("Not connected — integration not implemented")
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Notifications") },
                        supportingContent = {
                            Text("Not configured in this demo")
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(title: String, value: String) {
    Card {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title)
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}

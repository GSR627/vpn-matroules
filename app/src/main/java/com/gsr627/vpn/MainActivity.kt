package com.gsr627.vpn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

class MainActivity : ComponentActivity() {
    private lateinit var vpnManager: VpnManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vpnManager = VpnManager(this)

        setContent {
            MaterialTheme {
                HomeScreen(vpnManager = vpnManager)
            }
        }
    }
}

@Composable
fun HomeScreen(vpnManager: VpnManager) {
    var configs by remember { mutableStateOf(listOf<VpnConfig>()) }
    var selectedConfig by remember { mutableStateOf<VpnConfig?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    
    val isConnected by vpnManager.isConnected.collectAsState()
    val trafficStats by vpnManager.trafficStats.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        Text(
            text = "VPN Matroules",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        StatusCard(isConnected, selectedConfig, trafficStats)

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Configurations",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(configs) { config ->
                ConfigItem(
                    config = config,
                    isSelected = config.id == selectedConfig?.id,
                    onSelect = { selectedConfig = config },
                    onDelete = { configs = configs.filter { it.id != config.id } }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add")
            }

            Button(
                onClick = {
                    selectedConfig?.let {
                        if (isConnected) {
                            vpnManager.disconnectVpn()
                        } else {
                            vpnManager.connectVpn(it)
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isConnected) Color(0xFFFF4444) else Color(0xFF4CAF50)
                )
            ) {
                Text(if (isConnected) "Disconnect" else "Connect")
            }
        }
    }

    if (showAddDialog) {
        AddConfigDialog(
            onAdd = { newConfig ->
                configs = configs + newConfig
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
fun StatusCard(
    isConnected: Boolean,
    config: VpnConfig?,
    stats: Pair<Long, Long>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected) Color(0xFF4CAF50) else Color(0xFFCCCCCC)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isConnected) "Connected" else "Disconnected",
                fontSize = 24.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            
            if (config != null) {
                Text(
                    text = config.name,
                    fontSize = 14.sp,
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (isConnected) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Download", fontSize = 12.sp, color = Color.White)
                        Text(
                            formatBytes(stats.first),
                            fontSize = 14.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Upload", fontSize = 12.sp, color = Color.White)
                        Text(
                            formatBytes(stats.second),
                            fontSize = 14.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConfigItem(
    config: VpnConfig,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onSelect() }
            .padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE3F2FD) else Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = config.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$${config.address}:$${config.port}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            IconButton(onClick = { onDelete() }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

@Composable
fun AddConfigDialog(
    onAdd: (VpnConfig) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Add VPN Config") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Config Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                TextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Server Address") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                TextField(
                    value = port,
                    onValueChange = { port = it },
                    label = { Text("Port") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotEmpty() && address.isNotEmpty() && port.isNotEmpty()) {
                        onAdd(
                            VpnConfig(
                                id = System.currentTimeMillis().toString(),
                                name = name,
                                protocol = "shadowsocks",
                                address = address,
                                port = port.toIntOrNull() ?: 8388,
                                password = password
                            )
                        )
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}

fun formatBytes(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "$${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "$${bytes / (1024 * 1024)} MB"
        else -> "$${bytes / (1024 * 1024 * 1024)} GB"
    }
}

data class VpnConfig(
    val id: String,
    val name: String,
    val protocol: String,
    val address: String,
    val port: Int,
    val password: String? = null,
    val isConnected: Boolean = false
)
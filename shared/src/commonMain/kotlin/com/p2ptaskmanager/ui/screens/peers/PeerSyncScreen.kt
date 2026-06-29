package com.p2ptaskmanager.ui.screens.peers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.p2ptaskmanager.ui.components.BottomNavBar
import com.p2ptaskmanager.ui.components.JournalDivider
import com.p2ptaskmanager.ui.components.NavDestination
import com.p2ptaskmanager.ui.viewmodel.PeerSyncViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeerSyncScreen(
    onNavigate: (NavDestination) -> Unit,
    vm: PeerSyncViewModel = koinViewModel()
) {
    val state by vm.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Sync") }) },
        bottomBar = { BottomNavBar(current = NavDestination.PEERS, onNavigate = onNavigate) }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {

            item {
                Text("Nearby (Local Network)", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Discovering")
                    Switch(
                        checked = state.isDiscovering,
                        onCheckedChange = { if (it) vm.startDiscovery() else vm.stopDiscovery() }
                    )
                }
                Spacer(Modifier.height(8.dp))
                JournalDivider()
            }

            if (state.discoveredPeers.isNotEmpty()) {
                item { Text("Nearby Peers", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp)) }
                items(state.discoveredPeers) { peer ->
                    val connected = peer in state.connectedPeers
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(peer.take(16), fontWeight = FontWeight.Medium)
                            Text(if (connected) "Connected" else "Discovered",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (connected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (!connected) {
                            Button(onClick = { vm.connectTo(peer) }) { Text("Connect") }
                        } else {
                            OutlinedButton(onClick = { vm.disconnect(peer) }) { Text("Disconnect") }
                        }
                    }
                    JournalDivider()
                }
            } else if (state.isDiscovering) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Scanning for nearby peers...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item {
                Spacer(Modifier.height(24.dp))
                Text("MQTT Broker", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Text(state.mqttBroker, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Text(
                    if (state.isMqttConnected) "Connected" else "Disconnected",
                    color = if (state.isMqttConnected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error
                )
            }

            if (state.connectedPeers.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(24.dp))
                    Text("Connected Peers", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                }
                items(state.connectedPeers) { peer ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Text("●", color = MaterialTheme.colorScheme.primary)
                        Text(peer, Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
    }
}

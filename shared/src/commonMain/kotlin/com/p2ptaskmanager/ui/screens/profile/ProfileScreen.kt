package com.p2ptaskmanager.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.p2ptaskmanager.ui.components.BottomNavBar
import com.p2ptaskmanager.ui.components.JournalDivider
import com.p2ptaskmanager.ui.components.NavDestination
import com.p2ptaskmanager.ui.viewmodel.ProfileViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigate: (NavDestination) -> Unit,
    vm: ProfileViewModel = koinViewModel()
) {
    val state by vm.uiState.collectAsState()
    var nameInput by remember(state.displayName) { mutableStateOf(state.displayName) }
    var brokerInput by remember(state.mqttBroker) { mutableStateOf(state.mqttBroker) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Profile") }) },
        bottomBar = { BottomNavBar(current = NavDestination.PROFILE, onNavigate = onNavigate) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Profile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            JournalDivider()

            Text("Display Name", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    if (nameInput != state.displayName) {
                        androidx.compose.material3.TextButton(onClick = { vm.setDisplayName(nameInput) }) {
                            Text("Save")
                        }
                    }
                }
            )

            Text("Peer ID", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(state.peerId, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            JournalDivider()
            Text("Theme", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("light", "dark", "system").forEach { mode ->
                    FilterChip(
                        selected = state.themeMode == mode,
                        onClick = { vm.setThemeMode(mode) },
                        label = { Text(mode.replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            JournalDivider()
            Text("MQTT Broker", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = brokerInput,
                onValueChange = { brokerInput = it },
                label = { Text("Broker URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            if (brokerInput != state.mqttBroker) {
                Button(onClick = { vm.setMqttBroker(brokerInput) }) { Text("Update Broker") }
            }

            JournalDivider()
            Text("Data", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Button(onClick = vm::exportData, modifier = Modifier.fillMaxWidth()) {
                Text("Export Data as JSON")
            }
            if (state.exportedJson != null) {
                Spacer(Modifier.height(8.dp))
                Text("${state.exportedJson!!.length} characters exported",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

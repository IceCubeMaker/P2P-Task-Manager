package com.p2ptaskmanager.ui.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.p2ptaskmanager.ui.viewmodel.OnboardingViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    vm: OnboardingViewModel = koinViewModel()
) {
    val state by vm.uiState.collectAsState()

    if (!state.isOnboardingRequired) {
        onFinished()
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        when (state.step) {
            0 -> {
                Text("Welcome", fontSize = 40.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                Text("P2P Task Manager\nYour journal, shared.", fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(40.dp))
                Text("What's your name?", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.displayName,
                    onValueChange = vm::onNameChanged,
                    placeholder = { Text("Display name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = vm::nextStep,
                    enabled = state.displayName.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Next") }
            }
            1 -> {
                Text("Your first collection", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Create your first task group to get started.", fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(32.dp))
                Text("Group name", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.firstGroupName,
                    onValueChange = vm::onGroupNameChanged,
                    placeholder = { Text("My Tasks") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        vm.finish()
                        onFinished()
                    },
                    enabled = state.firstGroupName.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Get Started") }
            }
        }
    }
}

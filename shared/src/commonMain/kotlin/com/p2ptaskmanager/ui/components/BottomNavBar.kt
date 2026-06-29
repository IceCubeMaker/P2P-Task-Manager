package com.p2ptaskmanager.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class NavDestination(val label: String) {
    HOME("Tasks"),
    TODAY("Today"),
    GROUPS("Groups"),
    PEERS("Sync"),
    PROFILE("Profile")
}

@Composable
fun BottomNavBar(
    current: NavDestination,
    onNavigate: (NavDestination) -> Unit,
    isSynced: Boolean = true,
    hasPending: Boolean = false,
    isOffline: Boolean = false
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = androidx.compose.ui.unit.Dp(0f)
    ) {
        NavDestination.entries.forEach { dest ->
            NavigationBarItem(
                selected = current == dest,
                onClick = { onNavigate(dest) },
                icon = {
                    when (dest) {
                        NavDestination.HOME -> Icon(Icons.Default.Home, contentDescription = dest.label)
                        NavDestination.TODAY -> Icon(Icons.Default.CalendarToday, contentDescription = dest.label)
                        NavDestination.GROUPS -> Icon(Icons.Default.Group, contentDescription = dest.label)
                        NavDestination.PEERS -> {
                            androidx.compose.foundation.layout.Box {
                                Icon(Icons.Default.Wifi, contentDescription = dest.label)
                                SyncStatusDot(
                                    isSynced = isSynced,
                                    hasPending = hasPending,
                                    isOffline = isOffline,
                                    modifier = androidx.compose.ui.Modifier.align(androidx.compose.ui.Alignment.TopEnd)
                                )
                            }
                        }
                        NavDestination.PROFILE -> Icon(Icons.Default.Person, contentDescription = dest.label)
                    }
                },
                label = { Text(dest.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

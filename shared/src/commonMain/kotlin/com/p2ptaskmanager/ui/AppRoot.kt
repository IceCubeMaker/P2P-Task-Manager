package com.p2ptaskmanager.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.p2ptaskmanager.ui.navigation.AppNavHost
import com.p2ptaskmanager.ui.navigation.Routes
import com.p2ptaskmanager.ui.theme.BujoTheme
import com.p2ptaskmanager.ui.viewmodel.OnboardingViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppRoot(
    darkTheme: Boolean? = null
) {
    BujoTheme(
        darkTheme = darkTheme ?: androidx.compose.foundation.isSystemInDarkTheme()
    ) {
        val onboardingVm: OnboardingViewModel = koinViewModel()
        val onboardingState by onboardingVm.uiState.collectAsState()

        val navController = rememberNavController()
        val startDest = if (onboardingState.isOnboardingRequired)
            Routes.ONBOARDING else Routes.HOME

        AppNavHost(
            navController = navController,
            startDestination = startDest
        )
    }
}

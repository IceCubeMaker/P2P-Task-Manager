package com.p2ptaskmanager.ui.navigation

import com.p2ptaskmanager.ui.components.NavDestination

val NavDestination.route: String
    get() = when (this) {
        NavDestination.HOME -> Routes.HOME
        NavDestination.TODAY -> Routes.TODAY_PLAN
        NavDestination.GROUPS -> Routes.GROUPS
        NavDestination.PEERS -> Routes.PEERS
        NavDestination.PROFILE -> Routes.PROFILE
    }

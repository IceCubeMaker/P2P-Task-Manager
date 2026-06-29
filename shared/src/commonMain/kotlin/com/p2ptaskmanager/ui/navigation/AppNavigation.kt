package com.p2ptaskmanager.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.p2ptaskmanager.ui.screens.groups.GroupDetailScreen
import com.p2ptaskmanager.ui.screens.groups.GroupsScreen
import com.p2ptaskmanager.ui.screens.home.HomeScreen
import com.p2ptaskmanager.ui.screens.onboarding.OnboardingScreen
import com.p2ptaskmanager.ui.screens.peers.PeerSyncScreen
import com.p2ptaskmanager.ui.screens.plan.TodayPlanScreen
import com.p2ptaskmanager.ui.screens.profile.ProfileScreen
import com.p2ptaskmanager.ui.screens.task.CreateEditTaskScreen
import com.p2ptaskmanager.ui.screens.task.TaskDetailScreen

object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val TODAY_PLAN = "today_plan"
    const val GROUPS = "groups"
    const val GROUP_DETAIL = "group/{groupId}"
    const val PEERS = "peers"
    const val PROFILE = "profile"
    const val TASK_DETAIL = "task/{taskId}"
    const val CREATE_TASK = "create_task?groupId={groupId}"
    const val EDIT_TASK = "edit_task/{taskId}"

    fun groupDetail(groupId: String) = "group/$groupId"
    fun taskDetail(taskId: String) = "task/$taskId"
    fun createTask(groupId: String? = null) = "create_task?groupId=${groupId ?: ""}"
    fun editTask(taskId: String) = "edit_task/$taskId"
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String = Routes.HOME
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(onFinished = { navController.navigate(Routes.HOME) { popUpTo(Routes.ONBOARDING) { inclusive = true } } })
        }
        composable(Routes.HOME) {
            HomeScreen(
                onTaskClick = { navController.navigate(Routes.taskDetail(it)) },
                onCreateTask = { navController.navigate(Routes.createTask()) },
                onNavigate = { dest -> navController.navigate(dest.route) }
            )
        }
        composable(Routes.TODAY_PLAN) {
            TodayPlanScreen(
                onTaskClick = { navController.navigate(Routes.taskDetail(it)) },
                onNavigate = { dest -> navController.navigate(dest.route) }
            )
        }
        composable(Routes.GROUPS) {
            GroupsScreen(
                onGroupClick = { navController.navigate(Routes.groupDetail(it)) },
                onNavigate = { dest -> navController.navigate(dest.route) }
            )
        }
        composable(Routes.GROUP_DETAIL) { backStack ->
            val groupId = backStack.arguments?.getString("groupId") ?: return@composable
            GroupDetailScreen(
                groupId = groupId,
                onBack = { navController.popBackStack() },
                onCreateTask = { navController.navigate(Routes.createTask(groupId)) }
            )
        }
        composable(Routes.PEERS) {
            PeerSyncScreen(onNavigate = { dest -> navController.navigate(dest.route) })
        }
        composable(Routes.PROFILE) {
            ProfileScreen(onNavigate = { dest -> navController.navigate(dest.route) })
        }
        composable(Routes.TASK_DETAIL) { backStack ->
            val taskId = backStack.arguments?.getString("taskId") ?: return@composable
            TaskDetailScreen(
                taskId = taskId,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Routes.editTask(taskId)) },
                onCreateSubtask = { navController.navigate(Routes.createTask()) }
            )
        }
        composable(Routes.CREATE_TASK) { backStack ->
            val groupId = backStack.arguments?.getString("groupId")?.takeIf { it.isNotEmpty() }
            CreateEditTaskScreen(
                taskId = null,
                defaultGroupId = groupId,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
        composable(Routes.EDIT_TASK) { backStack ->
            val taskId = backStack.arguments?.getString("taskId") ?: return@composable
            CreateEditTaskScreen(
                taskId = taskId,
                defaultGroupId = null,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }
    }
}

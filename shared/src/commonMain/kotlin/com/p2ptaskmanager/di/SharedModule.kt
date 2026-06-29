package com.p2ptaskmanager.di

import com.p2ptaskmanager.data.repository.GroupRepository
import com.p2ptaskmanager.data.repository.TaskRepository
import com.p2ptaskmanager.data.repository.TimerRepository
import com.p2ptaskmanager.ui.viewmodel.CreateEditTaskViewModel
import com.p2ptaskmanager.ui.viewmodel.GroupDetailViewModel
import com.p2ptaskmanager.ui.viewmodel.GroupsViewModel
import com.p2ptaskmanager.ui.viewmodel.HomeViewModel
import com.p2ptaskmanager.ui.viewmodel.OnboardingViewModel
import com.p2ptaskmanager.ui.viewmodel.PeerSyncViewModel
import com.p2ptaskmanager.ui.viewmodel.ProfileViewModel
import com.p2ptaskmanager.ui.viewmodel.TaskDetailViewModel
import com.p2ptaskmanager.ui.viewmodel.TodayPlanViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val sharedModule = module {
    // AppDatabase is provided by each platform's module
    single { TaskRepository(get()) }
    single { TimerRepository(get()) }
    single { GroupRepository(get()) }

    viewModel { HomeViewModel(get(), get(), get(), get()) }
    viewModel { TaskDetailViewModel(get(), get(), get()) }
    viewModel { CreateEditTaskViewModel(get(), get(), get()) }
    viewModel { GroupsViewModel(get(), get()) }
    viewModel { GroupDetailViewModel(get(), get()) }
    viewModel { ProfileViewModel(get(), get()) }
    viewModel { OnboardingViewModel(get(), get()) }
    viewModel { TodayPlanViewModel(get(), get(), get()) }
    viewModel { PeerSyncViewModel(get(), get(), get(), get()) }
}

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
import org.koin.dsl.module

val sharedModule = module {
    // AppDatabase is provided by each platform's module
    single { TaskRepository(get()) }
    single { TimerRepository(get()) }
    single { GroupRepository(get()) }

    factory { HomeViewModel(get(), get(), get(), get()) }
    factory { TaskDetailViewModel(get(), get(), get()) }
    factory { CreateEditTaskViewModel(get(), get(), get()) }
    factory { GroupsViewModel(get(), get()) }
    factory { GroupDetailViewModel(get(), get()) }
    factory { ProfileViewModel(get(), get()) }
    factory { OnboardingViewModel(get(), get()) }
    factory { TodayPlanViewModel(get(), get(), get()) }
    factory { PeerSyncViewModel(get(), get(), get(), get()) }
}

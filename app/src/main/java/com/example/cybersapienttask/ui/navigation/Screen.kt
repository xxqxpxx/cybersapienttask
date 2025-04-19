package com.example.cybersapienttask.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cybersapienttask.data.repo.TaskRepository
import com.example.cybersapienttask.ui.screens.TaskCreationScreen
import com.example.cybersapienttask.ui.screens.settings.SettingsScreen
import com.example.cybersapienttask.ui.screens.settings.SettingsViewModel
import com.example.cybersapienttask.ui.screens.taskdetails.TaskDetailScreen
import com.example.cybersapienttask.ui.screens.taskdetails.TaskDetailViewModel
import com.example.cybersapienttask.ui.screens.taskdetails.TaskDetailViewModelFactory
import com.example.cybersapienttask.ui.screens.tasklist.TaskListScreen
import com.example.cybersapienttask.ui.screens.tasklist.TaskListViewModel
import com.example.cybersapienttask.ui.screens.tasklist.TaskListViewModelFactory

sealed class Screen(val route: String) {
    object TaskList : Screen("task_list")
    object TaskCreation : Screen("task_creation?taskId={taskId}") {
        fun createRoute(taskId: Long = 0) = "task_creation?taskId=$taskId"
    }

    object TaskDetail : Screen("task_detail/{taskId}") {
        fun createRoute(taskId: Long) = "task_detail/$taskId"
    }

    object Settings : Screen("settings")
}

@Composable
fun AppNavigation(
    repository: TaskRepository,
    settingsViewModel: SettingsViewModel,
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = Screen.TaskList.route) {
        composable(Screen.TaskList.route) {
            val viewModel: TaskListViewModel = viewModel(
                factory = TaskListViewModelFactory(repository)
            )

            TaskListScreen(
                viewModel = viewModel,
                onTaskClick = { taskId ->
                    navController.navigate(Screen.TaskDetail.createRoute(taskId))
                },
                onAddTask = {
                    navController.navigate(Screen.TaskCreation.createRoute())
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.TaskCreation.route,
            arguments = listOf(
                navArgument("taskId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getLong("taskId") ?: 0L
            val isNewTask = taskId == 0L

            val viewModel: TaskDetailViewModel = viewModel(
                factory = TaskDetailViewModelFactory(
                    repository = repository,
                    savedStateHandle = backStackEntry.savedStateHandle
                )
            )

            TaskCreationScreen(
                viewModel = viewModel,
                isNewTask = isNewTask,
                onNavigateUp = {
                    navController.navigateUp()
                },
                onDeleteTask = {
                    viewModel.deleteTask()
                    navController.navigateUp()
                }
            )
        }

        composable(
            route = Screen.TaskDetail.route,
            arguments = listOf(
                navArgument("taskId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getLong("taskId") ?: 0L

            val viewModel: TaskDetailViewModel = viewModel(
                factory = TaskDetailViewModelFactory(
                    repository = repository,
                    savedStateHandle = backStackEntry.savedStateHandle
                )
            )

            TaskDetailScreen(
                viewModel = viewModel,
                onEditTask = {
                    navController.navigate(Screen.TaskCreation.createRoute(taskId))
                },
                onNavigateUp = {
                    navController.navigateUp()
                },
                onTaskDeleted = {
                    navController.navigateUp()
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateUp = {
                    navController.navigateUp()
                },
                onToggleDarkMode = onToggleDarkTheme,
                isDarkMode = isDarkTheme
            )
        }
    }
}
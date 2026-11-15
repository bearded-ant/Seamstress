package com.example.seamstress.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.seamstress.R
import com.example.seamstress.ui.components.ScreenScaffold
import com.example.seamstress.ui.screens.settings.ClassReferenceScreen
import com.example.seamstress.ui.screens.settings.CreateClassScreen
import com.example.seamstress.ui.screens.settings.MetricsReferenceScreen
import com.example.seamstress.ui.screens.settings.SettingsScreen
import com.example.seamstress.viewmodel.DressClassViewModel
import com.example.seamstress.viewmodel.MetricViewModel

fun NavGraphBuilder.settingsGraph(navController: NavHostController) {
    composable<Settings> {
        ScreenScaffold(
            title = stringResource(R.string.title_settings),
            onBack = { navController.popBackStack() }
        ) {
            SettingsScreen(
                onMetricsClick = { navController.navigate(MetricsReference) },
                onClassesClick = { navController.navigate(ClassReference) }
            )
        }
    }

    composable<MetricsReference> {
        val viewModel: MetricViewModel = viewModel()
        val metrics by viewModel.getAllMetrics.observeAsState(emptyList())

        ScreenScaffold(
            title = stringResource(R.string.title_metrics_reference),
            onBack = { navController.popBackStack() }
        ) {
            MetricsReferenceScreen(
                metrics = metrics,
                onCreateMetric = { shortName, name, description ->
                    viewModel.insertMetric(shortName, name, description)
                }
            )
        }
    }

    composable<ClassReference> {
        val viewModel: DressClassViewModel = viewModel()
        val classes by viewModel.getAllClasses.observeAsState(emptyList())

        ScreenScaffold(
            title = stringResource(R.string.title_class_reference),
            onBack = { navController.popBackStack() }
        ) {
            ClassReferenceScreen(
                classes = classes,
                onAddClass = { navController.navigate(CreateClass) }
            )
        }
    }

    composable<CreateClass> {
        val classViewModel: DressClassViewModel = viewModel()
        val metricViewModel: MetricViewModel = viewModel()
        val metrics by metricViewModel.getAllMetrics.observeAsState(emptyList())
        var saving by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            classViewModel.classSaved.collect {
                navController.navigate(ClassReference) {
                    popUpTo<ClassReference> { inclusive = true }
                }
            }
        }

        ScreenScaffold(
            title = stringResource(R.string.title_create_class),
            onBack = { navController.popBackStack() }
        ) {
            CreateClassScreen(
                metrics = metrics,
                saving = saving,
                onSave = { name, metricIds ->
                    if (!saving) {
                        saving = true
                        classViewModel.insertClass(name, metricIds)
                    }
                },
                onCancel = { navController.popBackStack() }
            )
        }
    }
}

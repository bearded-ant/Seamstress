package com.example.seamstress.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.seamstress.R
import com.example.seamstress.ui.components.ScreenScaffold
import com.example.seamstress.ui.components.formatMeasure
import com.example.seamstress.ui.components.observeLoaded
import com.example.seamstress.ui.components.rememberToast
import com.example.seamstress.ui.screens.settings.ClassReferenceScreen
import com.example.seamstress.ui.screens.settings.CreateClassScreen
import com.example.seamstress.ui.screens.settings.MetricsReferenceScreen
import com.example.seamstress.ui.screens.settings.SettingsScreen
import com.example.seamstress.ui.screens.settings.StandardMetricsScreen
import com.example.seamstress.ui.screens.settings.StandardSetFormScreen
import com.example.seamstress.viewmodel.DressClassViewModel
import com.example.seamstress.viewmodel.MetricViewModel
import com.example.seamstress.viewmodel.StandardSetViewModel

fun NavGraphBuilder.settingsGraph(navController: NavHostController) {
    composable<Settings> {
        ScreenScaffold(
            title = stringResource(R.string.title_settings),
            onBack = { navController.popBackStack() }
        ) {
            SettingsScreen(
                onMetricsClick = { navController.navigate(MetricsReference) },
                onClassesClick = { navController.navigate(ClassReference) },
                onStandardMetricsClick = { navController.navigate(StandardMetricsReference) }
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

    composable<StandardMetricsReference> {
        val viewModel: StandardSetViewModel = viewModel()
        val sets by viewModel.allSets.observeAsState(emptyList())

        ScreenScaffold(
            title = stringResource(R.string.title_standard_metrics_reference),
            onBack = { navController.popBackStack() }
        ) {
            StandardMetricsScreen(
                sets = sets,
                onAddSet = { navController.navigate(CreateStandardSet) },
                onSetClick = { id -> navController.navigate(UpdateStandardSet(id)) }
            )
        }
    }

    composable<CreateStandardSet> {
        val viewModel: StandardSetViewModel = viewModel()
        val classes by viewModel.allClasses.observeAsState(emptyList())
        val allMetrics by viewModel.allMetrics.observeAsState(emptyList())
        val classMetrics by viewModel.classMetrics.collectAsState()
        var saving by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            viewModel.setSaved.collect { navController.popBackStack() }
        }

        ScreenScaffold(
            title = stringResource(R.string.title_create_standard_set),
            onBack = { navController.popBackStack() }
        ) {
            StandardSetFormScreen(
                classes = classes,
                allMetrics = allMetrics,
                classMetrics = classMetrics,
                saving = saving,
                onClassSelected = viewModel::loadClassMetrics,
                onSave = { name, values ->
                    if (!saving) {
                        saving = true
                        viewModel.insertSet(name, values)
                    }
                },
                onCancel = { navController.popBackStack() }
            )
        }
    }

    composable<UpdateStandardSet> { entry ->
        val route = entry.toRoute<UpdateStandardSet>()
        val viewModel: StandardSetViewModel = viewModel()
        val set = remember(route.setId) { viewModel.setById(route.setId) }.observeLoaded()
        val setMetrics = remember(route.setId) { viewModel.setMetrics(route.setId) }.observeLoaded()
        val classes by viewModel.allClasses.observeAsState(emptyList())
        val allMetrics by viewModel.allMetrics.observeAsState(emptyList())
        val classMetrics by viewModel.classMetrics.collectAsState()
        var saving by remember { mutableStateOf(false) }
        var deleting by remember { mutableStateOf(false) }
        val toast = rememberToast()

        LaunchedEffect(set.loaded, set.value) {
            if (set.loaded && set.value == null && !deleting) {
                toast(R.string.standard_set_not_found)
                navController.popBackStack()
            }
        }
        LaunchedEffect(Unit) {
            viewModel.setUpdated.collect { navController.popBackStack() }
        }
        LaunchedEffect(Unit) {
            viewModel.setDeleted.collect {
                navController.navigate(StandardMetricsReference) {
                    popUpTo<StandardMetricsReference> { inclusive = true }
                }
            }
        }

        ScreenScaffold(
            title = stringResource(R.string.title_update_standard_set),
            onBack = { navController.popBackStack() }
        ) {
            val row = set.value
            val metricRows = setMetrics.value
            if (row == null || metricRows == null) {
                Box(modifier = Modifier.fillMaxSize())
            } else {
                StandardSetFormScreen(
                    classes = classes,
                    allMetrics = allMetrics,
                    classMetrics = classMetrics,
                    saving = saving,
                    onClassSelected = viewModel::loadClassMetrics,
                    onSave = { name, values ->
                        if (!saving) {
                            saving = true
                            viewModel.updateSet(route.setId, name, values)
                        }
                    },
                    onCancel = { navController.popBackStack() },
                    onDelete = {
                        deleting = true
                        viewModel.deleteSet(route.setId)
                    },
                    initialName = row.name,
                    initialValues = metricRows.associate { it.metricId to formatMeasure(it.value) }
                )
            }
        }
    }
}

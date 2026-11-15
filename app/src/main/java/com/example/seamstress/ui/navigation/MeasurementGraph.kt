package com.example.seamstress.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.seamstress.ui.screens.measurement.AddMeasurementScreen
import com.example.seamstress.ui.screens.measurement.CardMeasurementScreen
import com.example.seamstress.viewmodel.MeasurementsViewModel

fun NavGraphBuilder.measurementGraph(navController: NavHostController) {
    composable<AddMeasurement> { entry ->
        val route = entry.toRoute<AddMeasurement>()
        val viewModel: MeasurementsViewModel = viewModel()
        val classes by viewModel.allClasses.observeAsState(emptyList())
        val allMetrics by viewModel.allMetrics.observeAsState(emptyList())
        val classMetrics by viewModel.classMetricsLiveData.observeAsState(emptyList())
        var saving by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            viewModel.measurementSaved.collect { navController.popBackStack() }
        }

        ScreenScaffold(
            title = stringResource(R.string.title_add_measurement),
            onBack = { navController.popBackStack() }
        ) {
            AddMeasurementScreen(
                classes = classes,
                allMetrics = allMetrics,
                classMetrics = classMetrics,
                saving = saving,
                onClassSelected = viewModel::loadClassMetrics,
                onSave = { date, name, classId, values ->
                    if (!saving) {
                        saving = true
                        viewModel.addMeasurement(
                            customerId = route.customerId,
                            name = name,
                            classId = classId,
                            dateCreation = date,
                            values = values
                        )
                    }
                },
                onCancel = { navController.popBackStack() }
            )
        }
    }

    composable<CardMeasurement> { entry ->
        val route = entry.toRoute<CardMeasurement>()
        val viewModel: MeasurementsViewModel = viewModel()
        val measurementLive = remember(route.measurementId) {
            viewModel.measurementById(route.measurementId)
        }
        val takenLive = remember(route.measurementId) {
            viewModel.takenMeasurements(route.measurementId)
        }
        val measurement = measurementLive.observeLoaded()
        val taken by takenLive.observeAsState(emptyList())
        val toast = rememberToast()

        LaunchedEffect(measurement.loaded, measurement.value) {
            if (measurement.loaded && measurement.value == null) {
                toast(R.string.measurement_not_found)
                navController.popBackStack()
            }
        }

        ScreenScaffold(
            title = stringResource(R.string.title_card_measurement),
            onBack = { navController.popBackStack() }
        ) {
            CardMeasurementScreen(
                measurement = measurement.value,
                loaded = measurement.loaded,
                taken = taken,
                onEdit = { navController.navigate(UpdateMeasurement(route.measurementId)) }
            )
        }
    }

    composable<UpdateMeasurement> { entry ->
        val route = entry.toRoute<UpdateMeasurement>()
        val viewModel: MeasurementsViewModel = viewModel()
        val measurement = remember(route.measurementId) {
            viewModel.measurementById(route.measurementId)
        }.observeLoaded()
        val taken = remember(route.measurementId) {
            viewModel.takenMeasurements(route.measurementId)
        }.observeLoaded()
        var saving by remember { mutableStateOf(false) }
        val toast = rememberToast()

        LaunchedEffect(measurement.loaded, measurement.value) {
            if (measurement.loaded && measurement.value == null) {
                toast(R.string.measurement_not_found)
                navController.popBackStack()
            }
        }
        LaunchedEffect(Unit) {
            viewModel.measurementUpdated.collect { navController.popBackStack() }
        }

        ScreenScaffold(
            title = stringResource(R.string.title_update_measurement),
            onBack = { navController.popBackStack() }
        ) {
            val row = measurement.value
            val takenRows = taken.value
            if (row == null || takenRows == null) {
                Box(modifier = Modifier.fillMaxSize())
            } else {
                val classes by viewModel.allClasses.observeAsState(emptyList())
                val allMetrics by viewModel.allMetrics.observeAsState(emptyList())
                val classMetrics by viewModel.classMetricsLiveData.observeAsState(emptyList())

                LaunchedEffect(row.classId) { viewModel.loadClassMetrics(row.classId) }

                AddMeasurementScreen(
                    classes = classes,
                    allMetrics = allMetrics,
                    classMetrics = classMetrics,
                    saving = saving,
                    onClassSelected = viewModel::loadClassMetrics,
                    onSave = { date, name, classId, values ->
                        if (!saving) {
                            saving = true
                            viewModel.updateMeasurement(
                                measurementId = route.measurementId,
                                customerId = row.customerId,
                                name = name,
                                classId = classId,
                                dateCreation = date,
                                values = values
                            )
                        }
                    },
                    onCancel = { navController.popBackStack() },
                    initialDate = row.dateCreation,
                    initialName = row.name,
                    initialClassId = row.classId,
                    initialValues = takenRows.associate { it.metricId to formatMeasure(it.value) }
                )
            }
        }
    }
}

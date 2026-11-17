package com.example.seamstress.ui.navigation

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.FileProvider
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
import com.example.seamstress.ui.screens.dress.DressCardScreen
import com.example.seamstress.ui.screens.dress.DressFormScreen
import com.example.seamstress.viewmodel.DressViewModel
import com.example.seamstress.viewmodel.StandardSetViewModel
import java.io.File

fun NavGraphBuilder.dressGraph(navController: NavHostController) {
    composable<DressCard> { entry ->
        val route = entry.toRoute<DressCard>()
        val viewModel: DressViewModel = viewModel()
        val dress = remember(route.dressId) { viewModel.dressById(route.dressId) }.observeLoaded()
        val metrics by remember(route.dressId) { viewModel.dressMetrics(route.dressId) }
            .observeAsState(emptyList())
        val photos by remember(route.dressId) { viewModel.dressPhotos(route.dressId) }
            .observeAsState(emptyList())
        val comments by remember(route.dressId) { viewModel.dressComments(route.dressId) }
            .observeAsState(emptyList())
        val toast = rememberToast()
        val context = LocalContext.current

        var pendingCameraFile by remember { mutableStateOf<File?>(null) }
        val cameraLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->
            val file = pendingCameraFile
            pendingCameraFile = null
            if (file != null) {
                if (success) viewModel.addPhotoFromFile(route.dressId, file) else file.delete()
            }
        }
        val pickLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) viewModel.addPhotoFromUri(route.dressId, uri)
        }

        LaunchedEffect(dress.loaded, dress.value) {
            if (dress.loaded && dress.value == null) {
                toast(R.string.dress_not_found)
                navController.popBackStack()
            }
        }

        ScreenScaffold(
            title = stringResource(R.string.title_dress_card),
            onBack = { navController.popBackStack() }
        ) {
            DressCardScreen(
                dress = dress.value,
                loaded = dress.loaded,
                metrics = metrics,
                photos = photos,
                comments = comments,
                onEdit = { navController.navigate(UpdateDress(route.dressId)) },
                onPickPhoto = {
                    pickLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onTakePhoto = {
                    val file = viewModel.createCameraPhotoFile(route.dressId)
                    pendingCameraFile = file
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    try {
                        cameraLauncher.launch(uri)
                    } catch (e: ActivityNotFoundException) {
                        pendingCameraFile = null
                        file.delete()
                        toast(R.string.dress_camera_unavailable)
                    }
                },
                onDeletePhoto = viewModel::deletePhoto,
                onUpdateMetricValue = { row, value ->
                    viewModel.updateMetricValue(route.dressId, row.metricId, value)
                },
                loadClassMetrics = viewModel::classMetricsOnce,
                loadAllMetrics = viewModel::allMetricsOnce,
                loadNeighborMeasurements = { customerId ->
                    viewModel.neighborMeasurements(customerId, route.dressId)
                },
                onAddMetric = { metricId, value ->
                    viewModel.addMetric(route.dressId, metricId, value)
                },
                onAddComment = { text -> viewModel.addComment(route.dressId, text) },
                onUpdateComment = { comment, text -> viewModel.updateComment(comment, text) },
                onDeleteComment = viewModel::deleteComment,
                onToggleCommentPin = viewModel::toggleCommentPin,
                onReorderComments = viewModel::reorderComments
            )
        }
    }

    composable<AddDress> { entry ->
        val route = entry.toRoute<AddDress>()
        val viewModel: DressViewModel = viewModel()
        val setViewModel: StandardSetViewModel = viewModel()
        val classes by viewModel.allClasses.observeAsState(emptyList())
        val allMetrics by viewModel.allMetrics.observeAsState(emptyList())
        val classMetrics by viewModel.classMetrics.collectAsState()
        val measurements by remember(route.customerId) {
            viewModel.measurementsByCustomer(route.customerId)
        }.observeAsState(emptyList())
        val sets by setViewModel.allSets.observeAsState(emptyList())
        var saving by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            viewModel.dressSaved.collect { dressId ->
                navController.navigate(DressCard(dressId)) {
                    popUpTo<CustomerCard> { inclusive = false }
                }
            }
        }

        ScreenScaffold(
            title = stringResource(R.string.title_add_dress),
            onBack = { navController.popBackStack() }
        ) {
            DressFormScreen(
                classes = classes,
                allMetrics = allMetrics,
                classMetrics = classMetrics,
                measurements = measurements,
                standardSets = sets,
                saving = saving,
                onClassSelected = viewModel::loadClassMetrics,
                loadMeasurementValues = viewModel::measurementValues,
                loadSetValues = { setId ->
                    setViewModel.metricsForSet(setId).map { it.metricId to it.value }
                },
                onSave = { name, classId, price, values ->
                    if (!saving) {
                        saving = true
                        viewModel.addDress(route.customerId, name, classId, price, values)
                    }
                },
                onCancel = { navController.popBackStack() }
            )
        }
    }

    composable<UpdateDress> { entry ->
        val route = entry.toRoute<UpdateDress>()
        val viewModel: DressViewModel = viewModel()
        val setViewModel: StandardSetViewModel = viewModel()
        val dress = remember(route.dressId) { viewModel.dressById(route.dressId) }.observeLoaded()
        val metricRows = remember(route.dressId) { viewModel.dressMetrics(route.dressId) }.observeLoaded()
        val classes by viewModel.allClasses.observeAsState(emptyList())
        val allMetrics by viewModel.allMetrics.observeAsState(emptyList())
        val classMetrics by viewModel.classMetrics.collectAsState()
        val sets by setViewModel.allSets.observeAsState(emptyList())
        var saving by remember { mutableStateOf(false) }
        var deleting by remember { mutableStateOf(false) }
        var deletedCustomerId by remember { mutableStateOf<Long?>(null) }
        val toast = rememberToast()

        LaunchedEffect(dress.loaded, dress.value) {
            if (dress.loaded && dress.value == null && !deleting) {
                toast(R.string.dress_not_found)
                navController.popBackStack()
            }
        }
        LaunchedEffect(Unit) {
            viewModel.dressUpdated.collect { navController.popBackStack() }
        }
        LaunchedEffect(Unit) {
            viewModel.dressDeleted.collect {
                val customerId = deletedCustomerId
                if (customerId != null) {
                    navController.navigate(CustomerCard(customerId)) {
                        popUpTo<CustomerCard> { inclusive = true }
                    }
                } else {
                    navController.popBackStack()
                }
            }
        }

        ScreenScaffold(
            title = stringResource(R.string.title_update_dress),
            onBack = { navController.popBackStack() }
        ) {
            val row = dress.value
            val rows = metricRows.value
            if (row == null || rows == null) {
                Box(modifier = Modifier.fillMaxSize())
            } else {
                val measurements by remember(row.customerId) {
                    viewModel.measurementsByCustomer(row.customerId)
                }.observeAsState(emptyList())

                LaunchedEffect(row.classId) { viewModel.loadClassMetrics(row.classId) }

                DressFormScreen(
                    classes = classes,
                    allMetrics = allMetrics,
                    classMetrics = classMetrics,
                    measurements = measurements,
                    standardSets = sets,
                    saving = saving,
                    onClassSelected = viewModel::loadClassMetrics,
                    loadMeasurementValues = viewModel::measurementValues,
                    loadSetValues = { setId ->
                        setViewModel.metricsForSet(setId).map { it.metricId to it.value }
                    },
                    onSave = { name, classId, price, values ->
                        if (!saving) {
                            saving = true
                            viewModel.updateDress(route.dressId, name, classId, price, values)
                        }
                    },
                    onCancel = { navController.popBackStack() },
                    onDelete = {
                        deleting = true
                        deletedCustomerId = row.customerId
                        viewModel.deleteDress(route.dressId)
                    },
                    initialName = row.name,
                    initialClassId = row.classId,
                    initialPrice = row.price?.toString().orEmpty(),
                    initialValues = rows.associate { it.metricId to formatMeasure(it.value) }
                )
            }
        }
    }
}

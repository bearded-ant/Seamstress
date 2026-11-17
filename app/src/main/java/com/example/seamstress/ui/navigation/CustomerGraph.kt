package com.example.seamstress.ui.navigation

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.seamstress.R
import com.example.seamstress.ui.components.ScreenScaffold
import com.example.seamstress.ui.components.observeLoaded
import com.example.seamstress.ui.components.rememberToast
import com.example.seamstress.ui.screens.customer.CustomerCardScreen
import com.example.seamstress.ui.screens.customer.CustomerFormScreen
import com.example.seamstress.ui.screens.customer.CustomersListScreen
import com.example.seamstress.viewmodel.CustomerFormState
import com.example.seamstress.viewmodel.DressViewModel
import com.example.seamstress.viewmodel.SeamstressViewModel
import java.io.File

fun NavGraphBuilder.customerGraph(navController: NavHostController) {
    composable<CustomersList> {
        val viewModel: SeamstressViewModel = viewModel()
        val customers by viewModel.getAllCustomers.observeAsState(emptyList())

        ScreenScaffold(
            title = stringResource(R.string.title_customers_list),
            actions = {
                IconButton(onClick = { navController.navigate(Settings) }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_baseline_settings_24),
                        contentDescription = stringResource(R.string.settings)
                    )
                }
            }
        ) {
            CustomersListScreen(
                customers = customers,
                onCustomerClick = { id -> navController.navigate(CustomerCard(id)) },
                onAddCustomer = { navController.navigate(AddCustomer) }
            )
        }
    }

    composable<CustomerCard> { entry ->
        val route = entry.toRoute<CustomerCard>()
        val viewModel: SeamstressViewModel = viewModel()
        val dressViewModel: DressViewModel = viewModel()
        val customer = remember(route.customerId) { viewModel.customerById(route.customerId) }.observeLoaded()
        val dressesLive = remember(route.customerId) {
            dressViewModel.dressesByCustomer(route.customerId)
        }
        val dresses by dressesLive.observeAsState(emptyList())
        val toast = rememberToast()
        val context = LocalContext.current

        var pendingCameraFile by remember { mutableStateOf<File?>(null) }
        val cameraLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->
            val file = pendingCameraFile
            pendingCameraFile = null
            if (file != null) {
                if (success) viewModel.setCustomerPhotoFromFile(route.customerId, file) else file.delete()
            }
        }
        val pickLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) viewModel.setCustomerPhotoFromUri(route.customerId, uri)
        }

        LaunchedEffect(customer.loaded, customer.value) {
            if (customer.loaded && customer.value == null) {
                toast(R.string.customer_not_found)
                navController.popBackStack()
            }
        }

        ScreenScaffold(
            title = stringResource(R.string.title_customer),
            onBack = { navController.popBackStack() }
        ) {
            CustomerCardScreen(
                customer = customer.value,
                loaded = customer.loaded,
                dresses = dresses,
                onEdit = { navController.navigate(UpdateCustomer(route.customerId)) },
                onAddDress = { navController.navigate(AddDress(route.customerId)) },
                onDressClick = { id -> navController.navigate(DressCard(id)) },
                onPickPhoto = {
                    pickLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onTakePhoto = {
                    val file = viewModel.createCustomerCameraFile(route.customerId)
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
                        toast(R.string.customer_camera_unavailable)
                    }
                },
                onDeletePhoto = { viewModel.deleteCustomerPhoto(route.customerId) }
            )
        }
    }

    composable<AddCustomer> {
        val viewModel: SeamstressViewModel = viewModel()
        val state by viewModel.formState.observeAsState(CustomerFormState())
        val toast = rememberToast()
        val pickLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) viewModel.addCustomerPhotoFromUri(uri)
        }

        LaunchedEffect(Unit) {
            viewModel.newCustomerId.collect { newCustomerId ->
                navController.navigate(CustomerCard(newCustomerId)) {
                    popUpTo<CustomersList> { inclusive = false }
                }
            }
        }
        LaunchedEffect(Unit) {
            viewModel.saveFailed.collect { toast(R.string.save_failed) }
        }

        ScreenScaffold(
            title = stringResource(R.string.title_add_customer),
            onBack = { navController.popBackStack() }
        ) {
            CustomerFormScreen(
                state = state,
                onNameChange = viewModel::onNameChange,
                onPhoneChange = viewModel::onPhoneChange,
                onBalanceChange = viewModel::onBalanceChange,
                onPickPhoto = {
                    pickLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onSave = { viewModel.saveCustomer() },
                onCancel = {
                    viewModel.discardPendingPhoto()
                    navController.popBackStack()
                }
            )
        }
    }

    composable<UpdateCustomer> { entry ->
        val route = entry.toRoute<UpdateCustomer>()
        val viewModel: SeamstressViewModel = viewModel()
        val state by viewModel.formState.observeAsState(CustomerFormState())
        val customer = remember(route.customerId) { viewModel.customerById(route.customerId) }.observeLoaded()
        val toast = rememberToast()
        val pickLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            if (uri != null) viewModel.addCustomerPhotoFromUri(uri)
        }

        LaunchedEffect(customer.loaded, customer.value) {
            val loaded = customer.value
            if (customer.loaded) {
                if (loaded == null) {
                    toast(R.string.customer_not_found)
                    navController.popBackStack()
                } else {
                    viewModel.fillForm(loaded)
                }
            }
        }
        LaunchedEffect(Unit) {
            viewModel.customerSaved.collect { customerId ->
                navController.navigate(CustomerCard(customerId)) {
                    popUpTo<CustomersList> { inclusive = false }
                }
            }
        }
        LaunchedEffect(Unit) {
            viewModel.saveFailed.collect { toast(R.string.save_failed) }
        }
        LaunchedEffect(Unit) {
            viewModel.customerDeleted.collect {
                navController.navigate(CustomersList) {
                    popUpTo<CustomersList> { inclusive = true }
                }
            }
        }

        ScreenScaffold(
            title = stringResource(R.string.title_update_customer),
            onBack = { navController.popBackStack() }
        ) {
            CustomerFormScreen(
                state = state,
                onNameChange = viewModel::onNameChange,
                onPhoneChange = viewModel::onPhoneChange,
                onBalanceChange = viewModel::onBalanceChange,
                onPickPhoto = {
                    pickLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onSave = { viewModel.saveCustomer(route.customerId) },
                onCancel = {
                    viewModel.discardPendingPhoto()
                    navController.popBackStack()
                },
                onDelete = customer.value?.let { loaded ->
                    { viewModel.deleteCustomer(loaded) }
                }
            )
        }
    }
}

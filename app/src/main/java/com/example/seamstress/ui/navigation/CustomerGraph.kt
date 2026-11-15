package com.example.seamstress.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import com.example.seamstress.viewmodel.MeasurementsViewModel
import com.example.seamstress.viewmodel.SeamstressViewModel

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
        val measurementsViewModel: MeasurementsViewModel = viewModel()
        val customer = viewModel.selectByIdCustomerLiveData.observeLoaded()
        val measurementsLive = remember(route.customerId) {
            measurementsViewModel.measurementsByCustomer(route.customerId)
        }
        val measurements by measurementsLive.observeAsState(emptyList())
        val toast = rememberToast()

        LaunchedEffect(route.customerId) { viewModel.selectCustomerById(route.customerId) }
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
                measurements = measurements,
                onEdit = { navController.navigate(UpdateCustomer(route.customerId)) },
                onAddMeasurement = { navController.navigate(AddMeasurement(route.customerId)) },
                onMeasurementClick = { id -> navController.navigate(CardMeasurement(id)) }
            )
        }
    }

    composable<AddCustomer> {
        val viewModel: SeamstressViewModel = viewModel()
        val state by viewModel.formState.observeAsState(CustomerFormState())
        val toast = rememberToast()

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
                onSave = { viewModel.saveCustomer() },
                onCancel = { navController.popBackStack() }
            )
        }
    }

    composable<UpdateCustomer> { entry ->
        val route = entry.toRoute<UpdateCustomer>()
        val viewModel: SeamstressViewModel = viewModel()
        val state by viewModel.formState.observeAsState(CustomerFormState())
        val customer = viewModel.selectByIdCustomerLiveData.observeLoaded()
        val toast = rememberToast()

        LaunchedEffect(route.customerId) { viewModel.selectCustomerById(route.customerId) }
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
                onSave = { viewModel.saveCustomer(route.customerId) },
                onCancel = { navController.popBackStack() },
                onDelete = customer.value?.let { loaded ->
                    { viewModel.deleteCustomer(loaded) }
                }
            )
        }
    }
}

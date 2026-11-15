package com.example.seamstress.ui.navigation

import kotlinx.serialization.Serializable

/** Маршруты графа: типизированные, вместо `navigation.xml` и Safe Args. */
@Serializable
sealed interface Route

@Serializable
data object CustomersList : Route

@Serializable
data class CustomerCard(val customerId: Long) : Route

@Serializable
data object AddCustomer : Route

@Serializable
data class UpdateCustomer(val customerId: Long) : Route

@Serializable
data class AddMeasurement(val customerId: Long) : Route

@Serializable
data class CardMeasurement(val measurementId: Long) : Route

@Serializable
data class UpdateMeasurement(val measurementId: Long) : Route

@Serializable
data object Settings : Route

@Serializable
data object MetricsReference : Route

@Serializable
data object ClassReference : Route

@Serializable
data object CreateClass : Route

package com.example.seamstress.ui.fragments.measurement.update

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.seamstress.ui.fragments.measurement.add.AddMeasurementScreen
import com.example.seamstress.ui.theme.SeamstressTheme
import com.example.seamstress.viewmodel.MeasurementsViewModel
import kotlinx.coroutines.launch


class UpdateMeasurementFragment : Fragment() {
    companion object {
        fun newInstance(): UpdateMeasurementFragment = UpdateMeasurementFragment()
    }

    private val viewModel by lazy { ViewModelProvider(this)[MeasurementsViewModel::class.java] }
    private val args by lazy { UpdateMeasurementFragmentArgs.fromBundle(requireArguments()) }
    private val measurementLive by lazy { viewModel.measurementById(args.measurementId) }
    private val takenLive by lazy { viewModel.takenMeasurements(args.measurementId) }
    private val saving = mutableStateOf(false)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            val measurement by measurementLive.observeAsState()
            val taken by takenLive.observeAsState()
            val row = measurement
            val takenRows = taken

            SeamstressTheme {
                if (row == null || takenRows == null) {
                    Box(modifier = Modifier.fillMaxSize())
                    return@SeamstressTheme
                }

                LaunchedEffect(row.classId) { viewModel.loadClassMetrics(row.classId) }

                val classes by viewModel.allClasses.observeAsState(emptyList())
                val allMetrics by viewModel.allMetrics.observeAsState(emptyList())
                val classMetrics by viewModel.classMetricsLiveData.observeAsState(emptyList())

                AddMeasurementScreen(
                    classes = classes,
                    allMetrics = allMetrics,
                    classMetrics = classMetrics,
                    saving = saving.value,
                    onClassSelected = { classId -> viewModel.loadClassMetrics(classId) },
                    onSave = { date, name, classId, values ->
                        if (!saving.value) {
                            saving.value = true
                            viewModel.updateMeasurement(
                                measurementId = args.measurementId,
                                customerId = row.customerId,
                                name = name,
                                classId = classId,
                                dateCreation = date,
                                values = values
                            )
                        }
                    },
                    onCancel = { findNavController().navigateUp() },
                    initialDate = row.dateCreation,
                    initialName = row.name,
                    initialClassId = row.classId,
                    initialValues = takenRows.associate { it.metricId to formatMeasure(it.value) }
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.measurementUpdated.collect {
                findNavController().popBackStack()
            }
        }
    }
}

private fun formatMeasure(value: Float): String =
    if (value == value.toLong().toFloat()) value.toLong().toString() else value.toString()

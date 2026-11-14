package com.example.seamstress.ui.fragments.measurement.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.seamstress.ui.theme.SeamstressTheme
import com.example.seamstress.viewmodel.MeasurementsViewModel
import kotlinx.coroutines.launch


class AddMeasurementFragment : Fragment() {
    companion object {
        fun newInstance(): AddMeasurementFragment = AddMeasurementFragment()
    }

    private val viewModel by lazy { ViewModelProvider(this)[MeasurementsViewModel::class.java] }
    private val args by lazy { AddMeasurementFragmentArgs.fromBundle(requireArguments()) }
    private val saving = mutableStateOf(false)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            val classes by viewModel.allClasses.observeAsState(emptyList())
            val allMetrics by viewModel.allMetrics.observeAsState(emptyList())
            val classMetrics by viewModel.classMetricsLiveData.observeAsState(emptyList())
            SeamstressTheme {
                AddMeasurementScreen(
                    classes = classes,
                    allMetrics = allMetrics,
                    classMetrics = classMetrics,
                    saving = saving.value,
                    onClassSelected = { classId -> viewModel.loadClassMetrics(classId) },
                    onSave = { date, name, classId, values ->
                        if (!saving.value) {
                            saving.value = true
                            viewModel.addMeasurement(
                                customerId = args.customerId,
                                name = name,
                                classId = classId,
                                dateCreation = date,
                                values = values
                            )
                        }
                    },
                    onCancel = { findNavController().navigateUp() }
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.measurementSaved.collect {
                findNavController().popBackStack()
            }
        }
    }
}

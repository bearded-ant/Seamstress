package com.example.seamstress.ui.fragments.measurement.card

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.seamstress.ui.theme.SeamstressTheme
import com.example.seamstress.viewmodel.MeasurementsViewModel


class CardMeasurementFragment : Fragment() {
    companion object {
        fun newInstance(): CardMeasurementFragment = CardMeasurementFragment()
    }

    private val viewModel by lazy { ViewModelProvider(this)[MeasurementsViewModel::class.java] }
    private val args by lazy { CardMeasurementFragmentArgs.fromBundle(requireArguments()) }
    private val measurementLive by lazy { viewModel.measurementById(args.measurementId) }
    private val takenLive by lazy { viewModel.takenMeasurements(args.measurementId) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            val measurement by measurementLive.observeAsState()
            val taken by takenLive.observeAsState(emptyList())
            SeamstressTheme {
                CardMeasurementScreen(
                    measurement = measurement,
                    taken = taken,
                    onEdit = {
                        findNavController().navigate(
                            CardMeasurementFragmentDirections
                                .actionCardMeasurementToUpdateMeasurement(args.measurementId)
                        )
                    }
                )
            }
        }
    }
}

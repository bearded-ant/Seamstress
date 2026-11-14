package com.example.seamstress.ui.fragments.settings

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
import com.example.seamstress.ui.theme.SeamstressTheme
import com.example.seamstress.viewmodel.MetricViewModel

class MetricsReferenceFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val metricViewModel = ViewModelProvider(this)[MetricViewModel::class.java]
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val metrics by metricViewModel.getAllMetrics.observeAsState(emptyList())
                SeamstressTheme {
                    MetricsReferenceScreen(
                        metrics = metrics,
                        onCreateMetric = { shortName, name, description ->
                            metricViewModel.insertMetric(shortName, name, description)
                        }
                    )
                }
            }
        }
    }
}

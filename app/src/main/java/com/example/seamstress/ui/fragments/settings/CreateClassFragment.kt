package com.example.seamstress.ui.fragments.settings

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
import com.example.seamstress.viewmodel.DressClassViewModel
import com.example.seamstress.viewmodel.MetricViewModel
import kotlinx.coroutines.launch

class CreateClassFragment : Fragment() {

    private val classViewModel by lazy {
        ViewModelProvider(this)[DressClassViewModel::class.java]
    }
    private val metricViewModel by lazy {
        ViewModelProvider(this)[MetricViewModel::class.java]
    }
    private val saving = mutableStateOf(false)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            val metrics by metricViewModel.getAllMetrics.observeAsState(emptyList())
            SeamstressTheme {
                CreateClassScreen(
                    metrics = metrics,
                    saving = saving.value,
                    onSave = { name, metricIds ->
                        if (!saving.value) {
                            saving.value = true
                            classViewModel.insertClass(name, metricIds)
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
            classViewModel.classSaved.collect {
                findNavController().navigate(
                    CreateClassFragmentDirections.actionCreateClassToClassReference()
                )
            }
        }
    }
}

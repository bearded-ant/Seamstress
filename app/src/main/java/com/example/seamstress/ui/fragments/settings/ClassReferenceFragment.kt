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
import androidx.navigation.fragment.findNavController
import com.example.seamstress.ui.theme.SeamstressTheme
import com.example.seamstress.viewmodel.DressClassViewModel

class ClassReferenceFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel = ViewModelProvider(this)[DressClassViewModel::class.java]
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val classes by viewModel.getAllClasses.observeAsState(emptyList())
                SeamstressTheme {
                    ClassReferenceScreen(
                        classes = classes,
                        onAddClass = {
                            findNavController().navigate(
                                ClassReferenceFragmentDirections.actionClassReferenceToCreateClass()
                            )
                        }
                    )
                }
            }
        }
    }
}

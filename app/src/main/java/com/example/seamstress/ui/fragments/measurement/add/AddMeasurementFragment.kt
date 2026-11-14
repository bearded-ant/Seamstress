package com.example.seamstress.ui.fragments.measurement.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.seamstress.ui.theme.SeamstressTheme


class AddMeasurementFragment : Fragment() {
    companion object {
        fun newInstance(): AddMeasurementFragment = AddMeasurementFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            SeamstressTheme {
                AddMeasurementScreen(
                    onSave = { _, _ ->
                        // TODO: сохранять замер вместе с мерками (этап 1, п.7)
                        Toast.makeText(
                            requireContext(),
                            "Сохранение замера ещё не подключено",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onCancel = { findNavController().navigateUp() }
                )
            }
        }
    }
}

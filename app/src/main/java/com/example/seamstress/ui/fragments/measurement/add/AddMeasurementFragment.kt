package com.example.seamstress.ui.fragments.measurement.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.seamstress.databinding.FragmentAddMeasurementBinding
import com.example.seamstress.viewmodel.SeamstressViewModel


class AddMeasurementFragment : Fragment() {
    companion object {
        fun newInstance(): AddMeasurementFragment = AddMeasurementFragment()
    }

    private lateinit var seamstressViewModel: SeamstressViewModel

    private var _binding: FragmentAddMeasurementBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddMeasurementBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
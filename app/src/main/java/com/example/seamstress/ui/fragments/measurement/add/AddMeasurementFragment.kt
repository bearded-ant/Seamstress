package com.example.seamstress.ui.fragments.measurement.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
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

        seamstressViewModel = ViewModelProvider(requireActivity())[SeamstressViewModel::class.java]
        seamstressViewModel.getAllMetrics.observe(requireActivity()) {

            val recycler = binding.frAddMeasurementRecycler
            recycler.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            val adapter = MetricRecyclerAdapter(it)
            recycler.adapter = adapter

        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
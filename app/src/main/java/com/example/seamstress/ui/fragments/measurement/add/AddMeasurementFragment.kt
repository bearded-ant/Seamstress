package com.example.seamstress.ui.fragments.measurement.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.seamstress.databinding.FragmentAddMeasurementBinding
import com.example.seamstress.viewmodel.MetricViewModel


class AddMeasurementFragment : Fragment() {
    companion object {
        fun newInstance(): AddMeasurementFragment = AddMeasurementFragment()
    }

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.frAddMeasurementRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        val metricViewModel = ViewModelProvider(this)[MetricViewModel::class.java]
        metricViewModel.getAllMetrics.observe(viewLifecycleOwner) {
            binding.frAddMeasurementRecycler.adapter = MetricRecyclerAdapter(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.example.seamstress.ui.fragments.measurement.card

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.seamstress.databinding.FragmentCardMeasurementBinding
import com.example.seamstress.domain.measured.taken.TakenMeasurement
import com.example.seamstress.ui.fragments.measurement.card.recycler.TakenMeasuredRecyclerAdapter
import com.example.seamstress.viewmodel.MeasurementsViewModel
import com.example.seamstress.viewmodel.TakenMeasurementsViewModel


class CardMeasurementFragment : Fragment() {
    companion object {
        fun newInstance(): CardMeasurementFragment = CardMeasurementFragment()
    }

    private var _binding: FragmentCardMeasurementBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCardMeasurementBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val measurementId = CardMeasurementFragmentArgs.fromBundle(requireArguments()).measurementId

        val measurementsViewModel = ViewModelProvider(this)[MeasurementsViewModel::class.java]
        measurementsViewModel.getMeasurementById(measurementId)
        measurementsViewModel.measurementByIdLiveData.observe(viewLifecycleOwner) { measurement ->
            binding.frCardMeasurementName.text = measurement.name
            binding.frCardMeasurementType.text = measurement.dressType
            binding.frCardMeasurementDate.text = measurement.dateCreation
        }

        val takenMeasurementViewModel =
            ViewModelProvider(this)[TakenMeasurementsViewModel::class.java]
        takenMeasurementViewModel.getMeasurementByCustomerId(measurementId)
        takenMeasurementViewModel.measurementLiveData.observe(viewLifecycleOwner) {
            initTakenRecycler(it)
        }

        binding.frCardMeasurementRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initTakenRecycler(taken: List<TakenMeasurement>) {
        binding.frCardMeasurementRecycler.adapter = TakenMeasuredRecyclerAdapter(taken)
    }
}

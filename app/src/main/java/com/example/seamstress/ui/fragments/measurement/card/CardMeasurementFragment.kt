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

    private lateinit var takenMeasurementViewModel: TakenMeasurementsViewModel
    private lateinit var measurementsViewModel: MeasurementsViewModel

    private var _binding: FragmentCardMeasurementBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCardMeasurementBinding.inflate(layoutInflater)

        val safeArgs = CardMeasurementFragmentArgs.fromBundle(requireArguments())
        val measurementId = safeArgs.measurementId

        measurementsViewModel =
            ViewModelProvider(requireActivity())[MeasurementsViewModel::class.java]
        measurementsViewModel.getMeasurementById(measurementId)
        measurementsViewModel.measurementByIdLiveData.observe(viewLifecycleOwner) { measurement ->
            binding.frCardMeasurementName.text = measurement.name
            binding.frCardMeasurementType.text = measurement.dressType
            binding.frCardMeasurementDate.text = measurement.dateCreation
        }

        takenMeasurementViewModel =
            ViewModelProvider(requireActivity())[TakenMeasurementsViewModel::class.java]
        takenMeasurementViewModel.getMeasurementByCustomerId(measurementId)
        takenMeasurementViewModel.measurementLiveData.observe(viewLifecycleOwner) {
            initTakenRecycler(it)
        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun initTakenRecycler(taken: List<TakenMeasurement>) {
        val takenRecycler = binding.frCardMeasurementRecycler
        val takenRecyclerAdapter = TakenMeasuredRecyclerAdapter(taken)

        takenRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        takenRecycler.adapter = takenRecyclerAdapter
    }
}

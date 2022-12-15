package com.example.seamstress.ui.fragments.customer.card

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.seamstress.R
import com.example.seamstress.databinding.FragmentCardCustomerBinding
import com.example.seamstress.domain.customers.Customers
import com.example.seamstress.domain.measured.measurements.Measurement
import com.example.seamstress.ui.fragments.customer.card.recycler.MeasurementRecyclerAdapter
import com.example.seamstress.viewmodel.MeasurementsViewModel
import com.example.seamstress.viewmodel.SeamstressViewModel

class CustomerCardFragment : Fragment() {
    companion object {
        fun newInstance(): CustomerCardFragment = CustomerCardFragment()
    }

    private lateinit var seamstressViewModel: SeamstressViewModel
    private lateinit var measurementsViewModel: MeasurementsViewModel

    private var _binding: FragmentCardCustomerBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCardCustomerBinding.inflate(layoutInflater)

//todo на писать проверку на наличие аргумента?
        val safeArgs = CustomerCardFragmentArgs.fromBundle(requireArguments())
        val customerId = safeArgs.customerEntityId

        seamstressViewModel = ViewModelProvider(this)[SeamstressViewModel::class.java]
        seamstressViewModel.selectCustomerById(customerId)
        seamstressViewModel.selectByIdCustomerLiveData.observe(requireActivity()) {
            bindCustomerCard(it)
        }

        measurementsViewModel = ViewModelProvider(this)[MeasurementsViewModel::class.java]

        measurementsViewModel.getMeasurementByCustomerId(customerId)
        measurementsViewModel.measurementLiveData.observe(viewLifecycleOwner) {
            initMeasurementsRecycler(it)
        }

        binding.floatingActionButton.setOnClickListener {
            val action =
                CustomerCardFragmentDirections.actionCustomerCardToUpdateCustomer(customerId)
            findNavController().navigate(action)
        }

        binding.itemCustomer.itemCustomerBtnAddMeasurement.setOnClickListener {
            findNavController().navigate(CustomerCardFragmentDirections.actionCustomerCardToAddMeasurement())
        }

        return binding.root

    }

    private fun initMeasurementsRecycler(measurements: List<Measurement>) {
        val measurementsRecycler = binding.frCardCustomerRecyclerMeasurements
        measurementsRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val adapter = MeasurementRecyclerAdapter(measurements)
        measurementsRecycler.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun bindCustomerCard(customer: Customers) {
        binding.itemCustomer.itemCustomerPhoto.setImageResource(R.drawable.ic_baseline_add_a_photo_24)
        binding.itemCustomer.itemCustomerName.text = customer.name
        binding.itemCustomer.itemCustomerPhone.text = customer.phone
        binding.itemCustomer.itemCustomerBalance.text = customer.balance.toString()
    }
}
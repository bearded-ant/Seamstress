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
import com.example.seamstress.ui.fragments.customer.card.recycler.MeasurementItemClickListener
import com.example.seamstress.ui.fragments.customer.card.recycler.MeasurementRecyclerAdapter
import com.example.seamstress.viewmodel.MeasurementsViewModel
import com.example.seamstress.viewmodel.SeamstressViewModel

class CustomerCardFragment : Fragment(), MeasurementItemClickListener {
    companion object {
        fun newInstance(): CustomerCardFragment = CustomerCardFragment()
    }

    private var _binding: FragmentCardCustomerBinding? = null
    private val binding
        get() = _binding!!

    private var customerId: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCardCustomerBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        customerId = CustomerCardFragmentArgs.fromBundle(requireArguments()).customerEntityId

        val seamstressViewModel = ViewModelProvider(this)[SeamstressViewModel::class.java]
        seamstressViewModel.selectCustomerById(customerId)
        seamstressViewModel.selectByIdCustomerLiveData.observe(viewLifecycleOwner) {
            bindCustomerCard(it)
        }

        val measurementsViewModel = ViewModelProvider(this)[MeasurementsViewModel::class.java]
        measurementsViewModel.getMeasurementByCustomerId(customerId)
        measurementsViewModel.measurementLiveData.observe(viewLifecycleOwner) {
            initMeasurementsRecycler(it)
        }

        binding.frCardCustomerRecyclerMeasurements.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        binding.floatingActionButton.setOnClickListener {
            val action =
                CustomerCardFragmentDirections.actionCustomerCardToUpdateCustomer(customerId)
            findNavController().navigate(action)
        }

        binding.itemCustomer.itemCustomerBtnAddMeasurement.setOnClickListener {
            findNavController().navigate(CustomerCardFragmentDirections.actionCustomerCardToAddMeasurement())
        }
    }

    private fun initMeasurementsRecycler(measurements: List<Measurement>) {
        binding.frCardCustomerRecyclerMeasurements.adapter =
            MeasurementRecyclerAdapter(this, measurements)
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

    override fun onMeasureItemClick(measurement: Measurement) {
        val action =
            CustomerCardFragmentDirections.actionCustomerCardToMeasurementCard(measurement.id)
        findNavController().navigate(action)
    }
}

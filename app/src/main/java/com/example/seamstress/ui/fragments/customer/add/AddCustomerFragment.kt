package com.example.seamstress.ui.fragments.customer.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.seamstress.R
import com.example.seamstress.databinding.FragmentAddCustomerBinding
import com.example.seamstress.domain.customers.Customers
import com.example.seamstress.ui.fragments.customer.card.CustomerCardFragment
import com.example.seamstress.viewmodel.SeamstressViewModel

class AddCustomerFragment : Fragment() {
    companion object {
        fun newInstance(): CustomerCardFragment = CustomerCardFragment()
    }

    private lateinit var seamstressViewModel: SeamstressViewModel

    private var _binding: FragmentAddCustomerBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddCustomerBinding.inflate(layoutInflater)

        seamstressViewModel = ViewModelProvider(this)[SeamstressViewModel::class.java]

        binding.frNewCustomerBtnCancel.setOnClickListener {
            findNavController().navigate(AddCustomerFragmentDirections.actionAddCustomerToCustomersList())
        }
        binding.frNewCustomerBtnSave.setOnClickListener {
            val customer = Customers(
                id = 0,
                name = binding.frNewCustomerName.text.toString(),
                phone = binding.frNewCustomerPhone.text.toString(),
                img = R.drawable.ic_baseline_add_a_photo_24,
                balance = binding.frNewCustomerBalance.text.toString().toFloat()
            )
            seamstressViewModel.insertCustomer(customer)
            seamstressViewModel.newCustomerIdLiveData.observe(requireActivity()) {
                Toast.makeText(requireContext(), "$it", Toast.LENGTH_LONG).show()
                findNavController().navigate(
                    AddCustomerFragmentDirections.actionAddCustomerToCustomerCard(it)
                )
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
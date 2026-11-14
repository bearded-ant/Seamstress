package com.example.seamstress.ui.fragments.customer.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.seamstress.R
import com.example.seamstress.databinding.FragmentAddCustomerBinding
import com.example.seamstress.domain.customers.Customers
import com.example.seamstress.ui.fragments.customer.card.CustomerCardFragment
import com.example.seamstress.ui.fragments.customer.parseBalance
import com.example.seamstress.viewmodel.SeamstressViewModel
import kotlinx.coroutines.launch

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
            val name = binding.frNewCustomerName.text?.toString()?.trim().orEmpty()
            val phone = binding.frNewCustomerPhone.text?.toString()?.trim().orEmpty()
            val balance = parseBalance(binding.frNewCustomerBalance.text?.toString())
            if (name.isEmpty()) {
                binding.frNewCustomerName.error = getString(R.string.error_customer_name_empty)
                return@setOnClickListener
            }
            if (balance == null) {
                binding.frNewCustomerBalance.error = getString(R.string.error_customer_balance_invalid)
                return@setOnClickListener
            }
            binding.frNewCustomerBtnSave.isEnabled = false
            val customer = Customers(
                id = 0,
                name = name,
                phone = phone.ifEmpty { null },
                img = R.drawable.ic_baseline_add_a_photo_24,
                balance = balance
            )
            seamstressViewModel.insertCustomer(customer)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            seamstressViewModel.newCustomerId.collect { newCustomerId ->
                Toast.makeText(requireContext(), "$newCustomerId", Toast.LENGTH_LONG).show()
                findNavController().navigate(
                    AddCustomerFragmentDirections.actionAddCustomerToCustomerCard(newCustomerId)
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
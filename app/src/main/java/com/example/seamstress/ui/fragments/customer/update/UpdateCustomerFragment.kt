package com.example.seamstress.ui.fragments.customer.update

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.seamstress.R
import com.example.seamstress.databinding.FragmentUpdateCustomerBinding
import com.example.seamstress.domain.customers.Customers
import com.example.seamstress.ui.fragments.customer.card.CustomerCardFragment
import com.example.seamstress.ui.fragments.customer.parseBalance
import com.example.seamstress.ui.fragments.customer.parsePhone
import com.example.seamstress.viewmodel.SeamstressViewModel

class UpdateCustomerFragment : Fragment() {
    companion object {
        fun newInstance(): CustomerCardFragment = CustomerCardFragment()
    }

    private lateinit var seamstressViewModel: SeamstressViewModel

    private var _binding: FragmentUpdateCustomerBinding? = null
    private val binding
        get() = _binding!!

    private var customerId: Long = 0L
    private var customer: Customers? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpdateCustomerBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        customerId = UpdateCustomerFragmentArgs.fromBundle(requireArguments()).customerEntityId

        seamstressViewModel = ViewModelProvider(this)[SeamstressViewModel::class.java]
        seamstressViewModel.selectCustomerById(customerId)
        seamstressViewModel.selectByIdCustomerLiveData.observe(viewLifecycleOwner) { loaded ->
            customer = loaded
            bindCustomerCard(loaded)
        }

        binding.frUpdateCustomerBtnSave.setOnClickListener { saveCustomer() }
        binding.frUpdateCustomerBtnDelete.setOnClickListener { confirmDelete() }
        binding.frUpdateCustomerBtnCancel.setOnClickListener {
            findNavController().navigate(
                UpdateCustomerFragmentDirections.actionUpdateCustomerToCustomerCard(customerId)
            )
        }
    }

    private fun saveCustomer() {
        val current = customer ?: return

        val name = binding.frUpdateCustomerName.text?.toString()?.trim().orEmpty()
        val phone = parsePhone(binding.frUpdateCustomerPhone.text?.toString())
        val balance = parseBalance(binding.frUpdateCustomerBalance.text?.toString())
        if (name.isEmpty()) {
            binding.frUpdateCustomerName.error = getString(R.string.error_customer_name_empty)
            return
        }
        if (phone == null) {
            binding.frUpdateCustomerPhone.error =
                getString(R.string.error_customer_phone_invalid)
            return
        }
        if (balance == null) {
            binding.frUpdateCustomerBalance.error =
                getString(R.string.error_customer_balance_invalid)
            return
        }

        binding.frUpdateCustomerBtnSave.isEnabled = false
        val updatedCustomer = Customers(
            id = current.id,
            name = name,
            phone = phone.ifEmpty { null },
            img = R.drawable.ic_baseline_add_a_photo_24,
            balance = balance
        )
        seamstressViewModel.updateCustomer(updatedCustomer)
        Toast.makeText(requireContext(), "success update", Toast.LENGTH_SHORT).show()
        findNavController().navigate(
            UpdateCustomerFragmentDirections.actionUpdateCustomerToCustomerCard(customerId)
        )
    }

    private fun confirmDelete() {
        val current = customer ?: return
        AlertDialog.Builder(requireContext())
            .setPositiveButton("Да") { _, _ ->
                seamstressViewModel.deleteCustomer(current)
                findNavController().navigate(
                    UpdateCustomerFragmentDirections.actionUpdateCustomerToCustomersList()
                )
            }
            .setNegativeButton("Нет") { _, _ -> }
            .setTitle("Удалить клиента ${current.name}?")
            .setMessage("Вы уверены, что хотите удалить клиента ${current.name}?")
            .create()
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun bindCustomerCard(customer: Customers) {
        binding.frUpdateCustomerPhoto.setImageResource(R.drawable.ic_baseline_add_a_photo_24)
        binding.frUpdateCustomerName.setText(customer.name)
        binding.frUpdateCustomerPhone.setText(customer.phone)
        binding.frUpdateCustomerBalance.setText(customer.balance.toString())
    }
}

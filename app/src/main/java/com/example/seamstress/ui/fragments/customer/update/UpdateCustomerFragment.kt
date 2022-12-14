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
import com.example.seamstress.viewmodel.SeamstressViewModel

class UpdateCustomerFragment : Fragment() {
    companion object {
        fun newInstance(): CustomerCardFragment = CustomerCardFragment()
    }

    private lateinit var seamstressViewModel: SeamstressViewModel

    private var _binding: FragmentUpdateCustomerBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpdateCustomerBinding.inflate(layoutInflater)

        seamstressViewModel = ViewModelProvider(this)[SeamstressViewModel::class.java]
        val safeArg = UpdateCustomerFragmentArgs.fromBundle(requireArguments())
        val customerId = safeArg.customerEntityId

        seamstressViewModel = ViewModelProvider(this)[SeamstressViewModel::class.java]
        seamstressViewModel.selectCustomerById(customerId)
        seamstressViewModel.selectByIdCustomerLiveData.observe(requireActivity()) { customer ->
            bindCustomerCard(customer)

            binding.frUpdateCustomerBtnSave.setOnClickListener {
                val updatedCustomer = Customers(
                    id = customer.id,
                    name = binding.frUpdateCustomerName.text.toString(),
                    phone = binding.frUpdateCustomerPhone.text.toString(),
                    img = R.drawable.ic_baseline_add_a_photo_24,
                    balance = binding.frUpdateCustomerBalance.text.toString().toFloat()
                )
                seamstressViewModel.update(updatedCustomer)
                Toast.makeText(requireContext(), "success update", Toast.LENGTH_SHORT).show()
                findNavController().navigate(
                    UpdateCustomerFragmentDirections.actionUpdateCustomerToCustomerCard(
                        customerId
                    )
                )
            }

            binding.frUpdateCustomerBtnDelete.setOnClickListener {


                val alertBuilder = AlertDialog.Builder(requireContext())
                alertBuilder.setPositiveButton("Да") { _, _ ->
                    seamstressViewModel.delete(customer)
                    findNavController().navigate(UpdateCustomerFragmentDirections.actionUpdateCustomerToCustomersList())
                }
                alertBuilder.setNegativeButton("Нет") { _, _ -> }
                alertBuilder.setTitle("Удалить клиента ${customer.name}?")
                alertBuilder.setMessage("Вы уверены, что хотите удалить клиента ${customer.name}?")
                alertBuilder.create().show()
            }

            binding.frUpdateCustomerBtnCancel.setOnClickListener {
                findNavController().navigate(
                    UpdateCustomerFragmentDirections.actionUpdateCustomerToCustomerCard(customerId)
                )
            }
        }

        return binding.root
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
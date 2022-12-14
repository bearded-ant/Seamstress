package com.example.seamstress.ui.fragments.customer.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.seamstress.databinding.FragmentCustomersListBinding
import com.example.seamstress.domain.client.Customers
import com.example.seamstress.ui.fragments.customer.list.recycler.CustomerClickListener
import com.example.seamstress.ui.fragments.customer.list.recycler.CustomersRecyclerAdapter
import com.example.seamstress.viewmodel.SeamstressViewModel


class CustomersListFragment : Fragment(), CustomerClickListener {
    companion object {
        fun newInstance(): CustomersListFragment = CustomersListFragment()
    }

    private lateinit var seamstressViewModel: SeamstressViewModel

    private var _binding: FragmentCustomersListBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomersListBinding.inflate(layoutInflater)

        seamstressViewModel = ViewModelProvider(requireActivity())[SeamstressViewModel::class.java]
        seamstressViewModel.getAllCustomers.observe(requireActivity()) { initRecycler(it) }

        binding.floatingActionButton.setOnClickListener {
            findNavController().navigate(CustomersListFragmentDirections.actionCustomersListToAddCustomer())
        }

        return binding.root
    }

    private fun initRecycler(clients: List<Customers>) {
        val clientsRecycler = binding.frCustomerListRecycler
        val customersRecyclerAdapter = CustomersRecyclerAdapter(clients, this)

        clientsRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        clientsRecycler.adapter = customersRecyclerAdapter
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onCustomerCardClick(id: Long) {
        val action =
            CustomersListFragmentDirections.actionCustomerListToCustomerCard(id)
        findNavController().navigate(action)
    }
}
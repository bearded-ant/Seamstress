package com.example.seamstress.ui.fragments.customer.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.seamstress.R
import com.example.seamstress.databinding.FragmentCustomersListBinding
import com.example.seamstress.domain.customers.Customers
import com.example.seamstress.ui.fragments.customer.list.recycler.CustomerClickListener
import com.example.seamstress.ui.fragments.customer.list.recycler.CustomersRecyclerAdapter
import com.example.seamstress.viewmodel.SeamstressViewModel


class CustomersListFragment : Fragment(), CustomerClickListener, MenuProvider {
    companion object {
        fun newInstance(): CustomersListFragment = CustomersListFragment()
    }

    private var _binding: FragmentCustomersListBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomersListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        binding.frCustomerListRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        val seamstressViewModel = ViewModelProvider(this)[SeamstressViewModel::class.java]
        seamstressViewModel.getAllCustomers.observe(viewLifecycleOwner) { initRecycler(it) }

        binding.floatingActionButton.setOnClickListener {
            findNavController().navigate(CustomersListFragmentDirections.actionCustomersListToAddCustomer())
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_customers_list, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_settings -> {
                findNavController().navigate(CustomersListFragmentDirections.actionCustomersListToSettings())
                true
            }

            else -> false
        }
    }

    private fun initRecycler(clients: List<Customers>) {
        binding.frCustomerListRecycler.adapter = CustomersRecyclerAdapter(clients, this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCustomerCardClick(id: Long) {
        val action =
            CustomersListFragmentDirections.actionCustomerListToCustomerCard(id)
        findNavController().navigate(action)
    }
}

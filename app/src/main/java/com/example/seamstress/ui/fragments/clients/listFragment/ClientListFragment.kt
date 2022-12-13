package com.example.seamstress.ui.fragments.clients.listFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.seamstress.databinding.FragmentClientsListBinding
import com.example.seamstress.domain.client.Clients
import com.example.seamstress.ui.fragments.clients.listFragment.recycler.ClientsClickListener
import com.example.seamstress.ui.fragments.clients.listFragment.recycler.ClientsRecyclerAdapter
import com.example.seamstress.viewmodel.SeamstressViewModel


class ClientListFragment : Fragment(), ClientsClickListener {
    companion object {
        fun newInstance(): ClientListFragment = ClientListFragment()
    }

    private lateinit var seamstressViewModel: SeamstressViewModel

    private var _binding: FragmentClientsListBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClientsListBinding.inflate(layoutInflater)

        seamstressViewModel = ViewModelProvider(requireActivity())[SeamstressViewModel::class.java]
        seamstressViewModel.getAllClients.observe(requireActivity()) { initRecycler(it) }

        return binding.root
    }

    private fun initRecycler(clients: List<Clients>) {
        val clientsRecycler = binding.frClientListRecycler
        val clientsRecyclerAdapter = ClientsRecyclerAdapter(clients, this)

        clientsRecycler.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        clientsRecycler.adapter = clientsRecyclerAdapter
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onClientCardClick(id: Int) {
        val action =
            ClientListFragmentDirections.actionClientListFragmentToClientFragment(id)
        findNavController().navigate(action)
    }
}
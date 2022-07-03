package com.example.seamstress.ui.clients.listFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.seamstress.R
import com.example.seamstress.databinding.FragmentClientsListBinding
import com.example.seamstress.domain.client.Client
import com.example.seamstress.domain.client.ClientSources
import com.example.seamstress.ui.clients.listFragment.recycler.ClientsClickListener
import com.example.seamstress.ui.clients.listFragment.recycler.ClientsRecyclerAdapter

class ClientListFragment : Fragment(), ClientsClickListener {
    companion object {
        fun newInstance(): ClientListFragment = ClientListFragment()
    }

    private var _binding: FragmentClientsListBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClientsListBinding.inflate(layoutInflater)

        initRecycler()

        return binding.root
    }

    private fun initRecycler() {
        val clientsRecycler = binding.frClientListRecycler
        val clientsRecyclerAdapter = ClientsRecyclerAdapter(this)

        clientsRecycler.layoutManager = LinearLayoutManager(requireContext())
        clientsRecyclerAdapter.getData(ClientSources().bindClientData())

        clientsRecycler.adapter = clientsRecyclerAdapter
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onClintCardClick(client: Client, position: Int) {
        val action = ClientListFragmentDirections.actionClientListFragmentToClientFragment(client)
        findNavController().navigate(action)
    }
}
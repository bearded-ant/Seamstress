package com.example.seamstress.ui.fragments.clients.mainFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.seamstress.R
import com.example.seamstress.databinding.FragmentClientBinding
import com.example.seamstress.domain.client.Clients
import com.example.seamstress.viewmodel.SeamstressViewModel

class ClientFragment : Fragment() {
    companion object {
        fun newInstance(): ClientFragment = ClientFragment()
    }

    private lateinit var seamstressViewModel: SeamstressViewModel

    private var _binding: FragmentClientBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentClientBinding.inflate(layoutInflater)

//todo на писать проверку на наличие аргумента?
        val safeArgs = ClientFragmentArgs.fromBundle(requireArguments())
        val clientId = safeArgs.clientEntityId

        seamstressViewModel = ViewModelProvider(this)[SeamstressViewModel::class.java]
        seamstressViewModel.selectClientById(clientId)
        seamstressViewModel.selectByIdClientLiveData.observe(requireActivity()){
            bindClientCard(it)
        }

        return binding.root

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun bindClientCard(client: Clients) {
        binding.itemClient.itemClientPhoto.setImageResource(R.drawable.ic_baseline_add_a_photo_24)
        binding.itemClient.itemClientName.text = client.name
        binding.itemClient.itemClientPhone.text = client.phone
        binding.itemClient.itemClientBalance.text = client.balance.toString()
    }
}
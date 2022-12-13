package com.example.seamstress.ui.fragments.clients.mainFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.seamstress.R
import com.example.seamstress.databinding.FragmentClientBinding

class ClientFragment : Fragment() {
    companion object {
        fun newInstance(): ClientFragment = ClientFragment()
    }

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
        val safeArgs = ClientFragmentArgs.fromBundle(arguments!!)

        val client = safeArgs.clientEntityId

//        binding.itemClient.itemClientPhoto.setImageResource(client.img)
//        binding.itemClient.itemClientName.text = client.name
//        binding.itemClient.itemClientPhone.text = client.phone
//        binding.itemClient.itemClientBalance.text = client.balance.toString()

        return binding.root

    }
}
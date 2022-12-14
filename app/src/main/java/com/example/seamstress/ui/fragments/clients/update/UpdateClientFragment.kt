package com.example.seamstress.ui.fragments.clients.update

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
import com.example.seamstress.databinding.FragmentUpdateClientBinding
import com.example.seamstress.domain.client.Clients
import com.example.seamstress.ui.fragments.clients.mainFragment.ClientFragment
import com.example.seamstress.viewmodel.SeamstressViewModel

class UpdateClientFragment : Fragment() {
    companion object {
        fun newInstance(): ClientFragment = ClientFragment()
    }

    private lateinit var seamstressViewModel: SeamstressViewModel

    private var _binding: FragmentUpdateClientBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpdateClientBinding.inflate(layoutInflater)

        seamstressViewModel = ViewModelProvider(this)[SeamstressViewModel::class.java]
        val safeArg = UpdateClientFragmentArgs.fromBundle(requireArguments())
        val clientId = safeArg.clientEntityId

        seamstressViewModel = ViewModelProvider(this)[SeamstressViewModel::class.java]
        seamstressViewModel.selectClientById(clientId)
        seamstressViewModel.selectByIdClientLiveData.observe(requireActivity()) { client ->
            bindClientCard(client)

            binding.frUpdateClientBtnSave.setOnClickListener {
                val updatedClient: Clients = Clients(
                    id = client.id,
                    name = binding.frUpdateClientName.text.toString(),
                    phone = binding.frUpdateClientPhone.text.toString(),
                    img = R.drawable.ic_baseline_add_a_photo_24,
                    balance = binding.frUpdateClientBalance.text.toString().toFloat()
                )
                seamstressViewModel.update(updatedClient)
                Toast.makeText(requireContext(), "success update", Toast.LENGTH_SHORT).show()
                findNavController().navigate(
                    UpdateClientFragmentDirections.actionUpdateClientFragmentToClientFragment(
                        clientId
                    )
                )
            }

            binding.frUpdateClientBtnDelete.setOnClickListener {


                val alertBuilder = AlertDialog.Builder(requireContext())
                alertBuilder.setPositiveButton("Да") { _, _ ->
                    seamstressViewModel.delete(client)
                    findNavController().navigate(UpdateClientFragmentDirections.actionUpdateClientFragmentToClientListFragment())
                }
                alertBuilder.setNegativeButton("Нет") { _, _ -> }
                alertBuilder.setTitle("Удалить клиента ${client.name}?")
                alertBuilder.setMessage("Вы уверены, что хотите удалить клиента ${client.name}?")
                alertBuilder.create().show()
                Toast.makeText(requireContext(), "client deleted", Toast.LENGTH_SHORT).show()
            }

            binding.frUpdateClientBtnCancel.setOnClickListener {
                findNavController().navigate(
                    UpdateClientFragmentDirections.actionUpdateClientFragmentToClientFragment(
                        clientId
                    )
                )
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun bindClientCard(client: Clients) {
        binding.frUpdateClientPhoto.setImageResource(R.drawable.ic_baseline_add_a_photo_24)
        binding.frUpdateClientName.setText(client.name)
        binding.frUpdateClientPhone.setText(client.phone)
        binding.frUpdateClientBalance.setText(client.balance.toString())
    }
}
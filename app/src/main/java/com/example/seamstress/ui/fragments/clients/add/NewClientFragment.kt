package com.example.seamstress.ui.fragments.clients.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.seamstress.R
import com.example.seamstress.databinding.FragmentNewClientBinding
import com.example.seamstress.domain.client.Clients
import com.example.seamstress.ui.fragments.clients.mainFragment.ClientFragment
import com.example.seamstress.viewmodel.SeamstressViewModel

class NewClientFragment : Fragment() {
    companion object {
        fun newInstance(): ClientFragment = ClientFragment()
    }

    private lateinit var seamstressViewModel: SeamstressViewModel

    private var _binding: FragmentNewClientBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewClientBinding.inflate(layoutInflater)

        seamstressViewModel = ViewModelProvider(this)[SeamstressViewModel::class.java]

        binding.frNewClientBtnCancel.setOnClickListener {
            findNavController().navigate(NewClientFragmentDirections.actionNewClientFragmentToClientListFragment())
        }
        binding.frNewClientBtnSave.setOnClickListener {
            val client: Clients = Clients(
                id = 0,
                name = binding.frNewClientName.text.toString(),
                phone = binding.frNewClientPhone.text.toString(),
                img = R.drawable.ic_baseline_add_a_photo_24,
                balance = binding.frNewClientBalance.text.toString().toFloat()
            )
            seamstressViewModel.insert(client)
            seamstressViewModel.newIdLiveData.observe(requireActivity()){
                Toast.makeText(requireContext(), "$it", Toast.LENGTH_LONG).show()
                findNavController().navigate(NewClientFragmentDirections.actionNewClientFragmentToClientFragment(it))
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
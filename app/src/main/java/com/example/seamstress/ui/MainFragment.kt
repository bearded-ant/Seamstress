package com.example.seamstress.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.seamstress.databinding.FragmentMainBinding

class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding
        get() = _binding!!

    companion object {
        fun newInstance(): MainFragment = MainFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(layoutInflater)
        return binding.root
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        binding.btnAddClient.setOnClickListener {
//            requireActivity().supportFragmentManager
//                .beginTransaction()
//                .replace(R.id.fragment_container, NewClientFragment.newInstance())
//                .addToBackStack("")
//                .commit()
//        }
//
//        binding.btnClients.setOnClickListener {
//            requireActivity().supportFragmentManager
//                .beginTransaction()
//                .replace(R.id.fragment_container, ClientListFragment.newInstance())
//                .addToBackStack("")
//                .commit()
//        }
//
//        binding.btnMaterials.setOnClickListener {
//            requireActivity().supportFragmentManager
//                .beginTransaction()
//                .replace(R.id.fragment_container, MaterialListFragment.newInstance())
//                .addToBackStack("")
//                .commit()
//        }
//
//        binding.btnAddMaterial.setOnClickListener {
//            requireActivity().supportFragmentManager
//                .beginTransaction()
//                .replace(R.id.fragment_container, NewMaterialFragment.newInstance())
//                .addToBackStack("")
//                .commit()
//        }
//
//        binding.btnClothes.setOnClickListener {
//            requireActivity().supportFragmentManager
//                .beginTransaction()
//                .replace(R.id.fragment_container, ClothesListFragment.newInstance())
//                .addToBackStack("")
//                .commit()
//        }
//
//        binding.btnAddClothes.setOnClickListener {
//            requireActivity().supportFragmentManager
//                .beginTransaction()
//                .replace(R.id.fragment_container, NewClothesFragment.newInstance())
//                .addToBackStack("")
//                .commit()
//        }
//
//    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
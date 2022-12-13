package com.example.seamstress.ui.fragments.notused

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.seamstress.R

class MaterialListFragment: Fragment() {
    companion object {
        fun newInstance(): MaterialListFragment = MaterialListFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_material_list, container, false)
    }
}
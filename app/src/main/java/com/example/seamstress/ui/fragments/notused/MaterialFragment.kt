package com.example.seamstress.ui.fragments.notused

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.seamstress.R

class MaterialFragment: Fragment() {
    companion object {
        fun newInstance(): MaterialFragment = MaterialFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_material, container, false)
    }
}
package com.example.seamstress.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.seamstress.R

class ClothesListFragment: Fragment() {
    companion object {
        fun newInstance(): ClothesListFragment = ClothesListFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_dress_list, container, false)
    }
}
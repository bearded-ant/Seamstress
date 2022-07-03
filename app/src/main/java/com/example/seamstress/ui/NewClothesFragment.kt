package com.example.seamstress.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.seamstress.R

class NewClothesFragment: Fragment() {
    companion object {
        fun newInstance(): NewClothesFragment = NewClothesFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //TODO собрат ьаблон новой шмотки
        return inflater.inflate(R.layout.fragment_new_client, container, false)
    }
}
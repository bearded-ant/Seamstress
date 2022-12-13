package com.example.seamstress.ui.fragments.notused

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.seamstress.R

class ReadyToWearFragment: Fragment() {
    companion object {
        fun newInstance(): ReadyToWearFragment = ReadyToWearFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_ready_to_wear, container, false)
    }
}
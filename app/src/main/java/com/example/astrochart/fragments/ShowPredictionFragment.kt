package com.example.astrochart.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.astrochart.R
import com.example.astrochart.viewmodels.MainViewModel

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ShowPredictionFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_show_prediction, container, false)
        val fragmentTextView: TextView = view.findViewById(R.id.fragment_tv)
        viewModel.geminiResponse.observe(viewLifecycleOwner) { response ->
            Log.d("ddddd","ddddd")
            response.let {
                fragmentTextView.text = it
            }
        }
        return inflater.inflate(R.layout.fragment_show_prediction, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ShowPredictionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
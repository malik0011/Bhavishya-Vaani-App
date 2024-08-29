package com.example.astrochart.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.astrochart.databinding.FragmentShowPredictionBinding
import com.example.astrochart.viewmodels.MainViewModel
import com.example.astrochart.viewmodels.PredictionDetailsViewModel

class ShowPredictionFragment : Fragment() {

    //main
    private lateinit var binding: FragmentShowPredictionBinding
    private lateinit var viewModel: PredictionDetailsViewModel

    //required
    private var userName = ""
    private var gender = ""
    private var location = ""
    private var selectedYear= ""
    private var selectedMonth= ""
    private var selectedDay = ""
    private var selectedHour = ""
    private var selectedMinute = ""
    private var selectedAPM = "am"
    private var selectedRegion = "North India"
    private var currentProfession = ""
    private var topic = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userName = it.getString("username")?: ""
            gender = it.getString("gender")?: ""
            selectedYear = it.getString("year")?: ""
            selectedMonth = it.getString("month")?: ""
            selectedDay = it.getString("day")?: ""
            selectedHour = it.getString("hour")?: ""
            selectedMinute = it.getString("min")?: ""
            selectedAPM = it.getString("apm")?: ""
            location = it.getString("placeOfBirth")?: ""
            selectedRegion = it.getString("selectedRegion")?: ""
            currentProfession = it.getString("currentProfession")?: ""
            topic = it.getString("topic")?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentShowPredictionBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this)[PredictionDetailsViewModel::class.java]
        viewModel.getGeminiResponse(
            userName = userName,
            dateOfBirth = "$selectedDay/$selectedMonth/$selectedYear",
            timeOfBirth = "$selectedHour:$selectedMinute",
            amp = selectedAPM,
            gender = gender,
            placeOfBirth = location,
            currentLocation = selectedRegion,
            currentProfession = currentProfession,
            responseTopic = topic//"Work and Growth"
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpObservers()
    }

    private fun setUpObservers() {
        viewModel.geminiPredictionResponse.observe(viewLifecycleOwner) { data ->
            showUiAndHidePBar()
            binding.apply {
                tvTitle.text = data?.title
                tvDescription.text = data?.analysis?.description
                tvFields.text =  data?.analysis?.suitableFields.listToSingleString()
                tvStrengths.text = data?.analysis?.strengths.listToSingleString()
                tvChallenges.text = data?.analysis?.challenges.listToSingleString()
                tvSolutions.text = data?.analysis?.solutions.listToSingleString()
                tvSuccessStrategies.text = data?.analysis?.successStrategies.listToSingleString()
            }
        }
    }

    private fun List<String>?.listToSingleString(): String {
        var list = ""
        this?.forEach {
            list += "$it, "
        }
        return list
    }

    private fun showUiAndHidePBar() {
        binding.apply {
            scrollView.isVisible = true
            animationView.isVisible = false
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = ShowPredictionFragment()
    }
}
package com.example.astrochart.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.astrochart.R
import com.example.astrochart.databinding.FragmentShowPredictionBinding
import com.example.astrochart.viewmodels.MainViewModel
import com.example.astrochart.viewmodels.PredictionDetailsViewModel
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
    private var language = ""

    //Ads
    private var mInterstitialAd: InterstitialAd? = null

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
            language = it.getString("language")?: "English"
        }

        syncFirebase()
        setUpAds()
        loadIndustrialAds()
    }

    private fun syncFirebase() {
        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)

        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
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
            responseLang = language,
            currentProfession = currentProfession,
            responseTopic = topic//"Work and Growth"
        )
        return binding.root
    }

    private fun showAd() {
        if (mInterstitialAd != null) {
            this.activity?.let { mInterstitialAd?.show(it) }
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.")
        }
    }

    private fun loadIndustrialAds() {
        val adRequest = AdRequest.Builder().build()

        context?.let {
            InterstitialAd.load(it,"ca-app-pub-2791337005168410/6312477779", adRequest, object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    adError.toString().let { Log.d("===ads", it) }
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d("===ads", "Ad was loaded.")
                    mInterstitialAd = interstitialAd
                }
            })
        }

        mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
            override fun onAdClicked() {
                // Called when a click is recorded for an ad.
                Log.d("===ads", "Ad was clicked.")
            }

            override fun onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                Log.d("===ads", "Ad dismissed fullscreen content.")
                mInterstitialAd = null
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                // Called when ad fails to show.
                Log.e("===ads", "Ad failed to show fullscreen content.")
                mInterstitialAd = null
            }

            override fun onAdImpression() {
                // Called when an impression is recorded for an ad.
                Log.d("===ads", "Ad recorded an impression.")
            }

            override fun onAdShowedFullScreenContent() {
                // Called when ad is shown.
                Log.d("===ads", "Ad showed fullscreen content.")
            }
        }
    }

    private fun setUpAds() {
        val backgroundScope = CoroutineScope(Dispatchers.IO)
        backgroundScope.launch {
            // Initialize the Google Mobile Ads SDK on a background thread.
            context?.let { MobileAds.initialize(it) {} }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpObservers()
    }

    private fun setUpObservers() {
        viewModel.geminiPredictionResponse.observe(viewLifecycleOwner) { data ->
            showUiAndHidePBar()
            showAd()
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
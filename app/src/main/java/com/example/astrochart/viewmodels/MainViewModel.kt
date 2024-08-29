package com.example.astrochart.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.astrochart.data.GeminiPredictionResponse
import com.example.astrochart.data.Prediction
import com.google.ai.client.generativeai.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.regex.Pattern

class MainViewModel: ViewModel() {

    private val _chartUrl = MutableLiveData<String?>()
    val chartUrl: LiveData<String?> get() = _chartUrl

    //ChatGPT livedata
    private val _chatResponse = MutableLiveData<String?>()
    val chatResponse: LiveData<String?> get() = _chatResponse

    private val _geminiPredictionResponse = MutableLiveData<GeminiPredictionResponse?>()
    val geminiPredictionResponse: LiveData<GeminiPredictionResponse?> get() = _geminiPredictionResponse

    //available predictions list
    private val _predictionsList = MutableLiveData<List<Prediction?>>()
    val predictionsList: LiveData<List<Prediction?>> get() = _predictionsList
    var selectedRegion = "North Indian"


    fun loadBirthCart(
        userName:String,
        gender:String,
        year: String,
        month: String,
        day: String,
        hour: String,
        min: String,
        apm: String,
        location: String,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val mediaType = "application/x-www-form-urlencoded".toMediaType()
                val body = "name=$userName&gender=$gender&year=$year&month=$month&day=$day&hour=$hour&min=$min&apm=$apm&location=$location&loc=1275004&location-change=1&utm_source=Birth_Chart&utm_medium=&utm_campaign=&p=1"
                    .toRequestBody(mediaType)
                val request = Request.Builder()
                    .url("https://www.prokerala.com/astrology/birth-chart/")
                    .post(body)
                    .addHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
                    .addHeader("accept-language", "en-US,en;q=0.8")
                    .addHeader("cache-control", "max-age=0")
                    .addHeader("content-type", "application/x-www-form-urlencoded")
                    .addHeader("cookie", "cab=0; UID=pB12RAFSHzcjJkYSZHUl; SID=KNEPMV; srec_en=1; ua_tracked=11; _PAdC=5")
                    .addHeader("origin", "https://www.prokerala.com")
                    .addHeader("priority", "u=0, i")
                    .addHeader("referer", "https://www.prokerala.com/astrology/birth-chart/")
                    .addHeader("sec-ch-ua", "\"Not/A)Brand\";v=\"8\", \"Chromium\";v=\"126\", \"Brave\";v=\"126\"")
                    .addHeader("sec-ch-ua-mobile", "?1")
                    .addHeader("sec-ch-ua-platform", "\"Android\"")
                    .addHeader("sec-fetch-dest", "empty")
                    .addHeader("sec-fetch-mode", "navigate")
                    .addHeader("sec-fetch-site", "same-origin")
                    .addHeader("sec-gpc", "1")
                    .addHeader("upgrade-insecure-requests", "1")
                    .addHeader("user-agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36")
                    .build()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                responseBody?.let {
                    val chartUrl = extractChartUrl(it)
                    _chartUrl.postValue(chartUrl)
                }
            } catch (e: Exception) {
                Log.e("=====", "callTheAPi: Error", e)
                _chartUrl.postValue(null)
            }
        }
    }

    private fun extractChartUrl(html: String): String? {
        val regex = """src="(https://files\.prokerala\.com/astrology/birth-chart/imgs/charts/[^"]*)""""
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(html)
        return if (matcher.find()) {
            matcher.group(1)
        } else {
            null
        }
    }

    fun updateSelectedRegion(region: String) {
        when (region) {
            "North Indian" -> {
                selectedRegion = "North Indian"
                _chartUrl.value = updateUrlSegment(_chartUrl.value?: "","nr")
            }
            "South Indian" -> {
                selectedRegion = "South Indian"
                _chartUrl.value = updateUrlSegment(_chartUrl.value?: "","sr")
            }
            "East Indian" ->  {
                selectedRegion = "East Indian"
                _chartUrl.value = updateUrlSegment(_chartUrl.value?: "","er")
            }
        }
    }

    private fun updateUrlSegment(originalUrl: String, newSegment: String): String {
        val regex = "(.*_\\d{7}_1_)[a-z]{2}(_en\\.png)".toRegex()
        return originalUrl.replace(regex, "$1$newSegment$2")
    }

    fun isDebugBuild(): Boolean {
        return BuildConfig.DEBUG
    }

    fun getPredictions() {
        val predictions = listOf(
            Prediction("Short Details", "inshort_detils"),
            Prediction("Life", "life"),
            Prediction("Study", "study"),
            Prediction("Family", "family"),
            Prediction("Job", "job"),
            Prediction("Health", "Health"),
            Prediction("Ask a Question", "custom_input"),
        )

        _predictionsList.value = predictions
    }
}
package com.example.astrochart.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.astrochart.data.GeminiPredictionResponse
import com.example.astrochart.data.Prediction
import com.google.ai.client.generativeai.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.asTextOrNull
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
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

    fun getGeminiResponse(
        userName: String,
        dateOfBirth: String,
        timeOfBirth: String,
        amp: String,
        gender: String,
        placeOfBirth: String,
        currentLocation: String,
        currentProfession: String,
        responseTopic: String
    ) {
        Log.d("======", "getGeminiResposne: api stared....")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // In Android Studio, add the following dependency to your build.gradle.kts file:
                // implementation("com.google.ai.client.generativeai:generativeai:0.7.0")
                //
                // See the Getting Started guide for more information:
                // https://ai.google.dev/gemini-api/docs/get-started/android

                // Add the following code to your Kotlin source code

                val geminiApiKey = ""

                val model = GenerativeModel(
                    "gemini-1.5-flash",
                    // Retrieve API key as an environmental variable defined in a Build Configuration
                    // see https://github.com/google/secrets-gradle-plugin for further instructions
                    geminiApiKey, //api key
                    generationConfig = generationConfig {
                        temperature = 1f
                        topK = 64
                        topP = 0.95f
                        maxOutputTokens = 8192
                        responseMimeType = "text/plain"
                    },
                    // safetySettings = Adjust safety settings
                    // See https://ai.google.dev/gemini-api/docs/safety-settings
                )

                Log.d("======", "getGeminiResponse: model created....")
                val chatHistory = listOf(
                    content("user") {
                        text("Hi")
                    },
                    content("model") {
                        text("..")
                    },
                )
                Log.d("======", "getGeminiResponse: chat history created....")
                val chat = model.startChat(chatHistory)
                Log.d("======", "getGeminiResponse: chat startted....")

                // Note that sendMessage() is a suspend function and should be called from
                // a coroutine scope or another suspend function
                val prompt = generateGeminiPromptBasedOnData(
                    userName = userName,
                    dateOfBirth = dateOfBirth,
                    timeOfBirth = timeOfBirth,
                    amp = amp,
                    gender = gender,
                    placeOfBirth = placeOfBirth,
                    currentLocation = currentLocation,
                    currentProfession = currentProfession,
                    responseTopic = responseTopic
                )
                Log.d("=====gemini", "getGeminiResponse: $prompt")
                val response = chat.sendMessage(prompt)
                Log.d("======", "getGeminiResponse: message send....")
                Log.d("======", "getGeminiResposne: respose completed....\n data: ${response.candidates.first().content.parts.first().asTextOrNull()?.trimIndent()}")


                // Get the first text part of the first candidate
                // Alternatively
                val parseData = response.candidates.first().content.parts.first().asTextOrNull()?.let { parseApiResponse(extractJson(it.trimIndent())) }

                Log.d("======gemini", "getGeminiResposne-afterclean: ${response.candidates.first().content.parts.first().asTextOrNull()
                    ?.let { extractJson(it) }}")

                parseData?.let {
                    Log.d("======gemini", "data: ${it.title}")
                }

            } catch (e: Exception) {
                Log.d("======geminiError", "getGeminiResponse: ${e.message}")
            }
        }
    }

    // Function to parse JSON string into ApiResponse object
    private fun parseApiResponse(jsonString: String): GeminiPredictionResponse? {
        val gson = Gson()
        return try {
            gson.fromJson(jsonString, GeminiPredictionResponse::class.java)
        } catch (e: JsonSyntaxException) {
            // Handle JSON parsing errors
            Log.d("======geminiSyntaxError","Error parsing JSON: ${e.message}")
            null
        }
    }

    private fun extractJson(jsonString: String): String {
        // Find the index where the JSON starts
        val jsonStartIndex = jsonString.indexOf("{")

        // Check if the JSON start index was found
        if (jsonStartIndex != -1) {
            // Extract the JSON part starting from the JSON start index
            var jsonPart = jsonString.substring(jsonStartIndex).trim()

            // Remove trailing backticks (```)
            while (jsonPart.endsWith("```")) {
                jsonPart = jsonPart.removeSuffix("```").trim()
            }

            return jsonPart
        } else {
            // Return an error message or empty string if JSON start index is not found
            return "Invalid JSON: JSON start not found"
        }
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

    fun isDebugBuild(): Boolean {
        return BuildConfig.DEBUG
    }

    private fun generateGeminiPromptBasedOnData(
        userName: String,
        dateOfBirth: String,
        timeOfBirth: String,
        amp: String,
        gender: String,
        placeOfBirth: String,
        currentLocation: String,
        currentProfession: String,
        responseTopic: String
    ): String {

        return """Generate a concise, human-friendly Vedic astrology analysis based on this specific topic/domain $responseTopic. The response should include a title and a detailed analysis. Incorporate the following user details into the analysis:
            
        User's Name: $userName
        User's Date of Birth: $dateOfBirth (day/month/year)
        User's Time of Birth: $timeOfBirth $amp
        User's Gender: $gender
        User's Birth Place: $placeOfBirth
        User's Current Residence: $currentLocation
        User's Current Profession: $currentProfession
        The analysis should focus on the domain provided, covering the following:
        
        Description: Provide a thoughtful overview of the domain with in-depth insights and focus on future potential.
        Suitable Fields: Mention areas where the individual is likely to excel.
        Strengths: Highlight key abilities that will contribute to success.
        Challenges: Identify obstacles and how to overcome them.
        Solutions: Offer actionable advice for personal and professional growth.
        Success Strategies: Provide strategic advice for long-term achievement.
        Additional Instructions:
        
        Ensure the text is concise, fitting all information within brief, impactful sentences.
        Use word play and human-centered language that feels relatable.
        Include emojis sparingly to add emphasis or positivity without overwhelming the text.
        Do not include any "next steps" section.
        The structure of the response should be flexible enough to accommodate different domains ("life," "career," "health").
        Generate the response in JSON format, with minimal but meaningful content.
        
        Example JSON Structure:
        {
          "title": "",
          "analysis": {
            "domain": {
              "description": "",
              "suitable_fields": [
                ""
              ],
              "strengths": [
                ""
              ],
              "challenges": [
                ""
              ],
              "solutions": [
                ""
              ],
              "success_strategies": [
                ""
              ]
            }
          }
        }"""
    }

    private fun getTestPrompt(): String {
        return """Generate a concise, human-friendly Vedic astrology analysis based on this specific domain "career." The response should include a title and a detailed analysis. Incorporate the following user details into the analysis:
            
        Name: Ayan Malik
        Date of Birth: 06/08/2002 (day/month/year)
        Time of Birth: 06:30 PM
        Gender: Male
        Birth Place: Kamarkundu, West Bengal, India
        Current Residence: Hooghly, West Bengal, India
        Current Profession: Software Developer (Mobile Developer)
        The analysis should focus on the domain provided, covering the following:
        
        Description: Provide a thoughtful overview of the domain with in-depth insights and focus on future potential.
        Suitable Fields: Mention areas where the individual is likely to excel.
        Strengths: Highlight key abilities that will contribute to success.
        Challenges: Identify obstacles and how to overcome them.
        Solutions: Offer actionable advice for personal and professional growth.
        Success Strategies: Provide strategic advice for long-term achievement.
        Additional Instructions:
        
        Ensure the text is concise, fitting all information within brief, impactful sentences.
        Use word play and human-centered language that feels relatable.
        Include emojis sparingly to add emphasis or positivity without overwhelming the text.
        Do not include any "next steps" section.
        The structure of the response should be flexible enough to accommodate different domains ("life," "career," "health").
        Generate the response in JSON format, with minimal but meaningful content.
        
        Example JSON Structure:
        {
          "title": "",
          "analysis": {
            "domain": {
              "description": "",
              "suitable_fields": [
                ""
              ],
              "strengths": [
                ""
              ],
              "challenges": [
                ""
              ],
              "solutions": [
                ""
              ],
              "success_strategies": [
                ""
              ]
            }
          }
        }"""
    }
}
package com.example.astrochart.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.astrochart.data.GeminiPredictionResponse
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.asTextOrNull
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PredictionDetailsViewModel : ViewModel() {

    private val _geminiPredictionResponse = MutableLiveData<GeminiPredictionResponse?>()
    val geminiPredictionResponse: LiveData<GeminiPredictionResponse?> get() = _geminiPredictionResponse


    fun getGeminiResponse(
        userName: String,
        dateOfBirth: String,
        timeOfBirth: String,
        amp: String,
        gender: String,
        placeOfBirth: String,
        currentLocation: String,
        currentProfession: String,
        responseLang: String,
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

                val geminiApiKey = "AIzaSyB1wwUa6eDhRxqr_eZJ4B_B7pAFaGkEGRs"

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
                    responseLang = responseLang,
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

                //update the live data
                _geminiPredictionResponse.postValue(parseData)

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

    private fun generateGeminiPromptBasedOnData(
        userName: String,
        dateOfBirth: String,
        timeOfBirth: String,
        amp: String,
        gender: String,
        placeOfBirth: String,
        currentLocation: String,
        currentProfession: String,
        responseLang: String = "English",
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
        Do not include any "next steps" section. Also make sure all the response data is in $responseLang.
        The structure of the response should be flexible enough to accommodate different domains ("life," "career," "health").
        Generate the response in JSON format, with minimal but meaningful content.
        
        Example JSON Structure:
        {
          "title": "",
          "analysis": {
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
        }"""
    }
}
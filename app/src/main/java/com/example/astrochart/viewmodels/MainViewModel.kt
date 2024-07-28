package com.example.astrochart.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.asTextOrNull
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.regex.Pattern

class MainViewModel: ViewModel() {

    private val _chartUrl = MutableLiveData<String?>()
    val chartUrl: LiveData<String?> get() = _chartUrl

    //ChatGPT livedata
    private val _chatResponse = MutableLiveData<String?>()
    val chatResponse: LiveData<String?> get() = _chatResponse

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
//                val body = "name=Ayan Malik&gender=male&year=2024&month=6&day=23&hour=10&min=55&apm=am&location=Kolkata, West Bengal, India&loc=1275004&location-change=1&utm_source=Birth_Chart&utm_medium=&utm_campaign=&p=1"
//                    .toRequestBody(mediaType)
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
                Log.e("AstroChartViewModel", "callTheAPi: Error", e)
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

//    fun getResponse2(question: String){
//
//        viewModelScope.launch {
//            // setting text on for question on below line.
//
//            val client = OkHttpClient()
//
//
//
//            val requestBody="""
//            {
//            "prompt": "$question",
//            "max_tokens": 100,
//            "temperature": 0
//            }
//        """.trimIndent()
//
//            val request = Request.Builder()
//                .url(url)
//                .addHeader("Content-Type", "application/json")
//                .addHeader("Authorization", "Bearer $apiKey")
//                .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
//                .build()
//
//            client.newCall(request).enqueue(object : okhttp3.Callback {
//                override fun onFailure(call: Call, e: IOException) {
//                    Log.e("error","API failed",e)
//                }
//
//                override fun onResponse(call: Call, response: Response) {
//                    val body=response.body?.string()
//                    if (body != null) {
//                        Log.v("data",body)
//                        _chatResponse.postValue(body.toString())
//                    }
//                    else{
//                        _chatResponse.postValue("empty")
//                        Log.v("data","empty")
//                    }
//                    val jsonObject= JSONObject(body)
//                    val jsonArray: JSONArray =jsonObject.getJSONArray("choices")
//                    val textResult=jsonArray.getJSONObject(0).getString("text")
////                callback(textResult)
//                }
//            })
//        }
//    }

//    fun getResponse3(question: String){
//
//        viewModelScope.launch(Dispatchers.IO) {
//            // setting text on for question on below line.
//
////            val client = OkHttpClient()
//            val loggingInterceptor = HttpLoggingInterceptor()
//            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
//            val client = OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()
//
//
//
////            val client = OkHttpClient().addInterceptor(loggingInterceptor)
//            val mediaType = "application/json".toMediaType()
//            val body = """
//                {
//                    "model": "mistralai/Mistral-7B-v0.1",
//                    "messages": [
//                        {
//                            "role": "user",
//                            "content": "$question"
//                        }
//                    ],
//                    "max_tokens": 64,
//                    "temperature": 0.5
//                }
//            """.toRequestBody(mediaType)
//            val request = Request.Builder()
//                .url("https://api.forefront.ai/v1/chat/completions")
//                .post(body)
//
//                .addHeader("content-type", "application/json")
//                .addHeader("Authorization", "Bearer $apiKey2")
//                .build()
//            val response = client.newCall(request).execute()
//
//        }
//    }

    fun getResponse(question: String) {
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                Log.d("====", "Question: $question")
//
//                val loggingInterceptor = HttpLoggingInterceptor().apply {
//                    level = HttpLoggingInterceptor.Level.BODY
//                }
//
//                val client = OkHttpClient.Builder()
//                    .addInterceptor(loggingInterceptor)
//                    .build()
//
//                val apiKey2 = "sk-icM6nUJbVqHeIScK5bppNsmvNmwtEFdO"
//                val url = "https://api.forefront.ai/v1/chat/completions"
//                Log.d("====", "Using API Key: $apiKey2")
//                Log.d("====", "API URL: $url")
//
//                val mediaType = "application/json".toMediaType()
//                val body = """
//                {
//                    "model": "mistralai/Mistral-7B-v0.1",
//                    "messages": [
//                        {
//                            "role": "user",
//                            "content": "$question"
//                        }
//                    ],
//                    "max_tokens": 64,
//                    "temperature": 0.5
//                }
//            """.trimIndent().toRequestBody(mediaType)
//
//                Log.d("====", "Request Body: $body")
//
//                val request = Request.Builder()
//                    .url(url)
//                    .post(body)
//                    .addHeader("content-type", "application/json")
//                    .addHeader("Authorization", "Bearer $apiKey2")
//                    .build()
//
//                Log.d("====", "Request: $request")
//
//                val response = client.newCall(request).execute()
//                val responseBody = response.body?.string()
//                Log.d("====", "Response: $responseBody")
//
//                // Using Gson to parse the response
//                responseBody?.let {
//                    val gson = Gson()
//                    val jsonResponse = gson.fromJson(it, JsonObject::class.java)
//                    Log.d("====", "Parsed JSON: $jsonResponse")
//
//                    val choices = jsonResponse.getAsJsonArray("choices")
//                    if (choices.size() > 0) {
//                        val messageContent = choices[0].asJsonObject
//                            .getAsJsonObject("message")
//                            .get("content")
//                            .asString
//                        Log.d("====", "Message Content: $messageContent")
//
//                        _chatResponse.postValue(messageContent)
//                    }
//                } ?: run {
//                    Log.e("====", "Response body is null")
//                }
//            } catch (e: Exception) {
//                Log.e("====", "Error during API call", e)
//            }
//        }
    }

    fun getGeminiResposne() {
        Log.d("======", "getGeminiResposne: api stared....")
        viewModelScope.launch(Dispatchers.IO) {
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

            Log.d("======", "getGeminiResposne: model created....")


            val chatHistory = listOf(
                content("user") {
                        text("Hi")
                },
                content("model") {
                        text("..")
                },
            )
            Log.d("======", "getGeminiResposne: chat history created....")



//                listOf(
//                content("user") {
//                    text("Detailed Vedic Astrology Analysis Request\n\nPersonal Information:\n\nName: Ayan Malik\nDate of Birth: 6th August 2002\nTime of Birth: 6:00 PM\nGender: Male\nBirth Place: Kamarkundu, West Bengal, India\nCurrent Residence: North India\nCurrent Prefestion: Software Developer engineer/Working from home.\nRequest for Comprehensive Astrological Analysis:\n\nUtilize your extensive knowledge of ancient Hindu Vedic astrology to provide an in-depth analysis of Ayan Malik’s astrological chart. Please cover the following aspects in detail:\n\nPersonality Analysis:\n\nProvide a thorough description of Ayan Malik's core personality traits.\nInclude insights into his strengths, weaknesses, and general disposition.\nExplain how the planetary positions influence his behavior, thinking patterns, and interactions with others.\nLove Life:\n\nOffer detailed predictions about his romantic relationships and marriage prospects.\nIndicate the potential timeframes for significant romantic events or marriage.\nDescribe the nature of his future spouse and how their relationship dynamics will be.\nProvide advice on how to cultivate successful and fulfilling romantic relationships.\nCareer:\n\nPredict his career path and professional success based on his astrological chart.\nIdentify suitable career fields or industries where he is likely to excel.\nDiscuss potential challenges he may face in his career and how to overcome them.\nProvide strategic advice for achieving long-term professional success and growth.\nEducation:\n\nAnalyze his educational prospects and intellectual capabilities.\nSuggest areas of study or fields where he might excel.\nIdentify any potential obstacles in his educational journey and how to navigate them.\nOffer guidance on how to maximize his academic success and continuous learning.\nHealth:\n\nPredict his overall health and well-being based on his chart.\nIdentify any potential health issues or vulnerabilities.\nSuggest preventive measures and lifestyle choices for maintaining good health.\nProvide advice on managing stress, mental health, and physical fitness.\nWealth and Finance:\n\nAnalyze his financial prospects and potential for wealth accumulation.\nIdentify favorable times for financial gains and potential challenges.\nOffer advice on financial planning, investments, and wealth management.\nSuggest strategies for achieving long-term financial stability and prosperity.\nImprovement and Advice:\n\nHighlight areas for personal growth and improvement.\nProvide actionable advice for overcoming weaknesses and enhancing strengths.\nOffer general life advice based on his astrological chart to help him achieve a balanced and fulfilling life.\nEnsure the analysis is written in a clear, engaging manner that captures readers' attention and provides valuable insights. Use precise astrological terminology and detailed explanations to support your predictions and advice.")
//                },
//                content("model") {
//                    text("## Ayan Malik's Vedic Astrology Analysis: Unveiling the Tapestry of Destiny\n\n**Introduction:**\n\nThis detailed Vedic astrology analysis delves into the intricate tapestry of Ayan Malik's life, weaving together the threads of his personality, love, career, education, health, wealth, and overall destiny. By meticulously examining the planetary placements in his birth chart, we can gain profound insights into his inherent potential, challenges, and opportunities for growth.\n\n**Birth Chart Analysis:**\n\n**Lagna (Ascendant):** Cancer\n\n**Moon Sign:** Virgo\n\n**Sun Sign:** Leo\n\n**Rashi (Moon Sign):** Virgo in the 10th House\n\n**Nakshatra (Lunar Constellation):** Hasta (Moon in the 3rd pada)\n\n**Planet Placement:**\n\n* **Sun:** Leo in the 9th House\n* **Moon:** Virgo in the 10th House\n* **Mars:** Leo in the 9th House\n* **Mercury:** Cancer in the 8th House\n* **Jupiter:** Gemini in the 7th House\n* **Venus:** Cancer in the 8th House\n* **Saturn:** Sagittarius in the 12th House\n* **Rahu:** Taurus in the 4th House\n* **Ketu:** Scorpio in the 10th House\n\n**Personality Analysis:**\n\nAyan is a complex individual with a sensitive heart and a sharp mind. His Cancer Ascendant grants him emotional depth, empathy, and a nurturing nature. He is likely to be deeply intuitive and possess a strong connection to his inner world. His Virgo Moon further enhances his analytical and detail-oriented nature, making him meticulous and practical in his approach to life. However, this placement can also make him prone to worry and self-criticism. His Leo Sun bestows him with natural leadership qualities, charisma, and a strong desire to be recognized and appreciated. He is likely to be creative and have a flair for the dramatic.\n\n**Strengths:**\n\n* **Empathy and compassion:** Ayan is deeply empathetic and has a natural ability to connect with others on an emotional level.\n* **Analytical and problem-solving skills:** His Virgo Moon grants him a keen eye for detail and a methodical approach to problem-solving.\n* **Leadership and charisma:** His Leo Sun gives him natural charisma and a strong desire to lead and inspire others.\n* **Creativity and artistic flair:** Ayan possesses a creative spark and an appreciation for beauty and aesthetics.\n* **Strong work ethic:** He is likely to be dedicated and hardworking, putting in the effort required to achieve his goals.\n\n**Weaknesses:**\n\n* **Overthinking and self-doubt:** Ayan's Virgo Moon can make him prone to overthinking and self-criticism.\n* **Moodiness and sensitivity:** His Cancer Ascendant can make him emotionally sensitive and prone to mood swings.\n* **Need for approval and recognition:** His Leo Sun makes him crave recognition and approval from others.\n* **Impulsiveness:** He can sometimes act impulsively without considering the consequences.\n* **Difficulty in saying \"no\":** Ayan's nurturing nature can make it difficult for him to set boundaries and say \"no\" to requests.\n\n**Love Life:**\n\nAyan is likely to be drawn to partners who are emotionally intelligent, supportive, and understanding. His 7th House Jupiter in Gemini suggests a love of intellectual stimulation and a desire for a partner who can challenge his mind. His 8th House Venus in Cancer suggests a passionate and committed nature in relationships. He may experience a strong emotional connection with his soulmate. \n\n* **Potential Timeframes for Marriage:** \n    * 2025 - 2027 (Favorable years due to Jupiter's transit over his 7th House)\n    * 2029 - 2031 (Venus's transit over his 8th House may lead to a romantic encounter)\n* **Nature of Spouse:**\n    * Ayan's spouse is likely to be intelligent, communicative, and adaptable. They may be artistic or creative in nature and possess a strong sense of empathy.\n    * They will likely have a close-knit family and value emotional intimacy.\n* **Relationship Dynamics:**\n    * Ayan's relationship will be characterized by deep emotional connection, intellectual stimulation, and a shared sense of adventure. \n    * They will have a strong sense of partnership and mutual respect.\n\n**Advice for Love:**\n\n* Embrace vulnerability and authenticity in your relationships. \n* Communicate openly and honestly with your partner.\n* Be mindful of your own emotional needs and those of your partner.\n* Seek out partners who share your intellectual curiosity and passion for life.\n\n**Career:**\n\nAyan's birth chart suggests a successful and fulfilling career in a field that allows him to utilize his analytical skills, creativity, and leadership qualities. His 10th House Moon in Virgo highlights his potential in areas that require meticulous attention to detail, problem-solving, and analytical thinking. \n\n* **Suitable Career Fields:**\n    * **Software Development:** Ayan's current profession aligns well with his astrological potential. His Virgo Moon and 8th House Mercury make him adept at technical skills and analytical thinking.\n    * **Finance:** His Virgo Moon and 10th House placement suggest a natural aptitude for managing finances and analyzing data.\n    * **Research and Development:** His analytical skills and intellectual curiosity could lead him towards a career in research and development.\n    * **Writing and Journalism:** His Virgo Moon and Gemini Jupiter give him the ability to communicate ideas clearly and creatively.\n    * **Design and Art:** Ayan's Leo Sun and Virgo Moon suggest a natural talent for creative pursuits.\n\n**Career Challenges:**\n\n* **Overthinking and perfectionism:** His Virgo Moon can make him overly critical of his work and fear making mistakes.\n* **Difficulty in delegating tasks:** Ayan's need for control can make it difficult for him to delegate tasks to others.\n* **Managing stress:** The pressure to achieve success can lead to stress and burnout.\n\n**Career Advice:**\n\n* **Embrace your strengths and learn to delegate:** Recognize your strengths and focus on tasks where you excel. Don't hesitate to delegate tasks to others when necessary.\n* **Manage stress and maintain balance:** Prioritize self-care and maintain a healthy work-life balance.\n* **Continue learning and developing new skills:** Stay up-to-date with the latest trends and technologies in your field.\n\n**Education:**\n\nAyan's intellectual curiosity and analytical skills make him a capable learner. His Virgo Moon and 10th House placement suggest a natural aptitude for academic pursuits, especially in the fields of science, technology, finance, or languages. He may also excel in fields that involve research, analysis, and problem-solving.\n\n* **Areas of Study:**\n    * **Computer Science:** Ayan's existing software development background makes this a natural choice. \n    * **Data Science and Analytics:** His analytical skills and interest in finance could lead him to this field.\n    * **Engineering:** His practical and analytical nature would lend well to various engineering disciplines.\n    * **Law:** His Virgo Moon and 10th House placement can provide the necessary detail-oriented approach to legal studies.\n\n**Educational Challenges:**\n\n* **Perfectionism:** His Virgo Moon can lead to self-doubt and anxiety over achieving perfection.\n* **Overthinking:** He may tend to overanalyze and become bogged down by details.\n* **Distractibility:** Ayan's Gemini Jupiter may make him easily distracted by new ideas and interests.\n\n**Education Advice:**\n\n* **Focus on understanding core concepts:** Don't get lost in the details. Focus on understanding the underlying principles of each subject.\n* **Practice regularly:** Repetition and consistent practice will help solidify knowledge and build confidence.\n* **Seek out mentors and guidance:** Connect with experienced individuals in your field for advice and support.\n\n**Health:**\n\nAyan's health is primarily influenced by his Cancer Ascendant and Virgo Moon. He is likely to be sensitive to changes in his environment and prone to digestive issues. He should prioritize maintaining a balanced diet, regular exercise, and sufficient rest to maintain his well-being.\n\n* **Potential Health Issues:**\n    * **Digestive problems:** Ayan should be mindful of his diet and avoid overeating or consuming processed foods.\n    * **Emotional imbalances:** His sensitive nature can make him prone to anxiety, mood swings, and sleep disturbances.\n    * **Physical ailments:** He may experience health issues related to his stomach, chest, or respiratory system.\n\n**Health Advice:**\n\n* **Maintain a balanced diet:** Emphasize whole foods, fruits, vegetables, and avoid excessive sugar and processed foods.\n* **Regular exercise:** Engaging in physical activity can help manage stress, improve mood, and maintain physical health.\n* **Practice relaxation techniques:** Meditation, yoga, or deep breathing exercises can help manage stress and promote emotional well-being.\n\n**Wealth and Finance:**\n\nAyan's financial prospects are influenced by his 10th House Moon in Virgo and 7th House Jupiter in Gemini. He is likely to be a meticulous and resourceful manager of his finances, with a natural aptitude for investments and financial planning. However, his tendency towards overthinking and impulsiveness can lead to financial missteps.\n\n* **Favorable Timeframes for Financial Gains:**\n    * 2025 - 2027 (Jupiter's transit over the 7th House can bring financial growth through partnerships or ventures).\n    * 2029 - 2031 (Venus's transit over the 8th House can bring sudden wealth or financial gains through inheritance or unexpected sources).\n\n**Financial Advice:**\n\n* **Plan for the long term:** Develop a sound financial plan that outlines goals, investments, and savings strategies.\n* **Avoid impulsive spending:** Before making major purchases, take time to consider the financial implications.\n* **Seek expert advice:** Consult a financial advisor for personalized guidance on investments and wealth management.\n\n**Improvement and Advice:**\n\n* **Embrace your sensitivity:** Don't shy away from your emotional nature. Embrace your empathy and use it to connect with others.\n* **Manage your overthinking:** Practice mindfulness techniques to help quiet your thoughts and focus on the present moment.\n* **Set boundaries:** Learn to say \"no\" to protect your time and energy.\n* **Develop your leadership skills:** Use your charisma and creativity to inspire others and achieve your goals.\n* **Maintain a balanced lifestyle:** Prioritize your physical and mental well-being by engaging in regular exercise, healthy eating, and relaxation techniques.\n\n**Conclusion:**\n\nAyan Malik's birth chart reveals a complex and multifaceted individual with a rich tapestry of strengths, weaknesses, and opportunities for growth. By understanding the influence of the planetary placements on his personality, love, career, education, health, and wealth, Ayan can navigate life's journey with greater awareness and clarity. By embracing his unique qualities and utilizing his inherent potential, he can achieve personal fulfillment, professional success, and a life filled with love and purpose. This detailed astrological analysis serves as a guidepost, illuminating his path towards a brighter and more fulfilling future. \n")
//                },
//            )

            val chat = model.startChat(chatHistory)
            Log.d("======", "getGeminiResposne: chat startted....")


            Log.d("======", "getGeminiResposne: message send....")

            // Note that sendMessage() is a suspend function and should be called from
            // a coroutine scope or another suspend function
            val response = chat.sendMessage("How are you ? tell me good joke in 100 words for a techie")
            Log.d("======", "getGeminiResposne: respose completed....")


            // Get the first text part of the first candidate
            Log.d("======", "getGeminiResposne: $response.text")
            // Alternatively
            Log.d("======", "data: ${response.candidates.first().content.parts.first().asTextOrNull()}")
        }
    }
}
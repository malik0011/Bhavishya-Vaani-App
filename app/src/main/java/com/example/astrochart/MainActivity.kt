package com.example.astrochart

import android.app.DatePickerDialog
import android.app.DownloadManager
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.astrochart.adapters.HorizontalPredictionListAdapter
import com.example.astrochart.adapters.RegionSelectionAdapter
import com.example.astrochart.databinding.ActivityMainBinding
import com.example.astrochart.fragments.ShowPredictionFragment
import com.example.astrochart.viewmodels.MainViewModel
import com.google.ai.client.generativeai.BuildConfig
import java.io.File
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    private var userName = ""
    private var gender = ""
    private var location = ""
    private var selectedYear: Int = 0
    private var selectedMonth: Int = 0
    private var selectedDay: Int = 0
    private var selectedHour: Int = 0
    private var selectedMinute: Int = 0
    private var selectedAPM: String = "am"
    private var url:String=""
    private var regionAdapter: RegionSelectionAdapter? = null
    private var predictionAdapter: HorizontalPredictionListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        initializeUi()

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        setUpObserver()
        setUpListener()
        feedDataForDebugTest()

    }

    private fun setUpObserver() {
        viewModel.chartUrl.observe(this) { chartUrl ->
            chartUrl?.let {
                loadImage(it)
                setUpPredictionsListView()
                //if already assigned then just skip
                if (regionAdapter == null) setUpRegionList()
                url = it //updating url for download purpose
                Log.d("url",url)
            }
        }

        viewModel.geminiPredictionResponse.observe(this) { response ->
            binding.pBar.isVisible = false
            Log.d("======geminiPredictionResponse", "setUpObserver: ${response?.title}")
        }

        viewModel.chatResponse.observe(this) {
            Log.d("=====", "setUpObserver: ${it.toString()}")
        }

        viewModel.predictionsList.observe(this) {
            Log.d("=====", "setUpObserver: ${it.size}, $predictionAdapter")
            predictionAdapter?.submitList(it)
        }
    }

    private fun setUpPredictionsListView() {
        binding.rcvPredictions.visibility = View.VISIBLE
        setUpPredictionList()
    }

    private fun setUpRegionList() {
        binding.rcvRegions.isVisible = true
        val regions = listOf("North Indian", "South Indian", "East Indian")
        regionAdapter = RegionSelectionAdapter(regions) {position ->
            binding.pBar.isVisible = true
            //updating the selected region in vm
            viewModel.updateSelectedRegion(regions[position])
        }
        binding.rcvRegions.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rcvRegions.adapter = regionAdapter

    }

    private fun initializeUi() {
        binding.ui1.isVisible = false
        binding.ui2.isVisible = true
    }

    private fun setUpListener() {
        binding.apply {
            buttonSubmit.setOnClickListener {
                // Get user input
                userName = binding.editTextName.text.toString().trim()
                gender = binding.spinnerGender.selectedItem.toString()
                location = binding.editTextLocation.text.toString().trim()

                Log.d("======", "data: name: $userName, g: $gender, y: $selectedYear, m: $selectedMonth\n\n" +
                        "d: $selectedDay, h: $selectedHour, m: $selectedMinute, apm: $selectedAPM\n\n" +
                        "l: $location")

                // Validate input (example: check if fields are not empty)
                if (userName.isNotEmpty() && location.isNotEmpty()) {
                    binding.ui1.isVisible = true
                    binding.ui2.isVisible = false
                    // TODO: Handle the data (e.g., pass it to ViewModel or process it further)
                    binding.pBar.isVisible = true
                    Log.d("=====", "setUpListener: 12345678")
                    viewModel.loadBirthCart(
                        userName = userName.lowercase().trim(),
                        gender = gender.lowercase().trim(),
                        year = selectedYear.toString().trim(),  // Example hardcoded values
                        month = selectedMonth.toString().trim(),
                        day = selectedDay.toString().trim(),
                        hour = selectedHour.toString().trim(),
                        min = selectedMinute.toString().trim(),
                        apm = selectedAPM.trim(),
                        location = location.trim()
                    )
                } else {
                    Toast.makeText(this@MainActivity, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            }

            binding.btn.setOnClickListener{
                binding.pBar.isVisible = true
                getKnowMoreDetails()

//                viewModel.loadBirthCart(
//                    userName = "Ayan Malik",
//                    gender = "male",
//                    year = "2002",
//                    month = "08",
//                    day = "06",
//                    hour = "12",
//                    min = "30",
//                    apm = "pm",
//                    location = "kamarkundu, West Bengal, India",
//                )
            }

            buttonSelectDate.setOnClickListener {
                showDatePickerDialog()
            }

            buttonSelectTime.setOnClickListener {
                showTimePickerDialog()
            }
            btnDownload.setOnClickListener {
                //download and save the image in download folder
                downloadImage("download chart",url)
            }
        }
    }

    private fun getKnowMoreDetails() {
        val userName = userName.lowercase().trim()
        val gender = gender.lowercase().trim()
        val year = selectedYear.toString().trim()
        val month = selectedMonth.toString().trim()
        val day = selectedDay.toString().trim()
        val hour = selectedHour.toString().trim()
        val min = selectedMinute.toString().trim()
        val apm = selectedAPM.trim()
        val placeOfBirth = location.trim()
        val selectedRegion = viewModel.selectedRegion
        val currentProfession = "Student"

        val Question = """
                    Detailed Vedic Astrology Analysis Request

                    Personal Information:
                    Name: $userName
                    Date of Birth: $day/$month/$year (day/month/year)
                    Time of Birth: $hour:$min $apm
                    Gender: $gender
                    Birth Place: $placeOfBirth
                    Current Residence: $selectedRegion
                    Current Profession: $currentProfession
                    Request for Comprehensive Astrological Analysis:

                    Utilize your extensive knowledge of ancient Hindu Vedic astrology to provide an in-depth analysis of $userName’s astrological chart. Please cover the following aspects in detail:

                    Personality Analysis:

                    Provide a thorough description of $userName's core personality traits.
                    Include insights into his strengths, weaknesses, and general disposition.
                    Explain how the planetary positions influence his behavior, thinking patterns, and interactions with others.
                    Love Life:

                    Offer detailed predictions about his romantic relationships and marriage prospects.
                    Indicate the potential timeframes for significant romantic events or marriage.
                    Describe the nature of his future spouse and how their relationship dynamics will be.
                    Provide advice on how to cultivate successful and fulfilling romantic relationships.
                    Career:

                    Predict his career path and professional success based on his astrological chart.
                    Identify suitable career fields or industries where he is likely to excel.
                    Discuss potential challenges he may face in his career and how to overcome them.
                    Provide strategic advice for achieving long-term professional success and growth.
                    Education:

                    Analyze his educational prospects and intellectual capabilities.
                    Suggest areas of study or fields where he might excel.
                    Identify any potential obstacles in his educational journey and how to navigate them.
                    Offer guidance on how to maximize his academic success and continuous learning.
                    Health:

                    Predict his overall health and well-being based on his chart.
                    Identify any potential health issues or vulnerabilities.
                    Suggest preventive measures and lifestyle choices for maintaining good health.
                    Provide advice on managing stress, mental health, and physical fitness.
                    Wealth and Finance:

                    Analyze his financial prospects and potential for wealth accumulation.
                    Identify favorable times for financial gains and potential challenges.
                    Offer advice on financial planning, investments, and wealth management.
                    Suggest strategies for achieving long-term financial stability and prosperity.
                    Improvement and Advice:

                    Highlight areas for personal growth and improvement.
                    Provide actionable advice for overcoming weaknesses and enhancing strengths.
                    Offer general life advice based on his astrological chart to help him achieve a balanced and fulfilling life.
                    Ensure the analysis is written in a clear, engaging manner that captures readers' attention and provides valuable insights. Use precise astrological terminology and detailed explanations to support your predictions and advice.
                """.trimIndent()
        //viewModel.getResponse(question = Question)

        viewModel.getGeminiResponse(
            userName = userName,
            dateOfBirth = "$day/$month/$year",
            timeOfBirth = "$hour:$min",
            amp = apm,
            gender = gender,
            placeOfBirth = placeOfBirth,
            currentLocation = selectedRegion,
            currentProfession = currentProfession,
            responseTopic = "Work and Growth"
        )
    }

    private fun downloadImage(filename: String, url: String) {
        try {
            var downloadmanager: DownloadManager?=null
            downloadmanager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            val downloaduri = Uri.parse(url)
            val request= DownloadManager.Request(downloaduri)
            request.setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE
            )
                .setAllowedOverRoaming(false)
                .setTitle(filename)
                .setMimeType("image/jpeg")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, File.separator + filename+".jpg")
            downloadmanager.enqueue(request)
            Toast.makeText(this,"Downloaded successfully",Toast.LENGTH_SHORT).show()
        }
        catch (e:Exception){
            Toast.makeText(this,"Image download failed !",Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDayOfMonth ->
            this.selectedYear = selectedYear
            this.selectedMonth = selectedMonth + 1
            this.selectedDay = selectedDayOfMonth
            binding.textViewBirthDate.text = "Birth Date : $selectedDayOfMonth/${selectedMonth + 1}/$selectedYear"
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val formattedHour = if (selectedHour == 0 || selectedHour == 12) 12 else selectedHour % 12
            selectedAPM = if (selectedHour < 12) "am" else "pm"
            this.selectedHour = formattedHour
            this.selectedMinute = selectedMinute
            binding.textViewTime.text = "Birth Time : " + String.format("%02d:%02d %s", formattedHour, selectedMinute, selectedAPM)
        }, hour, minute, false)

        timePickerDialog.show()
    }

    private fun loadImage(url: String) {
        Glide.with(this).load(url).into(binding.chartImageView)
        binding.pBar.isVisible = false
    }

    private fun setUpPredictionList() {
        predictionAdapter = HorizontalPredictionListAdapter {
            Log.d("=====", "setUpPredictionList: ItemClicked: $it")
            binding.pBar.isVisible = true
            getKnowMoreDetails()
        }
        binding.rcvPredictions.apply {
            layoutManager = GridLayoutManager(context, 2) //LinearLayoutManager(baseContext, RecyclerView.HORIZONTAL, false)
            adapter = predictionAdapter
        }
        viewModel.getPredictions()
    }

    private fun feedDataForDebugTest() {
        Log.d("=======", "BuildConfig.DEBUG = ${BuildConfig.DEBUG}")
        Log.d("=======", "Build Type: ${BuildConfig.BUILD_TYPE}")
        Log.d("=======", "Build version: ${applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0).versionName}")
            binding.buttonSubmit.setOnClickListener {
                Toast.makeText(this, "Using Debug data", Toast.LENGTH_SHORT).show()
                binding.ui1.isVisible = true
                binding.ui2.isVisible = false
                binding.pBar.isVisible = true
                viewModel.loadBirthCart(
                    userName = "Ayan Malik",
                    gender = "male",
                    year = "2002",
                    month = "08",
                    day = "06",
                    hour = "12",
                    min = "30",
                    apm = "pm",
                    location = "kamarkundu, West Bengal, India",
                )
        }
    }

    private fun openFragment() {
        val fragment = ShowPredictionFragment.newInstance("", "") // Create an instance of your fragment

        // Get the FragmentManager and begin a transaction
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment) // Replace the current fragment
            .addToBackStack(null) // Optional: add this transaction to the back stack
            .commit() // Commit the transaction
    }
}
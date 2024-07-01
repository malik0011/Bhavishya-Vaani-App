package com.example.astrochart

import android.app.DatePickerDialog
import android.app.DownloadManager
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.astrochart.databinding.ActivityMainBinding
import com.example.astrochart.viewmodels.MainViewModel
import com.example.astrochart.viewmodels.adapters.RegionSelectionAdapter
import java.io.File
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    private var selectedYear: Int = 0
    private var selectedMonth: Int = 0
    private var selectedDay: Int = 0
    private var selectedHour: Int = 0
    private var selectedMinute: Int = 0
    private var selectedAPM: String = "am"
    private var url:String=""
    private var regionAdapter: RegionSelectionAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        initializeUi()

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        viewModel.chartUrl.observe(this) { chartUrl ->
            chartUrl?.let {
                loadImage(it)
                //if already assigned then just skip
                if (regionAdapter == null) setUpRegionList()
                url = it //updating url for download purpose
            }
        }
        setUpListener()
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
                val userName = binding.editTextName.text.toString().trim()
                val gender = binding.spinnerGender.selectedItem.toString()
                val location = binding.editTextLocation.text.toString().trim()

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
            binding.textViewBirthDate.text = "$selectedDayOfMonth/${selectedMonth + 1}/$selectedYear"
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
            binding.textViewTime.text = String.format("%02d:%02d %s", formattedHour, selectedMinute, selectedAPM)
        }, hour, minute, false)

        timePickerDialog.show()
    }

    private fun loadImage(url: String) {
        Glide.with(this).load(url).into(binding.chartImageView)
        binding.pBar.isVisible = false
    }
}
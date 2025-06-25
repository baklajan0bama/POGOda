package com.example.myapplication1

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private lateinit var findButton: Button
    private lateinit var cityTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var overallTempTextView: TextView
    private lateinit var morningTempTextView: TextView
    private lateinit var dayTempTextView: TextView
    private lateinit var eveningTempTextView: TextView
    private lateinit var nightTempTextView: TextView
    private lateinit var tomorrowButton: Button
    private lateinit var weekButton: Button

    private var currentLatitude: Double = 57.7685
    private var currentLongitude: Double = 40.9270

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()

        cityTextView.text = "Кострома"
        fetchInitialWeather()

        setupClickListeners()
    }

    private fun initViews() {
        searchEditText = findViewById(R.id.editTextCity)
        findButton = findViewById(R.id.buttonSearch)
        cityTextView = findViewById(R.id.textViewCityName)
        dateTextView = findViewById(R.id.textViewDate)
        overallTempTextView = findViewById(R.id.textViewCurrentTemperature)
        morningTempTextView = findViewById(R.id.textViewMorningTemp)
        dayTempTextView = findViewById(R.id.textViewDayTemp)
        eveningTempTextView = findViewById(R.id.textViewEveningTemp)
        nightTempTextView = findViewById(R.id.textViewNightTemp)
        tomorrowButton = findViewById(R.id.buttonTomorrow)
        weekButton = findViewById(R.id.buttonWeek)
    }

    private fun setupClickListeners() {
        findButton.setOnClickListener {
            val city = searchEditText.text.toString().trim()
            if (city.isNotBlank()) {
                fetchCoordinatesAndWeather(city)
            } else {
                Toast.makeText(this, "Введите название города", Toast.LENGTH_SHORT).show()
            }
        }

        tomorrowButton.setOnClickListener {
            val intent = Intent(this, TomorrowActivity::class.java).apply {
                putExtra("city", cityTextView.text.toString())
                putExtra("latitude", currentLatitude)
                putExtra("longitude", currentLongitude)
            }
            startActivity(intent)
        }

        weekButton.setOnClickListener {
            val intent = Intent(this, WeekActivity::class.java).apply {
                putExtra("city", cityTextView.text.toString())
                putExtra("latitude", currentLatitude)
                putExtra("longitude", currentLongitude)
            }
            startActivity(intent)
        }
    }

    private fun fetchInitialWeather() {
        val url = "https://api.open-meteo.com/v1/forecast?latitude=$currentLatitude&longitude=$currentLongitude&hourly=temperature_2m&current=temperature_2m&timezone=GMT&forecast_days=1"

        lifecycleScope.launch {
            val response = withContext(Dispatchers.IO) {
                try {
                    URL(url).readText()
                } catch (e: Exception) {
                    null
                }
            }

            processWeatherResponse(response)
        }
    }

    private fun fetchCoordinatesAndWeather(city: String) {
        val geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name=$city"

        lifecycleScope.launch {
            val response = withContext(Dispatchers.IO) {
                try {
                    URL(geoUrl).readText()
                } catch (e: Exception) {
                    null
                }
            }

            if (response != null) {
                try {
                    val jsonObj = JSONObject(response)
                    val results = jsonObj.optJSONArray("results")
                    if (results != null && results.length() > 0) {
                        val location = results.getJSONObject(0)
                        currentLatitude = location.getDouble("latitude")
                        currentLongitude = location.getDouble("longitude")
                        cityTextView.text = city
                        fetchWeather()
                    } else {
                        showToast("Город не найден")
                    }
                } catch (e: Exception) {
                    showToast("Ошибка данных")
                }
            } else {
                showToast("Ошибка сети")
            }
        }
    }

    private fun fetchWeather() {
        val url = "https://api.open-meteo.com/v1/forecast?latitude=$currentLatitude&longitude=$currentLongitude&hourly=temperature_2m&current=temperature_2m&timezone=GMT&forecast_days=1"

        lifecycleScope.launch {
            val response = withContext(Dispatchers.IO) {
                try {
                    URL(url).readText()
                } catch (e: Exception) {
                    null
                }
            }

            processWeatherResponse(response)
        }
    }

    private fun processWeatherResponse(response: String?) {
        if (response != null) {
            try {
                val jsonObj = JSONObject(response)

                val currentTemp = jsonObj.getJSONObject("current").getDouble("temperature_2m")
                overallTempTextView.text = "${currentTemp.toInt()}°C"

                dateTextView.text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())

                val hourly = jsonObj.getJSONObject("hourly")
                val temps = hourly.getJSONArray("temperature_2m")

                val morningTemp = (0..5).map { temps.getDouble(it) }.average()
                val dayTemp = (6..11).map { temps.getDouble(it) }.average()
                val eveningTemp = (12..17).map { temps.getDouble(it) }.average()
                val nightTemp = (18..23).map { temps.getDouble(it) }.average()

                morningTempTextView.text = "Утро: ${morningTemp.toInt()}°C"
                dayTempTextView.text = "День: ${dayTemp.toInt()}°C"
                eveningTempTextView.text = "Вечер: ${eveningTemp.toInt()}°C"
                nightTempTextView.text = "Ночь: ${nightTemp.toInt()}°C"

            } catch (e: Exception) {
                showToast("Ошибка данных")
            }
        } else {
            showToast("Ошибка сети")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

package com.example.myapplication1

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import org.json.JSONArray

class TomorrowActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tomorrow)

        val city = intent.getStringExtra("city") ?: ""
        val latitude = intent.getDoubleExtra("latitude", 57.7685)
        val longitude = intent.getDoubleExtra("longitude", 40.9270)

        val cityTextView: TextView = findViewById(R.id.tomorrowCityTextView)
        val dateTextView: TextView = findViewById(R.id.tomorrowDateTextView)
        val tempTextView: TextView = findViewById(R.id.tomorrowTempTextView)
        val morningTemp: TextView = findViewById(R.id.tomorrowMorningTemp)
        val dayTemp: TextView = findViewById(R.id.tomorrowDayTemp)
        val eveningTemp: TextView = findViewById(R.id.tomorrowEveningTemp)
        val nightTemp: TextView = findViewById(R.id.tomorrowNightTemp)
        val backButton: Button = findViewById(R.id.tomorrowBackButton)

        cityTextView.text = city
        
        val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val tomorrowDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(calendar.time)
        dateTextView.text = tomorrowDate

        fetchTomorrowWeather(latitude, longitude)

        backButton.setOnClickListener {
            finish() 
        }
    }

    private fun fetchTomorrowWeather(latitude: Double, longitude: Double) {
        val calendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val tomorrowDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        val url = "https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude&hourly=temperature_2m&start_date=$tomorrowDate&end_date=$tomorrowDate"

        val tempTextView: TextView = findViewById(R.id.tomorrowTempTextView)
        val morningTemp: TextView = findViewById(R.id.tomorrowMorningTemp)
        val dayTemp: TextView = findViewById(R.id.tomorrowDayTemp)
        val eveningTemp: TextView = findViewById(R.id.tomorrowEveningTemp)
        val nightTemp: TextView = findViewById(R.id.tomorrowNightTemp)

        lifecycleScope.launch {
            val response = withContext(Dispatchers.IO) {
                try {
                    URL(url).readText()
                } catch (e: Exception) {
                    null
                }
            }

            if (response != null) {
                try {
                    val jsonObj = JSONObject(response)
                    val hourly = jsonObj.getJSONObject("hourly")
                    val temps = hourly.getJSONArray("temperature_2m")
                    
                    val morningHours = 6..11
                    val dayHours = 12..17
                    val eveningHours = 18..23
                    val nightHours = 0..5
                    
                    val morningAvg = calculateAverageTemp(temps, morningHours)
                    val dayAvg = calculateAverageTemp(temps, dayHours)
                    val eveningAvg = calculateAverageTemp(temps, eveningHours)
                    val nightAvg = calculateAverageTemp(temps, nightHours)
                    
                    val maxTemp = listOf(morningAvg, dayAvg, eveningAvg, nightAvg).maxOrNull() ?: 0.0
                    
                    runOnUiThread {
                        tempTextView.text = "${maxTemp.toInt()}°C"
                        morningTemp.text = "Утро: ${morningAvg.toInt()}°C"
                        dayTemp.text = "День: ${dayAvg.toInt()}°C"
                        eveningTemp.text = "Вечер: ${eveningAvg.toInt()}°C"
                        nightTemp.text = "Ночь: ${nightAvg.toInt()}°C"
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        tempTextView.text = "--°C"
                        morningTemp.text = "Утро: --°C"
                        dayTemp.text = "День: --°C"
                        eveningTemp.text = "Вечер: --°C"
                        nightTemp.text = "Ночь: --°C"
                    }
                }
            } else {
                runOnUiThread {
                    tempTextView.text = "Ошибка"
                }
            }
        }
    }

    private fun calculateAverageTemp(temps: JSONArray, hours: IntRange): Double {
        return hours.map { temps.getDouble(it) }.average()
    }
}

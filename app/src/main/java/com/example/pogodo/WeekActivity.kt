package com.example.myapplication1

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import android.widget.Toast
import java.util.*

class WeekActivity : AppCompatActivity() {

    private val russianDays = arrayOf(
        "Воскресенье", "Понедельник", "Вторник", "Среда",
        "Четверг", "Пятница", "Суббота"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_week)

        val city = intent.getStringExtra("city") ?: ""
        val latitude = intent.getDoubleExtra("latitude", 57.7685)
        val longitude = intent.getDoubleExtra("longitude", 40.9270)

        val cityTextView: TextView = findViewById(R.id.weekCityTextView)
        val backButton: Button = findViewById(R.id.weekBackButton)

        cityTextView.text = "Прогноз на неделю для $city"
        backButton.setOnClickListener { finish() }

        fetchWeekWeather(latitude, longitude)
    }

    private fun fetchWeekWeather(latitude: Double, longitude: Double) {
        val url = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=$latitude&longitude=$longitude" +
                "&daily=temperature_2m_max,temperature_2m_min" +
                "&timezone=auto&forecast_days=7"

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) { URL(url).readText() }
                parseWeekWeatherData(response)
            } catch (e: Exception) {
                showError()
            }
        }
    }

    private fun parseWeekWeatherData(response: String) {
        val jsonObj = JSONObject(response)
        val daily = jsonObj.getJSONObject("daily")
        val dates = daily.getJSONArray("time")
        val maxTemps = daily.getJSONArray("temperature_2m_max")
        val minTemps = daily.getJSONArray("temperature_2m_min")

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val container = findViewById<LinearLayout>(R.id.weekForecastContainer)

        runOnUiThread {
            container.removeAllViews()

            for (i in 0 until dates.length()) {
                val date = sdf.parse(dates.getString(i))
                val calendar = Calendar.getInstance().apply { time = date!! }
                val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                val dayName = russianDays[dayOfWeek - 1]
                val dateStr = SimpleDateFormat("dd.MM", Locale.getDefault()).format(date)

                val maxTemp = maxTemps.getDouble(i).toInt()
                val minTemp = minTemps.getDouble(i).toInt()

                addDayForecastItem(container, "$dayName, $dateStr", minTemp, maxTemp)
            }
        }
    }

    private fun addDayForecastItem(container: LinearLayout, dayName: String, minTemp: Int, maxTemp: Int) {
        val itemView = LayoutInflater.from(this).inflate(R.layout.item_day_forecast, container, false)

        itemView.findViewById<TextView>(R.id.dayNameTextView).text = dayName
        itemView.findViewById<TextView>(R.id.minTempTextView).text = "Мин: $minTemp°C"
        itemView.findViewById<TextView>(R.id.maxTempTextView).text = "Макс: $maxTemp°C"

        container.addView(itemView)
    }

    private fun showError() {
        runOnUiThread {
            Toast.makeText(this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show()
        }
    }
}
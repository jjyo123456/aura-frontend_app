package com.example.bharatiya_antariksh_hackatho_app

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import com.example.bharatiya_antariksh_hackatho_app.forecast_aqi_display.ForecastAQIApiResponse
import com.example.bharatiya_antariksh_hackatho_app.forecast_aqi_display.ForecastAQIRepository
import com.example.bharatiya_antariksh_hackatho_app.historical_aqi.HistoricalAQIActivity
import com.example.bharatiya_antariksh_hackatho_app.realtime_aqi_display.AQIViewModel
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.roundToInt
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: AQIViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var pm10: TextView
    private lateinit var pm25: TextView
    private lateinit var co: TextView
    private lateinit var no2: TextView
    private lateinit var so2: TextView
    private lateinit var o3: TextView
    private lateinit var loading: TextView
    private lateinit var aiExplanation: TextView

    private lateinit var cardFront: CardView
    private lateinit var cardBack: CardView
    private lateinit var aqiValueText: TextView
    private lateinit var aqiLevelText: TextView
    private lateinit var aqiAdviceText: TextView

    private var latitude by Delegates.notNull<Double>()
    private var longitude by Delegates.notNull<Double>()

    private var isFlipped = false
    private var aqiValue = 170

    private var apiKey: String = "AIzaSyDJW69wH1BqmlnSu7XoK9Avhp5v8q_PuE4"

    private lateinit var button: Button
    private var globalAqi by Delegates.notNull<Double>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI elements
        button = findViewById(R.id.maps_button)
        pm10 = findViewById(R.id.pm10Text)
        pm25 = findViewById(R.id.pm25Text)
        co = findViewById(R.id.coText)
        no2 = findViewById(R.id.no2Text)
        so2 = findViewById(R.id.so2Text)
        o3 = findViewById(R.id.o3Text)
        loading = findViewById(R.id.loadingText)
        aiExplanation = findViewById(R.id.ai_explanation)

        cardFront = findViewById(R.id.cardFront)
        cardBack = findViewById(R.id.cardBack)
        aqiValueText = findViewById(R.id.aqiValueText)
        aqiLevelText = findViewById(R.id.aqiLevelText)
        aqiAdviceText = findViewById(R.id.aqiAdviceText)

        viewModel = ViewModelProvider(this)[AQIViewModel::class.java]
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        supportActionBar?.hide()
        setupObservers()
        requestLocationPermission()
        requestNotificationPermission()
        populateSampleForecast()

        button.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)

            intent.putExtra("latitude", latitude)
            intent.putExtra("longitude", longitude)
            startActivity(intent)
        }

        cardFront.setOnClickListener { flipCard(cardFront, cardBack) }
        cardBack.setOnClickListener { flipCard(cardFront, cardBack) }
    }

    private fun flipCard(cardFront: View, cardBack: View) {
        if (cardFront.animation != null || cardBack.animation != null) return

        val outView: View
        val inView: View

        if (isFlipped) {
            outView = cardBack
            inView = cardFront
        } else {
            outView = cardFront
            inView = cardBack
        }

        outView.pivotX = outView.width / 2f
        outView.pivotY = outView.height / 2f
        inView.pivotX = inView.width / 2f
        inView.pivotY = inView.height / 2f

        inView.visibility = View.VISIBLE
        inView.alpha = 0f
        inView.rotationY = -90f

        val scale = cardFront.context.resources.displayMetrics.density
        cardFront.cameraDistance = 8000 * scale
        cardBack.cameraDistance = 8000 * scale

        val outAnimator = ObjectAnimator.ofFloat(outView, "rotationY", 0f, 90f)
        val inAnimator = ObjectAnimator.ofFloat(inView, "rotationY", -90f, 0f)

        outAnimator.duration = 300
        inAnimator.duration = 300

        outAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                outView.alpha = 0f
                outView.visibility = View.INVISIBLE
                inView.alpha = 1f
                inAnimator.start()
            }
        })

        outAnimator.start()
        isFlipped = !isFlipped
    }

    @SuppressLint("SetTextI18n")
    private fun fetchAiExplanation() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                loading.text = "Fetching AI explanation..."
                val generativeModel = GenerativeModel(
                    modelName = "gemini-1.5-flash",
                    apiKey = apiKey
                )
                val response = generativeModel.generateContent(
                    content { text("This is the AQI $globalAqi of the current location. Provide a health advisory in a short paragraph of 4 lines, formatted as: 'Air quality is [level]. [Suggestions]'") }
                )
                aqiAdviceText.text = response.text ?: "No response received"
                loading.text = ""
            } catch (e: Exception) {
                loading.text = "Error fetching AI explanation: ${e.message}"
                aiExplanation.text = ""
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupObservers() {
        viewModel.aqiData.observe(this) { data ->
            if (data != null) {
                fun latestNonNull(list: List<Number?>): Number? {
                    return list.reversed().firstOrNull { it != null }
                }

                val pm10Value = latestNonNull(data.hourly.pm10)
                val pm25Value = latestNonNull(data.hourly.pm2_5)
                val coValue = latestNonNull(data.hourly.carbon_monoxide)
                val no2Value = latestNonNull(data.hourly.nitrogen_dioxide)
                val so2Value = latestNonNull(data.hourly.sulphur_dioxide)
                val o3Value = latestNonNull(data.hourly.ozone)

                pm10.text = pm10Value?.toString() ?: "N/A"
                pm25.text = pm25Value?.toString() ?: "N/A"
                co.text = coValue?.toString() ?: "N/A"
                no2.text = no2Value?.toString() ?: "N/A"
                so2.text = so2Value?.toString() ?: "N/A"
                o3.text = o3Value?.toString() ?: "N/A"

                if (pm25Value != null) {
                    val newAqi = calculateAQI(pm25Value.toDouble())
                    globalAqi = newAqi.toDouble()
                    aqiValue = newAqi

                    val (_, advice, _) = getAQIAdvice(newAqi)
                    setAQIData(newAqi, advice)
                    fetchAiExplanation()
                    pushNotification(newAqi)
                    //fetchAndDisplayForecastAQI(latitude, longitude)
                } else {
                    Log.w("AQI", "PM2.5 is null, skipping AQI calculation.")
                }

                loading.text = ""
            } else {
                loading.text = "Failed to load AQI data"
            }
        }

        viewModel.errorMessage.observe(this) { error ->
            if (error.isNotEmpty()) {
                loading.text = error
            }
        }
    }

    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getCurrentLocation()
            }
            else -> {
                requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun requestNotificationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                pushNotification(aqiValue)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private val requestLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getCurrentLocation()
        } else {
            loading.text = "Location permission denied"
            Toast.makeText(this, "Location permission is required to fetch AQI data", Toast.LENGTH_LONG).show()
        }
    }

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            pushNotification(aqiValue)
        } else {
            Log.w("Notification", "Notification permission denied")
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission()
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                    viewModel.loadAqidata(latitude, longitude)
                    //fetchAndDisplayForecastAQI(latitude, longitude)
                } else {
                    loading.text = "Unable to get location"
                    Toast.makeText(this, "Unable to retrieve location", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                loading.text = "Error getting location: ${it.message}"
                Toast.makeText(this, "Error getting location", Toast.LENGTH_SHORT).show()
            }

        val historyButton = findViewById<Button>(R.id.historyButton)
        historyButton.setOnClickListener {
            val intent = Intent(this, HistoricalAQIActivity::class.java)
            intent.putExtra("latitude", latitude)
            intent.putExtra("longitude", longitude)
            startActivity(intent)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setAQIData(aqi: Int, message: String) {
        val (level, _, colorRes) = getAQIAdvice(aqi)
        globalAqi = aqi.toDouble()
        aqiValueText.text = "AQI: $aqi"
        aqiLevelText.text = "Level: $level"
        aqiAdviceText.text = message

        cardFront.setCardBackgroundColor(ContextCompat.getColor(this, colorRes))
        cardBack.setCardBackgroundColor(ContextCompat.getColor(this, colorRes))
    }

    private fun getAQIAdvice(aqi: Int): Triple<String, String, Int> {
        return when (aqi) {
            in 0..50 -> Triple("Good", "Air quality is good. Enjoy outdoor activities!", android.R.color.holo_green_light)
            in 51..100 -> Triple("Satisfactory", "Air quality is fine. Sensitive individuals should monitor symptoms.", android.R.color.holo_blue_light)
            in 101..200 -> Triple("Moderate", "Consider reducing outdoor exertion, especially if you have asthma.", android.R.color.holo_orange_light)
            in 201..300 -> Triple("Poor", "Avoid outdoor activities. Use a mask.", android.R.color.holo_red_light)
            in 301..400 -> Triple("Very Poor", "Stay indoors. Avoid physical exertion.", android.R.color.holo_purple)
            else -> Triple("Severe", "Hazardous air. Do not go outside without a proper respirator.", android.R.color.black)
        }
    }

    private fun calculateAQI(pm25: Double): Int {
        return when {
            pm25 <= 12.0 -> ((50.0 / 12.0) * pm25).toInt()
            pm25 <= 35.4 -> (50 + ((100 - 50) / (35.4 - 12.0)) * (pm25 - 12.0)).toInt()
            pm25 <= 55.4 -> (100 + ((150 - 100) / (55.4 - 35.4)) * (pm25 - 35.4)).toInt()
            pm25 <= 150.4 -> (150 + ((200 - 150) / (150.4 - 55.4)) * (pm25 - 55.4)).toInt()
            pm25 <= 250.4 -> (200 + ((300 - 200) / (250.4 - 150.4)) * (pm25 - 150.4)).toInt()
            pm25 <= 350.4 -> (300 + ((400 - 300) / (350.4 - 250.4)) * (pm25 - 250.4)).toInt()
            else -> (400 + ((500 - 400) / (500.4 - 350.4)) * (pm25 - 350.4)).toInt()
        }
    }

    private fun pushNotification(aqiValue: Int) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "aqi_channel"
        val channel = NotificationChannel(
            channelId,
            "AQI Alerts",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        val (level, advice, _) = getAQIAdvice(aqiValue)
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("AQI Alert: $level")
            .setContentText(advice)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }

    private fun populateSampleForecast() {
        val container = findViewById<LinearLayout>(R.id.forecastCardContainer)
        container.removeAllViews()

        val samplePmValues = listOf(18.0, 42.0, 65.0, 89.0, 120.0, 75.0, 50.0)

        samplePmValues.forEachIndexed { index, pm ->
            val card = LayoutInflater.from(this)
                .inflate(R.layout.item_aqi_card, container, false)

            card.findViewById<TextView>(R.id.dayLabel).text = "Day ${index + 1}"
            card.findViewById<TextView>(R.id.pmValue).text = "PM2.5: ${pm.toInt()}"
            card.findViewById<TextView>(R.id.aqiIcon).text = getAQIEmoji(pm)

            container.addView(card)
        }
    }

    private fun getAQIEmoji(pm: Double): String {
        return when {
            pm <= 30 -> "🟢"
            pm <= 60 -> "🟡"
            pm <= 90 -> "🟠"
            pm <= 120 -> "🔴"
            pm <= 250 -> "🟣"
            else -> "⚫"
        }
    }

}
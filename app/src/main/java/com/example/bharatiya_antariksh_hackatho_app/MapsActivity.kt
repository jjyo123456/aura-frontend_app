package com.example.bharatiya_antariksh_hackatho_app

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bharatiya_antariksh_hackatho_app.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.heatmaps.Gradient
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var currentOverlay: TileOverlay? = null

    private val pollutantFiles = mapOf(
        "PM2.5" to "india_pm25_heatmap.csv",
        "NO2" to "no2_data_cleaned.csv"
        // Add more mappings like SO2, AOD, etc. here
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Setup spinner
        val spinner = findViewById<Spinner>(R.id.spinner)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, pollutantFiles.keys.toList())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.setSelection(0) // Default to PM2.5
        spinner.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>, view: android.view.View?,
                position: Int, id: Long
            ) {
                val selectedPollutant = parent.getItemAtPosition(position) as String
                val fileName = pollutantFiles[selectedPollutant]
                fileName?.let { loadHeatmapFromAsset(it) }
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {}
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val delhi = LatLng(28.61, 77.2)
       // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(delhi, 10f))

        // Load default heatmap (PM2.5)
        loadHeatmapFromAsset(pollutantFiles["PM2.5"]!!)
    }

    private fun loadHeatmapFromAsset(fileName: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val heatList = mutableListOf<WeightedLatLng>()
                val file = assets.open(fileName)
                val reader = BufferedReader(InputStreamReader(file))
                reader.readLine() // Skip header

                var line: String? = reader.readLine()
                while (line != null) {
                    val tokens = line.split(",")
                    if (tokens.size >= 3) {
                        val lat = tokens[0].toDoubleOrNull()
                        val lon = tokens[1].toDoubleOrNull()
                        val weight = tokens[2].toDoubleOrNull()

                        if (lat != null && lon != null && weight != null) {
                            heatList.add(WeightedLatLng(LatLng(lat, lon), weight))
                        } else {
                            Log.w("MapsActivity", "Invalid data in line: $line")
                        }
                    }
                    line = reader.readLine()
                }
                reader.close()

                withContext(Dispatchers.Main) {
                    if (heatList.isNotEmpty()) {
                        // Remove previous overlay
                        currentOverlay?.remove()

                        // Define gradient
                        val gradient = Gradient(
                            intArrayOf(
                                0xFF00E400.toInt(), // Green
                                0xFFFFFF00.toInt(), // Yellow
                                0xFFFF7E00.toInt(), // Orange
                                0xFFFF0000.toInt(), // Red
                                0xFF7E0023.toInt()  // Maroon
                            ),
                            floatArrayOf(0.2f, 0.4f, 0.6f, 0.8f, 1.0f)
                        )

                        // Create new heatmap provider
                        val provider = HeatmapTileProvider.Builder()
                            .weightedData(heatList)
                            .radius(40)
                            .gradient(gradient)
                            .build()

                        // Add to map
                        currentOverlay = mMap.addTileOverlay(TileOverlayOptions().tileProvider(provider))
                    } else {
                        Log.w("MapsActivity", "No valid heatmap data")
                    }
                }
            } catch (e: Exception) {
                Log.e("MapsActivity", "Failed to load $fileName: ${e.message}", e)
            }
        }
    }
}

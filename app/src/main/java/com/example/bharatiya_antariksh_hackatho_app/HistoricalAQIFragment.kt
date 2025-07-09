package com.example.bharatiya_antariksh_hackatho_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class HistoricalAQIFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the same layout used by MainActivity
        return inflater.inflate(R.layout.activity_historical_aqiactivity, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Optionally reinitialize any code from MainActivity here
        // Or call reusable functions if you extract them from the Activity
    }
}
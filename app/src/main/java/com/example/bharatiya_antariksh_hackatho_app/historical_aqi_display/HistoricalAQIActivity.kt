package com.example.bharatiya_antariksh_hackatho_app.historical_aqi

import android.R.attr.data
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsAnimation
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.androidplot.xy.BoundaryMode
import com.androidplot.xy.LineAndPointFormatter
import com.androidplot.xy.PanZoom
import com.androidplot.xy.SimpleXYSeries
import com.androidplot.xy.XYGraphWidget
import com.androidplot.xy.XYPlot
import com.androidplot.xy.XYSeries
import com.example.bharatiya_antariksh_hackatho_app.R
import com.example.bharatiya_antariksh_hackatho_app.historical_aqi.HistoricalAQIActivity.pm10_fragment
import com.example.bharatiya_antariksh_hackatho_app.historical_aqi_display.HistoricalAQIViewModel
import com.example.bharatiya_antariksh_hackatho_app.historical_aqi_display.HistoricalAirQualityResponse
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.properties.Delegates


class HistoricalAQIActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var aqiDataText: TextView
    private val viewModel = HistoricalAQIViewModel()

    private var data by Delegates.notNull<HistoricalAirQualityResponse>()

    private lateinit  var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historical_aqiactivity)

        statusText = findViewById(R.id.statusText)
        aqiDataText = findViewById(R.id.aqiDataText)

        viewPager  = findViewById<ViewPager2>(R.id.viewPager2)
        tabLayout = findViewById<TabLayout>(R.id.tablayout)

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val today = LocalDate.now()
        val sevenDaysAgo = today.minusDays(7)
        val startDate = sevenDaysAgo.format(formatter)
        val endDate = today.format(formatter)

        // Load and poll for data
        lifecycleScope.launch {
            var latitude = intent.getDoubleExtra("latitude" , 0.0)
            var longitude = intent.getDoubleExtra("longitude" , 0.0)
            viewModel.loadHistoricalAQI(latitude, longitude, startDate, endDate)

            // Poll until data is available or error occurs
            repeat(20) {
                if (viewModel.historicalAQIData != null) {
                    displayData()
                    return@launch
                } else if (viewModel.errorMessage.isNotEmpty()) {
                    statusText.text = viewModel.errorMessage
                    return@launch
                }
                delay(300)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayData() {
        statusText.setText("")
        data = viewModel.historicalAQIData ?: return

        setupViewPagerWithData(data)


    }

    private fun setupViewPagerWithData(data: HistoricalAirQualityResponse) {
       viewPager.post {
           val adapter = createfragment(
               this,
               data.hourly.pm10,
               data.hourly.pm2_5,
               data.hourly.nitrogen_dioxide
           )

           viewPager.adapter = adapter

           val titles = listOf("PM10", "PM2_5", "NO2")

           TabLayoutMediator(tabLayout, viewPager) { tab, position ->
               tab.text = titles[position]
           }.attach()
       }
    }

    class createfragment(activity : HistoricalAQIActivity ,var pm10_series: List<Double> ,var pm2_5_series: List<Double> ,var no2_series: List<Double>) : FragmentStateAdapter(activity) {
        override fun createFragment(position: Int): Fragment {
            return when(position){
                0 -> pm10_fragment.newInstance( pm10_series)
                1 -> pm2_5_fragment.newInstance(pm2_5_series)
                2 -> no2_fragment.newInstance(no2_series)
                else -> {
                    throw IllegalStateException("hi")
                }
            }

        }

        override fun getItemCount(): Int {
           return 3
        }
    }


    class pm10_fragment : Fragment(){
        public lateinit var data:List<Double>
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.pm10_graphs,container,false)
        }


        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            data = arguments?.getDoubleArray("pm10_series")?.toList()!!



            val plot = view.findViewById<XYPlot>(R.id.XYPlot_pm10)

            // Create series with Y values only (X = index)
            val series = SimpleXYSeries(
                data,
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
                "PM10 µg/m³"
            )

            val formatter = LineAndPointFormatter().apply {
                linePaint.color = Color.BLUE
                linePaint.strokeWidth = 5f

                vertexPaint.color = Color.RED
                vertexPaint.strokeWidth = 8f

                fillPaint = null  // removes filled area under the line
            }

            // Clear old series if any
            plot.clear()
            plot.addSeries(series, formatter)

            // Axis boundaries
            plot.setTitle("Test AQI Graph")
            plot.setDomainLabel("Day")
            plot.setRangeLabel("AQI (µg/m³)")

            PanZoom.attach(plot)

            plot.outerLimits.set(0,150,0,150)

          //  plot.setMarkupEnabled(true)

            plot.domainTitle.setPadding(20.0F, 20.0F, 20.0F,20.0F)


            plot.graph.getLineLabelStyle(XYGraphWidget.Edge.LEFT).paint.textSize = 36f
            plot.graph.getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).paint.textSize = 36f
            // Prevent labels from getting cut off
            plot.setPadding(32, 32, 32, 64)

            // Redraw the graph
            plot.redraw()
        }
        companion object{
            fun newInstance( pm10_series : List<Double>) : pm10_fragment{
                var fragment = pm10_fragment()
                var bundle = Bundle()
                bundle.putDoubleArray("pm10_series" , pm10_series.toDoubleArray())
                fragment.arguments = bundle
                return fragment
            }

        }
    }

    class pm2_5_fragment : Fragment(){
        public lateinit var data:List<Double>
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.pm2_5_graphs,container,false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            data = arguments?.getDoubleArray("pm2_5_series")?.toList()!!



            val plot = view.findViewById<XYPlot>(R.id.XYPlot_pm2_5)

            // Create series with Y values only (X = index)
            val series = SimpleXYSeries(
                data,
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
                "PM2_5 µg/m³"
            )

            val formatter = LineAndPointFormatter().apply {
                linePaint.color = Color.BLUE
                linePaint.strokeWidth = 5f

                vertexPaint.color = Color.RED
                vertexPaint.strokeWidth = 8f

                fillPaint = null  // removes filled area under the line
            }

            // Clear old series if any
            plot.clear()
            plot.addSeries(series, formatter)

            // Axis boundaries
            plot.setTitle("Test AQI Graph")
            plot.setDomainLabel("Day")
            plot.setRangeLabel("AQI (µg/m³)")

            PanZoom.attach(plot)

            plot.outerLimits.set(0,150,0,150)

           // plot.setMarkupEnabled(true)

            plot.domainTitle.setPadding(20.0F, 20.0F, 20.0F,20.0F)


            plot.graph.getLineLabelStyle(XYGraphWidget.Edge.LEFT).paint.textSize = 36f
            plot.graph.getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).paint.textSize = 36f
            // Prevent labels from getting cut off
            plot.setPadding(32, 32, 32, 64)

            // Redraw the graph
            plot.redraw()
        }
        companion object{
            fun newInstance( pm2_5_series : List<Double>) : pm2_5_fragment{
                var fragment = pm2_5_fragment()
                var bundle = Bundle()
                bundle.putDoubleArray("pm2_5_series" , pm2_5_series.toDoubleArray())
                fragment.arguments = bundle
                return fragment
            }

        }
        }
    }

    class no2_fragment : Fragment(){
        public lateinit var data : List<Double>
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.no2_graphs,container,false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            data = arguments?.getDoubleArray("no2_series")?.toList()!!



            val plot = view.findViewById<XYPlot>(R.id.XYPlot_no2)

            // Create series with Y values only (X = index)
            val series = SimpleXYSeries(
                data,
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
                "PM2_5 µg/m³"
            )

            val formatter = LineAndPointFormatter().apply {
                linePaint.color = Color.BLUE
                linePaint.strokeWidth = 5f

                vertexPaint.color = Color.RED
                vertexPaint.strokeWidth = 8f

                fillPaint = null  // removes filled area under the line
            }

            // Clear old series if any
            plot.clear()
            plot.addSeries(series, formatter)

            // Axis boundaries
            plot.setTitle("Test AQI Graph")
            plot.setDomainLabel("Day")
            plot.setRangeLabel("AQI (µg/m³)")

            PanZoom.attach(plot)

            plot.outerLimits.set(0,150,0,150)

           // plot.setMarkupEnabled(true)

            plot.domainTitle.setPadding(20.0F, 20.0F, 20.0F,20.0F)


            plot.graph.getLineLabelStyle(XYGraphWidget.Edge.LEFT).paint.textSize = 36f
            plot.graph.getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).paint.textSize = 36f
            // Prevent labels from getting cut off
            plot.setPadding(32, 32, 32, 64)

            // Redraw the graph
            plot.redraw()
        }
        companion object{
            fun newInstance( no2_series : List<Double>) : no2_fragment{
                var fragment = no2_fragment()
                var bundle = Bundle()
                bundle.putDoubleArray("no2_series" , no2_series.toDoubleArray())
                fragment.arguments = bundle
                return fragment
            }

        }
    }

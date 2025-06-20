package com.example.transitbuddy_AndroidApp.ui

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.transitbuddy_AndroidApp.R
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapAdapter
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

class StationsMapActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var customFont: Typeface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))
        Configuration.getInstance().userAgentValue = packageName
        setContentView(R.layout.activity_stations_map)
        supportActionBar?.hide()

        map = findViewById(R.id.map)

        // CartoDB Positron Tile
        val cartoPositron = XYTileSource(
            "CartoDB-Positron",
            0, 20, 256, ".png",
            arrayOf("https://basemaps.cartocdn.com/light_all/")
        )
        map.setTileSource(cartoPositron)

        map.setMultiTouchControls(true)
        map.controller.setZoom(14.4)
        map.controller.setCenter(GeoPoint(14.6300, 121.0560)) // Cubao

        customFont = ResourcesCompat.getFont(this, R.font.red_hat_display_bold) ?: Typeface.DEFAULT_BOLD

        map.addMapListener(object : MapAdapter() {
            override fun onZoom(event: ZoomEvent?) = refreshMap().let { true }
            override fun onScroll(event: ScrollEvent?) = refreshMap().let { true }
        })

        refreshMap()
    }

    private fun refreshMap() {
        map.overlays.clear()
        drawMrt3()
        drawLrt1()
        drawLrt2()
        map.invalidate()
    }

    private fun drawMrt3() {
        val stations = listOf(
            "North Avenue" to GeoPoint(14.6546, 121.0341),
            "Quezon Avenue" to GeoPoint(14.6451, 121.0389),
            "GMA Kamuning" to GeoPoint(14.6386, 121.0453),
            "Araneta-Cubao" to GeoPoint(14.6225, 121.0531),
            "Santolan-Annapolis" to GeoPoint(14.6136, 121.0611),
            "Ortigas" to GeoPoint(14.5869, 121.0567),
            "Shaw Boulevard" to GeoPoint(14.5832, 121.0536),
            "Boni" to GeoPoint(14.5762, 121.0465),
            "Guadalupe" to GeoPoint(14.5673, 121.0433),
            "Buendia" to GeoPoint(14.5611, 121.0358),
            "Ayala" to GeoPoint(14.5543, 121.0300),
            "Magallanes" to GeoPoint(14.5469, 121.0209),
            "Taft Avenue" to GeoPoint(14.5384, 121.0004)
        )
        drawRoute(stations.map { it.second }, Color.BLUE)
        drawLabelMarkers(stations, Color.BLUE)
    }

    private fun drawLrt1() {
        val stations = listOf(
            "Roosevelt" to GeoPoint(14.6585, 121.0196),
            "Balintawak" to GeoPoint(14.6533, 121.0110),
            "Monumento" to GeoPoint(14.6412, 120.9847),
            "5th Avenue" to GeoPoint(14.6328, 120.9820),
            "R. Papa" to GeoPoint(14.6233, 120.9794),
            "Abad Santos" to GeoPoint(14.6158, 120.9764),
            "Blumentritt" to GeoPoint(14.6102, 120.9740),
            "Tayuman" to GeoPoint(14.6048, 120.9716),
            "Bambang" to GeoPoint(14.5993, 120.9705),
            "Doroteo Jose" to GeoPoint(14.5941, 120.9735),
            "Carriedo" to GeoPoint(14.5910, 120.9796),
            "Central Terminal" to GeoPoint(14.5891, 120.9818),
            "UN Avenue" to GeoPoint(14.5828, 120.9862),
            "Pedro Gil" to GeoPoint(14.5756, 120.9887),
            "Quirino" to GeoPoint(14.5700, 120.9922),
            "Vito Cruz" to GeoPoint(14.5632, 120.9951),
            "Gil Puyat" to GeoPoint(14.5542, 120.9988),
            "Libertad" to GeoPoint(14.5481, 121.0014),
            "EDSA" to GeoPoint(14.5410, 121.0034),
            "Baclaran" to GeoPoint(14.5364, 121.0024)
        )
        drawRoute(stations.map { it.second }, Color.YELLOW)
        drawLabelMarkers(stations, Color.YELLOW)
    }

    private fun drawLrt2() {
        val stations = listOf(
            "Recto" to GeoPoint(14.6097, 121.0038),
            "Legarda" to GeoPoint(14.6126, 121.0120),
            "Pureza" to GeoPoint(14.6158, 121.0203),
            "V. Mapa" to GeoPoint(14.6190, 121.0286),
            "J. Ruiz" to GeoPoint(14.6225, 121.0369),
            "Gilmore" to GeoPoint(14.6262, 121.0446),
            "Betty Go-Belmonte" to GeoPoint(14.6295, 121.0507),
            "Cubao" to GeoPoint(14.6346, 121.0518),
            "Anonas" to GeoPoint(14.6379, 121.0615),
            "Katipunan" to GeoPoint(14.6398, 121.0686),
            "Santolan" to GeoPoint(14.6237, 121.0810),
            "Marikina" to GeoPoint(14.6318, 121.0957),
            "Antipolo" to GeoPoint(14.6151, 121.1236)
        )
        drawRoute(stations.map { it.second }, Color.MAGENTA)
        drawLabelMarkers(stations, Color.MAGENTA)
    }

    private fun drawRoute(points: List<GeoPoint>, color: Int) {
        val borderLine = Polyline().apply {
            setPoints(points)
            outlinePaint.color = Color.BLACK
            outlinePaint.strokeWidth = 14f
            outlinePaint.style = Paint.Style.STROKE
            outlinePaint.isAntiAlias = true
        }

        val mainLine = Polyline().apply {
            setPoints(points)
            outlinePaint.color = color
            outlinePaint.strokeWidth = 10f
            outlinePaint.style = Paint.Style.STROKE
            outlinePaint.isAntiAlias = true
        }

        map.overlays.add(borderLine)
        map.overlays.add(mainLine)
    }

    private fun drawLabelMarkers(stations: List<Pair<String, GeoPoint>>, lineColor: Int) {
        val zoom = map.zoomLevelDouble
        val scale = ((zoom - 10.0) / 5.0).coerceIn(0.3, 1.2).toFloat()
        val fontSize = 28f * scale
        val padding = 20f

        for ((name, point) in stations) {
            // Station Dot
            val dotSize = 28
            val dot = Bitmap.createBitmap(dotSize, dotSize, Bitmap.Config.ARGB_8888).apply {
                val canvas = Canvas(this)
                val fill = Paint().apply {
                    color = lineColor
                    isAntiAlias = true
                }
                val border = Paint().apply {
                    color = Color.WHITE
                    strokeWidth = 4f
                    style = Paint.Style.STROKE
                    isAntiAlias = true
                }
                canvas.drawCircle(dotSize/2f, dotSize/2f, 10f, fill)
                canvas.drawCircle(dotSize/2f, dotSize/2f, 10f, border)
            }

            val dotMarker = Marker(map).apply {
                position = point
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                icon = BitmapDrawable(resources, dot)
            }
            map.overlays.add(dotMarker)

            if (zoom < 11.0) continue

            // Label with shadow
            val textPaint = Paint().apply {
                color = Color.BLACK
                typeface = customFont
                isAntiAlias = true
                textSize = fontSize
            }

            val textWidth = textPaint.measureText(name)
            val textHeight = textPaint.descent() - textPaint.ascent()
            val labelWidth = (textWidth + padding * 2).toInt()
            val labelHeight = (textHeight + padding * 2).toInt()

            val labelBitmap = Bitmap.createBitmap(labelWidth, labelHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(labelBitmap)

            val bgPaint = Paint().apply {
                color = Color.WHITE
                setShadowLayer(8f, 0f, 0f, Color.LTGRAY)
                isAntiAlias = true
            }

            canvas.drawRoundRect(
                RectF(0f, 0f, labelWidth.toFloat(), labelHeight.toFloat()),
                20f, 20f, bgPaint
            )
            canvas.drawText(name, padding, -textPaint.ascent() + padding, textPaint)

            val labelMarker = Marker(map).apply {
                position = point
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                icon = BitmapDrawable(resources, labelBitmap)
                title = null
                setOnMarkerClickListener { marker, _ ->
                    map.controller.animateTo(marker.position)
                    true
                }
            }

            map.overlays.add(labelMarker)
        }
    }
}

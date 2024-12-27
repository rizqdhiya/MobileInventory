package xriz.my.id.mobileinventory

import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.api.IGeoPoint
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.net.URL

class LokasiPickerActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var btnSimpanLokasi: Button
    private lateinit var searchEditText: EditText
    private lateinit var btnCariLokasi: Button

    // Mengubah tipe selectedLocation menjadi IGeoPoint? (nullable)
    private var selectedLocation: IGeoPoint? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lokasi_picker)

        // Konfigurasi OSMDroid
        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(this))

        // Inisialisasi UI
        mapView = findViewById(R.id.map)
        btnSimpanLokasi = findViewById(R.id.btn_simpan_lokasi)
        searchEditText = findViewById(R.id.search_edit_text)
        btnCariLokasi = findViewById(R.id.btn_cari_lokasi)

        // Konfigurasi MapView
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(15.0)
        val defaultPoint = GeoPoint(-6.200000, 106.816666) // Jakarta
        mapView.controller.setCenter(defaultPoint)

        var marker = createMarker(defaultPoint)

        // Klik pada peta untuk memilih lokasi
        mapView.setOnClickListener { event ->
            val projection = mapView.projection
            val geoPoint = projection.fromPixels(event.x.toInt(), event.y.toInt()) as GeoPoint
            selectedLocation = geoPoint

            mapView.overlays.remove(marker)
            marker = createMarker(geoPoint)
            mapView.overlays.add(marker)
        }

        // Fitur pencarian lokasi
        btnCariLokasi.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                cariLokasi(query) { lokasi ->
                    if (lokasi != null) {
                        selectedLocation = lokasi // IGeoPoint?

                        mapView.overlays.remove(marker) // Hapus marker lama
                        marker = createMarker(lokasi) // Buat marker baru
                        mapView.controller.setCenter(lokasi) // Pusatkan peta ke lokasi
                        mapView.overlays.add(marker)

                        Toast.makeText(
                            this,
                            "Lokasi ditemukan: Latitude ${lokasi.latitude}, Longitude ${lokasi.longitude}",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(this, "Lokasi tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Masukkan nama lokasi terlebih dahulu", Toast.LENGTH_SHORT).show()
            }
        }

        // Simpan lokasi yang dipilih
        btnSimpanLokasi.setOnClickListener {
            if (selectedLocation != null) {
                val resultIntent = intent
                resultIntent.putExtra("latitude", selectedLocation!!.latitude)
                resultIntent.putExtra("longitude", selectedLocation!!.longitude)
                setResult(RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(this, "Pilih lokasi terlebih dahulu!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Fungsi untuk membuat marker, menerima GeoPoint
    private fun createMarker(position: GeoPoint): Marker {
        val marker = Marker(mapView)
        marker.position = position
        marker.title = "Lokasi yang Dipilih"
        marker.isDraggable = true

        marker.setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
            override fun onMarkerDrag(marker: Marker) {}

            override fun onMarkerDragEnd(marker: Marker) {
                val geoPoint = marker.position
                selectedLocation = geoPoint
                Toast.makeText(
                    this@LokasiPickerActivity, // Gunakan this@LokasiPickerActivity
                    "Lokasi dipindahkan: Latitude ${geoPoint.latitude}, Longitude ${geoPoint.longitude}",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onMarkerDragStart(marker: Marker) {}
        })

        return marker
    }


    // Fungsi untuk mencari lokasi berdasarkan query
    private fun cariLokasi(query: String, callback: (GeoPoint?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url =
                    "https://nominatim.openstreetmap.org/search?q=$query&format=json&addressdetails=1"
                val response = URL(url).readText()
                val jsonArray = JSONArray(response)
                if (jsonArray.length() > 0) {
                    val lokasiObj = jsonArray.getJSONObject(0)
                    val lat = lokasiObj.getDouble("lat")
                    val lon = lokasiObj.getDouble("lon")
                    val geoPoint = GeoPoint(lat, lon)
                    withContext(Dispatchers.Main) {
                        callback(geoPoint) // Callback dengan GeoPoint
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        callback(null)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback(null)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}

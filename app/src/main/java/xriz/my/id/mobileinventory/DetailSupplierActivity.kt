package xriz.my.id.mobileinventory

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import xriz.my.id.mobileinventory.model.Supplier

class DetailSupplierActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var namaTextView: TextView
    private lateinit var alamatTextView: TextView
    private lateinit var kontakTextView: TextView
    private lateinit var lokasiTextView: TextView
    private lateinit var mapButton: Button
    private var supplierId: String? = null
    private var latitude: Double? = null
    private var longitude: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_supplier)

        // Ambil supplierId dari Intent
        supplierId = intent.getStringExtra("SUPPLIER_ID")
        if (supplierId.isNullOrEmpty()) {
            Log.e("DetailSupplierActivity", "Supplier ID tidak ditemukan di Intent!")
            Toast.makeText(this, "ID Supplier tidak ditemukan!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Referensi database Firebase
        database = FirebaseDatabase.getInstance().reference.child("supplier").child(supplierId!!)

        // Inisialisasi View
        namaTextView = findViewById(R.id.nama_supplier_detail)
        alamatTextView = findViewById(R.id.alamat_supplier_detail)
        kontakTextView = findViewById(R.id.kontak_supplier_detail)
        lokasiTextView = findViewById(R.id.lokasi_supplier_detail)
        mapButton = findViewById(R.id.btn_open_map)

        // Memuat detail supplier
        loadSupplierDetail()

        // Aksi tombol untuk membuka Google Maps
        mapButton.setOnClickListener {
            if (latitude != null && longitude != null) {
                val gmmIntentUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(Supplier)")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                if (mapIntent.resolveActivity(packageManager) != null) {
                    startActivity(mapIntent)
                } else {
                    Toast.makeText(this, "Google Maps tidak tersedia", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Koordinat lokasi tidak tersedia", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadSupplierDetail() {
        database.get()
            .addOnSuccessListener { snapshot ->
                val supplier = snapshot.getValue(Supplier::class.java)
                if (supplier != null) {
                    namaTextView.text = supplier.nama ?: "Nama tidak tersedia"
                    alamatTextView.text = supplier.alamat ?: "Alamat tidak tersedia"
                    kontakTextView.text = supplier.kontak ?: "Kontak tidak tersedia"

                    val lokasi = supplier.lokasi
                    latitude = lokasi?.latitude
                    longitude = lokasi?.longitude

                    lokasiTextView.text = if (latitude != null && longitude != null) {
                        "Lat: $latitude, Long: $longitude"
                    } else {
                        "Lokasi tidak tersedia"
                    }
                } else {
                    Toast.makeText(this, "Detail Supplier tidak ditemukan!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memuat detail supplier", Toast.LENGTH_SHORT).show()
            }
    }
}

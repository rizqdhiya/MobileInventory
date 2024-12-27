package xriz.my.id.mobileinventory

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.widget.Button
import android.widget.EditText
import xriz.my.id.mobileinventory.model.Lokasi
import xriz.my.id.mobileinventory.model.Supplier
import android.location.Geocoder
import android.location.Address
import java.util.*

class TambahSupplierActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    private lateinit var namaEditText: EditText
    private lateinit var alamatEditText: EditText
    private lateinit var kontakEditText: EditText
    private lateinit var tombolTambah: Button
    private lateinit var tombolAmbilLokasi: Button

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    companion object {
        const val LOCATION_PICKER_REQUEST_CODE = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_supplier)

        // Inisialisasi Firebase
        database = FirebaseDatabase.getInstance().reference.child("supplier")

        // Inisialisasi UI
        namaEditText = findViewById(R.id.nama_supplier)
        alamatEditText = findViewById(R.id.alamat_supplier)
        kontakEditText = findViewById(R.id.kontak_supplier)
        tombolTambah = findViewById(R.id.btn_tambah_supplier)
        tombolAmbilLokasi = findViewById(R.id.btn_ambil_lokasi)

        tombolAmbilLokasi.setOnClickListener {
            val intent = Intent(this, LokasiPickerActivity::class.java)
            startActivityForResult(intent, LOCATION_PICKER_REQUEST_CODE)
        }

        tombolTambah.setOnClickListener {
            simpanSupplier()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOCATION_PICKER_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            latitude = data.getDoubleExtra("latitude", 0.0)
            longitude = data.getDoubleExtra("longitude", 0.0)

            // Menggunakan Geocoder untuk mendapatkan alamat dari latitude dan longitude
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                val fullAddress = address.getAddressLine(0)
                alamatEditText.setText(fullAddress)
                Toast.makeText(this, "Lokasi diperbarui!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Alamat tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun simpanSupplier() {
        val nama = namaEditText.text.toString().trim()
        val alamat = alamatEditText.text.toString().trim()
        val kontak = kontakEditText.text.toString().trim()

        if (nama.isEmpty() || alamat.isEmpty() || kontak.isEmpty()) {
            Toast.makeText(this, "Harap lengkapi semua data", Toast.LENGTH_SHORT).show()
            return
        }

        val lokasi = Lokasi(latitude, longitude)

        val idSupplier = database.push().key ?: ""
        val supplier = Supplier(idSupplier, nama, alamat, kontak, lokasi)

        database.child(idSupplier).setValue(supplier)
            .addOnSuccessListener {
                Toast.makeText(this, "Supplier berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menambahkan supplier: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

package xriz.my.id.mobileinventory

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import xriz.my.id.mobileinventory.model.Lokasi
import xriz.my.id.mobileinventory.model.Supplier
import java.util.Locale

class EditSupplierActivity : AppCompatActivity() {

    private lateinit var supplierNameEditText: EditText
    private lateinit var supplierAddressEditText: EditText
    private lateinit var supplierContactEditText: EditText
    private lateinit var supplierLatitudeEditText: EditText
    private lateinit var supplierLongitudeEditText: EditText
    private lateinit var supplierId: String
    private val LOCATION_PICKER_REQUEST_CODE = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_supplier)

        supplierNameEditText = findViewById(R.id.et_nama_supplier)
        supplierAddressEditText = findViewById(R.id.et_alamat_supplier)
        supplierContactEditText = findViewById(R.id.et_kontak_supplier)
        supplierLatitudeEditText = findViewById(R.id.et_latitude_supplier)
        supplierLongitudeEditText = findViewById(R.id.et_longitude_supplier)

        supplierId = intent.getStringExtra("SUPPLIER_ID") ?: ""
        val nama = intent.getStringExtra("SUPPLIER_NAMA") ?: ""
        val alamat = intent.getStringExtra("SUPPLIER_ALAMAT") ?: ""
        val kontak = intent.getStringExtra("SUPPLIER_KONTAK") ?: ""
        val latitude = intent.getDoubleExtra("SUPPLIER_LATITUDE", 0.0)
        val longitude = intent.getDoubleExtra("SUPPLIER_LONGITUDE", 0.0)

        supplierNameEditText.setText(nama)
        supplierAddressEditText.setText(alamat)
        supplierContactEditText.setText(kontak)
        supplierLatitudeEditText.setText(latitude.toString())
        supplierLongitudeEditText.setText(longitude.toString())


        val pickLocationButton: Button = findViewById(R.id.btn_pilih_lokasi)
        pickLocationButton.setOnClickListener {
            val intent = Intent(this, LokasiPickerActivity::class.java)
            startActivityForResult(intent, LOCATION_PICKER_REQUEST_CODE)
        }

        val saveButton: Button = findViewById(R.id.btn_simpan_supplier)
        saveButton.setOnClickListener {
            val updatedName = supplierNameEditText.text.toString().trim()
            val updatedAddress = supplierAddressEditText.text.toString().trim()
            val updatedContact = supplierContactEditText.text.toString().trim()
            val updatedLatitude = if (supplierLatitudeEditText.text.toString().isNotEmpty()) supplierLatitudeEditText.text.toString().toDouble() else 0.0
            val updatedLongitude = if (supplierLongitudeEditText.text.toString().isNotEmpty()) supplierLongitudeEditText.text.toString().toDouble() else 0.0

            if (updatedName.isEmpty() || updatedAddress.isEmpty() || updatedContact.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            updateSupplier(supplierId, updatedName, updatedAddress, updatedContact, updatedLatitude, updatedLongitude)
        }
    }

    private fun updateSupplier(id: String, name: String, address: String, contact: String, latitude: Double, longitude: Double) {
        val database = FirebaseDatabase.getInstance().getReference("supplier")

        // Gunakan Geocoder untuk mendapatkan alamat dari latitude dan longitude
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

        if (addresses != null && addresses.isNotEmpty()) {
            val addressFromLocation = addresses[0].getAddressLine(0)
            val updatedSupplier = Supplier(id, name, addressFromLocation, contact, Lokasi(latitude, longitude))

            // Update database dengan alamat yang didapat dari Geocoder
            database.child(id).setValue(updatedSupplier)
                .addOnSuccessListener {
                    Toast.makeText(this, "Supplier berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal memperbarui supplier: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Jika Geocoder gagal, update database dengan alamat yang ada (jika ada)
            val updatedSupplier = Supplier(id, name, address, contact, Lokasi(latitude, longitude))
            database.child(id).setValue(updatedSupplier)
                .addOnSuccessListener {
                    Toast.makeText(this, "Supplier berhasil diperbarui (alamat mungkin tidak akurat)", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal memperbarui supplier: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOCATION_PICKER_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val latitude = data.getDoubleExtra("latitude", 0.0)
            val longitude = data.getDoubleExtra("longitude", 0.0)
            supplierLatitudeEditText.setText(latitude.toString())
            supplierLongitudeEditText.setText(longitude.toString())
        }
    }
}
package xriz.my.id.mobileinventory

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class TambahRiwayatActivity : AppCompatActivity() {

    private lateinit var riwayatRef: DatabaseReference
    private lateinit var barangRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_riwayat)

        // Ambil data barang dari Intent
        val namaBarang = intent.getStringExtra("BARANG_NAMA") ?: ""
        val stokBarang = intent.getIntExtra("BARANG_STOK", 0)
        val barangId = intent.getStringExtra("BARANG_ID") ?: ""
        if (barangId.isEmpty()) {
            Toast.makeText(this, "Barang ID tidak valid", Toast.LENGTH_SHORT).show()
            finish()
            return
        }


        // Bind data ke layout
        findViewById<TextView>(R.id.nama_barang).text = "Nama Barang: $namaBarang"
        findViewById<TextView>(R.id.stok_barang).text = "Stok: $stokBarang"

        // Firebase references
        val database = FirebaseDatabase.getInstance()
        riwayatRef = database.getReference("riwayat")
        barangRef = database.getReference("barang")

        val jenisTransaksi = findViewById<Spinner>(R.id.jenisTransaksi)
        val jumlah = findViewById<EditText>(R.id.jumlah)
        val btnPilihTanggal = findViewById<Button>(R.id.btnPilihTanggal)
        val btnSimpan = findViewById<Button>(R.id.btnSimpan)
        val tanggalText = findViewById<TextView>(R.id.tanggalTerpilih)

        // Pilihan untuk transaksi: masuk/keluar
        val transaksiOptions = arrayOf("Tambah Stok", "Kurangi Stok")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, transaksiOptions)
        jenisTransaksi.adapter = spinnerAdapter

        // Default tanggal ke hari ini
        var selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        tanggalText.text = "Tanggal: $selectedDate"

        // Tombol untuk memilih tanggal
        btnPilihTanggal.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                tanggalText.text = "Tanggal: $selectedDate"
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Tombol simpan transaksi
        btnSimpan.setOnClickListener {
            val transaksi = jenisTransaksi.selectedItem.toString()
            val jumlahTransaksi = jumlah.text.toString().toIntOrNull()

            if (transaksi.isNotEmpty() && jumlahTransaksi != null && jumlahTransaksi > 0) {
                // Validasi stok barang untuk transaksi "Kurangi Stok"
                if (transaksi == "Kurangi Stok" && jumlahTransaksi > stokBarang) {
                    Toast.makeText(this, "Stok tidak cukup untuk mengurangi stok", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Simpan data riwayat transaksi ke Firebase
                val riwayatId = riwayatRef.push().key
                val riwayat = mapOf(
                    "id" to riwayatId,
                    "barangId" to barangId,
                    "jenisTransaksi" to transaksi,
                    "jumlah" to jumlahTransaksi,
                    "tanggal" to selectedDate
                )
                riwayatId?.let {
                    riwayatRef.child(it).setValue(riwayat)
                }

                // Update stok barang di Firebase
                val updatedStok = if (transaksi == "Tambah Stok") {
                    stokBarang + jumlahTransaksi
                } else {
                    stokBarang - jumlahTransaksi
                }

                // Pastikan barangId benar dan update stok
                barangRef.child(barangId).child("stok").setValue(updatedStok)
                    .addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {
                            // Kirimkan hasil pembaruan stok kembali ke DetailBarangActivity
                            val resultIntent = Intent().apply {
                                putExtra("UPDATED_STOK", updatedStok)
                            }
                            setResult(RESULT_OK, resultIntent)
                            finish()
                        } else {
                            Toast.makeText(this, "Gagal memperbarui stok barang", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Pastikan semua field diisi dengan benar", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

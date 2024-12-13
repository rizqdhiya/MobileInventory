package xriz.my.id.mobileinventory

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import xriz.my.id.mobileinventory.db.AppDatabase
import xriz.my.id.mobileinventory.db.Riwayat

class TambahRiwayatActivity : AppCompatActivity() {
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_riwayat)

        database = AppDatabase.getDatabase(this)

        // Ambil data barang dari Intent
        val namaBarang = intent.getStringExtra("BARANG_NAMA") ?: ""
        val stokBarang = intent.getIntExtra("BARANG_STOK", 0)
        val barangId = intent.getIntExtra("BARANG_ID", -1)

        // Bind data ke layout
        findViewById<TextView>(R.id.nama_barang).text = "Nama Barang: $namaBarang"
        findViewById<TextView>(R.id.stok_barang).text = "Stok: $stokBarang"

        val jenisTransaksi = findViewById<Spinner>(R.id.jenisTransaksi)
        val jumlah = findViewById<EditText>(R.id.jumlah)
        val btnPilihTanggal = findViewById<Button>(R.id.btnPilihTanggal)
        val btnSimpan = findViewById<Button>(R.id.btnSimpan)
        val tanggalText = findViewById<TextView>(R.id.tanggalTerpilih)

        // Pilihan untuk transaksi: masuk/keluar
        val transaksiOptions = arrayOf("masuk", "keluar")
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

        btnSimpan.setOnClickListener {
            val transaksi = jenisTransaksi.selectedItem.toString()
            val jumlahTransaksi = jumlah.text.toString().toIntOrNull()

            if (transaksi.isNotEmpty() && jumlahTransaksi != null && jumlahTransaksi > 0) {
                val riwayat = Riwayat(
                    barangId = barangId,
                    jenisTransaksi = transaksi,
                    jumlah = jumlahTransaksi,
                    tanggal = selectedDate
                )

                lifecycleScope.launch {
                    try {
                        // Simpan riwayat transaksi ke database
                        database.riwayatDao().insertRiwayat(riwayat)

                        // Ambil data barang dari database
                        val barang = database.barangDao().getBarangById(barangId)
                        if (barang != null) {
                            val updatedStok = if (transaksi == "masuk") {
                                // Transaksi masuk: stok bertambah
                                barang.stok + jumlahTransaksi
                            } else {
                                // Transaksi keluar: stok berkurang (periksa apakah stok cukup)
                                if (barang.stok >= jumlahTransaksi) {
                                    barang.stok - jumlahTransaksi
                                } else {
                                    // Jika stok tidak cukup, tampilkan pesan kesalahan
                                    runOnUiThread {
                                        Toast.makeText(this@TambahRiwayatActivity, "Stok tidak cukup", Toast.LENGTH_SHORT).show()
                                    }
                                    return@launch
                                }
                            }

                            // Update stok barang di database
                            barang.stok = updatedStok
                            database.barangDao().updateBarang(barang)

                            // Kirimkan hasil pembaruan stok kembali ke DetailBarangActivity
                            val resultIntent = Intent().apply {
                                putExtra("UPDATED_STOK", updatedStok)
                            }
                            setResult(RESULT_OK, resultIntent)
                            finish()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        runOnUiThread {
                            Toast.makeText(this@TambahRiwayatActivity, "Terjadi kesalahan saat menyimpan riwayat", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this@TambahRiwayatActivity, "Pastikan semua field diisi dengan benar", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

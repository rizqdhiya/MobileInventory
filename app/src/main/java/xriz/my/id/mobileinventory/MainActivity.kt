package xriz.my.id.mobileinventory

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import xriz.my.id.mobileinventory.adapter.BarangAdapter
import xriz.my.id.mobileinventory.db.AppDatabase
import xriz.my.id.mobileinventory.db.Riwayat
import xriz.my.id.mobileinventory.db.RiwayatDao
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var barangAdapter: BarangAdapter
    private lateinit var riwayatDao: RiwayatDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkAndRequestPermissions()

        // Initialize database and DAO
        database = AppDatabase.getDatabase(this)
        riwayatDao = database.riwayatDao()

        // Initialize adapter for RecyclerView
        barangAdapter = BarangAdapter(
            onItemClick = { barang ->
                // On normal click: open DetailBarangActivity
                val intent = Intent(this, DetailBarangActivity::class.java).apply {
                    putExtra("BARANG_ID", barang.id)
                    putExtra("BARANG_NAMA", barang.nama)
                    putExtra("BARANG_DESKRIPSI", barang.deskripsi)
                    putExtra("BARANG_KATEGORI", barang.kategori)
                    putExtra("BARANG_HARGA", barang.harga)
                    putExtra("BARANG_STOK", barang.stok)
                    putExtra("BARANG_FOTO", barang.fotoPath)
                }
                startActivity(intent)
            },
            onItemLongClick = { barang ->
                // On long click: open EditBarangActivity
                val intent = Intent(this, EditBarangActivity::class.java).apply {
                    putExtra("barangId", barang.id)
                }
                startActivity(intent)
            }
        )

        // Set up RecyclerView for displaying barang list
        findViewById<RecyclerView>(R.id.recyclerView).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = barangAdapter
        }

        // Observe barang data
        database.barangDao().getAllBarang().observe(this) { barangList ->
            barangAdapter.submitList(barangList)
        }

        // Button to add new barang
        findViewById<Button>(R.id.btnTambahBarang).setOnClickListener {
            startActivity(Intent(this, TambahBarangActivity::class.java))
        }

        // Button to delete selected barang
        findViewById<Button>(R.id.btnHapusBarang).setOnClickListener {
            val barangToDelete = barangAdapter.getSelectedBarang()
            if (barangToDelete.isNotEmpty()) {
                barangToDelete.forEach { barang ->
                    lifecycleScope.launch {
                        database.barangDao().deleteBarang(barang)
                        insertRiwayat(barangId = barang.id, jenisTransaksi = "hapus", jumlah = 1)
                    }
                }
                Toast.makeText(this@MainActivity, "Barang berhasil dihapus", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Pilih barang yang ingin dihapus", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)
            }
        }
    }

    private fun insertRiwayat(barangId: Int, jenisTransaksi: String, jumlah: Int) {
        val tanggal = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val riwayat = Riwayat(
            barangId = barangId,
            jenisTransaksi = jenisTransaksi,
            jumlah = jumlah,
            tanggal = tanggal
        )
        lifecycleScope.launch {
            riwayatDao.insertRiwayat(riwayat)
        }
    }
}

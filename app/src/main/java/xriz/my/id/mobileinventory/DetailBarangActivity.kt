package xriz.my.id.mobileinventory

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import xriz.my.id.mobileinventory.db.AppDatabase
import xriz.my.id.mobileinventory.db.Riwayat

class DetailBarangActivity : AppCompatActivity() {

    private val REQUEST_CODE = 1 // Untuk memantau permintaan hasil transaksi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_barang)

        // Ambil data barang dari Intent
        val nama = intent.getStringExtra("BARANG_NAMA") ?: ""
        val deskripsi = intent.getStringExtra("BARANG_DESKRIPSI") ?: ""
        val kategori = intent.getStringExtra("BARANG_KATEGORI") ?: ""
        val harga = intent.getDoubleExtra("BARANG_HARGA", 0.0)
        val stok = intent.getIntExtra("BARANG_STOK", 0)
        val barangId = intent.getIntExtra("BARANG_ID", -1)
        val fotoPath = intent.getStringExtra("BARANG_FOTO") ?: ""
        val recyclerView = findViewById<RecyclerView>(R.id.rvRiwayat)
        recyclerView.layoutManager = LinearLayoutManager(this)
        lifecycleScope.launch {
            val riwayatDao = AppDatabase.getDatabase(this@DetailBarangActivity).riwayatDao()
            val riwayatList: List<Riwayat> = riwayatDao.getAllRiwayat()
                .filter { it.barangId == barangId } // Filter hanya untuk barang ini

            // Set adapter jika data tersedia
            if (riwayatList.isNotEmpty()) {
                val adapter = RiwayatAdapter(riwayatList)
                recyclerView.adapter = adapter
            } else {
                Toast.makeText(this@DetailBarangActivity, "Belum ada riwayat transaksi", Toast.LENGTH_SHORT).show()
            }
        }
        // Bind data ke layout
        findViewById<TextView>(R.id.nama_barang).text = "Nama Barang: $nama"
        findViewById<TextView>(R.id.deskripsi_barang).text = "Deskripsi: $deskripsi"
        findViewById<TextView>(R.id.kategori_barang).text = "Kategori: $kategori"
        findViewById<TextView>(R.id.harga_barang).text = "Rp $harga"
        val stokText = findViewById<TextView>(R.id.stok_barang)
        stokText.text = "Stok: $stok"
        Glide.with(this).load(fotoPath).into(findViewById<ImageView>(R.id.foto_barang))

        // Tombol Tambah Riwayat
        findViewById<Button>(R.id.btnTambahRiwayat).setOnClickListener {
            val intent = Intent(this, TambahRiwayatActivity::class.java).apply {
                putExtra("BARANG_NAMA", nama)
                putExtra("BARANG_STOK", stok)
                putExtra("BARANG_ID", barangId)
            }
            startActivityForResult(intent, REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            val updatedStok = data?.getIntExtra("UPDATED_STOK", -1) ?: return
            findViewById<TextView>(R.id.stok_barang).text = "Stok: $updatedStok"
            Toast.makeText(this, "Stok berhasil diperbarui", Toast.LENGTH_SHORT).show()
        }
    }
}

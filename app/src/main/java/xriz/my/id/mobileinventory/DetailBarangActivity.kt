package xriz.my.id.mobileinventory

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import xriz.my.id.mobileinventory.model.Riwayat

class DetailBarangActivity : AppCompatActivity() {

    private lateinit var riwayatRef: DatabaseReference
    private lateinit var riwayatAdapter: RiwayatAdapter
    private lateinit var supplierRef: DatabaseReference

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
        val barangId = intent.getStringExtra("BARANG_ID") ?: ""
        val fotoPath = intent.getStringExtra("BARANG_FOTO") ?: ""
        // Ambil data barang dari Intent
        val supplierId = intent.getStringExtra("BARANG_SUPPLIER_ID") ?: ""

        // Tambahkan referensi ke Firebase
        supplierRef = FirebaseDatabase.getInstance().getReference("supplier")

        fetchSupplierName(supplierId)

        // Bind data ke layout
        findViewById<TextView>(R.id.nama_barang).text = "Nama Barang: $nama"
        findViewById<TextView>(R.id.deskripsi_barang).text = "Deskripsi: $deskripsi"
        findViewById<TextView>(R.id.kategori_barang).text = "Kategori: $kategori"
        findViewById<TextView>(R.id.harga_barang).text = "Rp $harga"
        val stokText = findViewById<TextView>(R.id.stok_barang)
        stokText.text = "Stok: $stok"
        Glide.with(this).load(fotoPath).into(findViewById<ImageView>(R.id.foto_barang))

        // Setup RecyclerView untuk riwayat
        val recyclerView = findViewById<RecyclerView>(R.id.rvRiwayat)
        recyclerView.layoutManager = LinearLayoutManager(this)
        riwayatAdapter = RiwayatAdapter()
        recyclerView.adapter = riwayatAdapter

        // Referensi Firebase
        riwayatRef = FirebaseDatabase.getInstance().getReference("riwayat")

        // Fetch riwayat untuk barang ini
        fetchRiwayatData(barangId)

        // Tombol Tambah Riwayat
        findViewById<Button>(R.id.btnTambahRiwayat).setOnClickListener {
            val intent = Intent(this, TambahRiwayatActivity::class.java).apply {
                putExtra("BARANG_NAMA", nama)
                putExtra("BARANG_STOK", stok)  // stok yang sesuai dikirim
                putExtra("BARANG_ID", barangId)  // barangId dikirim
            }
            startActivity(intent)
        }
    }

    private fun fetchRiwayatData(barangId: String) {
        riwayatRef.orderByChild("barangId").equalTo(barangId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val riwayatList = mutableListOf<Riwayat>()
                    for (dataSnapshot in snapshot.children) {
                        val riwayat = dataSnapshot.getValue(Riwayat::class.java)
                        riwayat?.let { riwayatList.add(it) }
                    }
                    if (riwayatList.isNotEmpty()) {
                        riwayatAdapter.updateData(riwayatList) // Memperbarui data di adapter
                    } else {
                        Toast.makeText(
                            this@DetailBarangActivity,
                            "Belum ada riwayat transaksi",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@DetailBarangActivity,
                        "Gagal memuat data riwayat",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
    private fun fetchSupplierName(supplierId: String) {
        if (supplierId.isNotEmpty()) {
            Log.d("DetailBarangActivity", "Fetching supplier with ID: $supplierId")
            supplierRef.orderByChild("nama").equalTo(supplierId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (data in snapshot.children) {
                                val supplierName = data.child("nama").getValue(String::class.java) ?: "Tidak diketahui"
                                Log.d("DetailBarangActivity", "Supplier name: $supplierName")
                                findViewById<TextView>(R.id.supplier_barang).text = "Supplier: $supplierName"
                                return
                            }
                        } else {
                            Log.w("DetailBarangActivity", "Supplier not found for ID: $supplierId")
                            findViewById<TextView>(R.id.supplier_barang).text = "Supplier: Tidak diketahui"
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("DetailBarangActivity", "Error fetching supplier: ${error.message}")
                        Toast.makeText(this@DetailBarangActivity, "Gagal memuat data supplier", Toast.LENGTH_SHORT).show()
                    }
                })
        } else {
            findViewById<TextView>(R.id.supplier_barang).text = "Supplier: Tidak diketahui"
            Log.w("DetailBarangActivity", "Supplier ID is empty or null")
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

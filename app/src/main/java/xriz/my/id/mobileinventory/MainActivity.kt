package xriz.my.id.mobileinventory

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.launch
import xriz.my.id.mobileinventory.adapter.BarangAdapter
import xriz.my.id.mobileinventory.model.Barang

class MainActivity : AppCompatActivity() {

    private lateinit var barangAdapter: BarangAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var barangRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = FirebaseAuth.getInstance()

        //  Firebase Database reference
        barangRef = FirebaseDatabase.getInstance().getReference("barang")

        //  adapter for RecyclerView
        barangAdapter = BarangAdapter(
            onItemClick = { barang ->
                // Log untuk debugging
                Log.d("MainActivity", "Barang clicked: ${barang.barangId}")
                val intent = Intent(this, DetailBarangActivity::class.java).apply {
                    putExtra("BARANG_ID", barang.barangId)
                    putExtra("BARANG_NAMA", barang.nama)
                    putExtra("BARANG_DESKRIPSI", barang.deskripsi)
                    putExtra("BARANG_KATEGORI", barang.kategori)
                    putExtra("BARANG_HARGA", barang.harga)
                    putExtra("BARANG_STOK", barang.stok)
                    putExtra("BARANG_FOTO", barang.fotoPath)
                    putExtra("BARANG_SUPPLIER_ID", barang.supplierId)
                }
                startActivity(intent)
            },
            onItemLongClick = { barang ->
                // Log untuk debugging
                Log.d("MainActivity", "Barang long-clicked with ID: ${barang.barangId}")
                if (barang.barangId != null) {
                    val intent = Intent(this, EditBarangActivity::class.java).apply {
                        putExtra("BARANG_ID", barang.barangId)
                    }
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Barang ID tidak valid!", Toast.LENGTH_SHORT).show()
                }
            }
        )

        findViewById<RecyclerView>(R.id.recyclerView).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = barangAdapter
        }

        fetchBarangData()

        findViewById<Button>(R.id.btnTambahBarang).setOnClickListener {
            startActivity(Intent(this, TambahBarangActivity::class.java))
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Berhasil Logout", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
        }

        findViewById<Button>(R.id.btnHapusBarang).setOnClickListener {
            val selectedBarang = barangAdapter.getSelectedBarang()
            if (selectedBarang.isNotEmpty()) {
                selectedBarang.forEach { barang ->
                    lifecycleScope.launch {
                        barangRef.child(barang.barangId.toString()).removeValue().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this@MainActivity, "Barang berhasil dihapus", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@MainActivity, "Gagal menghapus barang", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(this@MainActivity, "Pilih barang yang ingin dihapus", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchBarangData() {
        barangRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val barangList = mutableListOf<Barang>()
                for (dataSnapshot in snapshot.children) {
                    // Menangkap barangId dari kunci Firebase
                    val barangId = dataSnapshot.key // Kunci Firebase adalah barangId
                    val barang = dataSnapshot.getValue(Barang::class.java)

                    // Log untuk debugging
                    Log.d("MainActivity", "Fetched barang with ID: $barangId")

                    if (barang != null) {
                        barang.barangId = barangId // Menetapkan barangId ke objek barang
                        barangList.add(barang)
                    }
                }
                barangAdapter.submitList(barangList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error fetching data", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

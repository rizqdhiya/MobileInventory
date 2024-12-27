package xriz.my.id.mobileinventory

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import xriz.my.id.mobileinventory.adapter.SupplierAdapter
import xriz.my.id.mobileinventory.model.Supplier

class DaftarSupplierActivity : AppCompatActivity() {

    private lateinit var tombolTambahSupplier: Button
    private lateinit var database: DatabaseReference
    private lateinit var supplierRecyclerView: RecyclerView
    private lateinit var supplierList: MutableList<Supplier>
    private lateinit var supplierAdapter: SupplierAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_daftar_supplier)
        tombolTambahSupplier = findViewById(R.id.btn_tambah_supplier)

        // Tombol Back di ActionBar
        supportActionBar?.apply {
            title = "Daftar Supplier"
            setDisplayHomeAsUpEnabled(true)
        }

        database = FirebaseDatabase.getInstance().reference.child("supplier")
        supplierRecyclerView = findViewById(R.id.supplier_recycler_view)
        supplierList = mutableListOf()
        tombolTambahSupplier.setOnClickListener {
            val intent = Intent(this, TambahSupplierActivity::class.java)
            startActivityForResult(intent, 1001)
        }
        // Set up RecyclerView
        supplierAdapter = SupplierAdapter(this, supplierList)
        supplierRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@DaftarSupplierActivity)
            adapter = supplierAdapter
        }

        loadSupplierData()
    }

    private fun loadSupplierData() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                supplierList.clear()
                for (dataSnapshot in snapshot.children) {
                    val supplier = dataSnapshot.getValue(Supplier::class.java)
                    supplier?.let {
                        it.id = dataSnapshot.key // Set ID supplier dari key Firebase
                        supplierList.add(it)
                    }
                }
                supplierAdapter.notifyDataSetChanged() // Update data di RecyclerView
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@DaftarSupplierActivity,
                    "Gagal mengambil data supplier",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    // Tangani klik tombol Back di ActionBar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    // Ketika Back ditekan, kembali ke Dashboard
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent().apply {
            putExtra("REFRESH_DASHBOARD", true) // Mengirimkan sinyal untuk refresh
        }
        setResult(RESULT_OK, intent) // Memberi tahu Activity sebelumnya
        finish() // Tutup Activity ini
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            loadSupplierData() // Refresh data
        }
    }

}

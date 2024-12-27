package xriz.my.id.mobileinventory

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class DashboardActivity : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var barangRef: DatabaseReference
    private lateinit var supplierRef: DatabaseReference

    private lateinit var totalBarangTextView: TextView
    private lateinit var totalSupplierTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Initialize Firebase Database references
        database = FirebaseDatabase.getInstance()
        barangRef = database.getReference("barang")
        supplierRef = database.getReference("supplier")

        // Initialize TextViews
        totalBarangTextView = findViewById(R.id.totalBarang)
        totalSupplierTextView = findViewById(R.id.totalSupplier)

        // Fetch and display data
        fetchBarangCount()
        fetchSupplierCount()

        // Set click listeners for navigation
        totalBarangTextView.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        totalSupplierTextView.setOnClickListener {
            startActivity(Intent(this, DaftarSupplierActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when activity resumes
        fetchBarangCount()
        fetchSupplierCount()
    }

    private fun fetchBarangCount() {
        barangRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val count = snapshot.childrenCount
                totalBarangTextView.text = "Total Barang: $count"
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DashboardActivity, "Gagal mengambil data barang", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchSupplierCount() {
        supplierRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val count = snapshot.childrenCount
                totalSupplierTextView.text = "Jumlah Supplier: $count"
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DashboardActivity, "Gagal mengambil data supplier", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

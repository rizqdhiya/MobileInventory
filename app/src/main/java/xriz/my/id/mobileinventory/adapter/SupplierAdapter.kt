package xriz.my.id.mobileinventory.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import xriz.my.id.mobileinventory.R
import xriz.my.id.mobileinventory.model.Supplier
import xriz.my.id.mobileinventory.DetailSupplierActivity
import com.google.firebase.database.FirebaseDatabase
import xriz.my.id.mobileinventory.DaftarSupplierActivity
import xriz.my.id.mobileinventory.EditSupplierActivity

class SupplierAdapter(private val context: Context, private val supplierList: MutableList<Supplier>) :
    RecyclerView.Adapter<SupplierAdapter.SupplierViewHolder>() {
    private val UPDATE_SUPPLIER_REQUEST_CODE = 2001

    // ViewHolder untuk menampung data setiap item supplier
    inner class SupplierViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nama: TextView = itemView.findViewById(R.id.nama_supplier_item)
        val alamat: TextView = itemView.findViewById(R.id.alamat_supplier_item)
        val kontak: TextView = itemView.findViewById(R.id.kontak_supplier_item)
    }

    // Menghubungkan data supplier ke dalam item di ListView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SupplierViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_supplier, parent, false)
        return SupplierViewHolder(itemView)
    }

    // Mengikat data dengan ViewHolder
    override fun onBindViewHolder(holder: SupplierViewHolder, position: Int) {
        val supplier = supplierList[position]

        holder.nama.text = supplier.nama ?: "Nama tidak tersedia"
        holder.alamat.text = supplier.alamat ?: "Alamat tidak tersedia"
        holder.kontak.text = supplier.kontak ?: "Kontak tidak tersedia"

        // Ketika item diklik, akan menampilkan detail supplier
        holder.itemView.setOnClickListener {
            val supplierId = supplier.id ?: ""
            if (supplierId.isNotEmpty()) {
                val intent = Intent(context, DetailSupplierActivity::class.java)
                intent.putExtra("SUPPLIER_ID", supplierId)
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "ID Supplier tidak valid", Toast.LENGTH_SHORT).show()
            }
        }

        // Ketika item diklik lama, tampilkan dialog untuk hapus atau update
        holder.itemView.setOnLongClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Opsi Supplier")
                .setMessage("Apa yang ingin Anda lakukan dengan ${supplier.nama ?: "Supplier"}?")
                .setPositiveButton("Hapus") { dialog, which ->
                    val supplierId = supplier.id ?: ""
                    if (supplierId.isNotEmpty()) {
                        hapusSupplier(supplierId)
                    } else {
                        Toast.makeText(context, "ID Supplier tidak valid", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Update") { dialog, which ->
                    val supplierId = supplier.id ?: ""
                    if (supplierId.isNotEmpty()) {
                        updateSupplier(supplierId)
                    } else {
                        Toast.makeText(context, "ID Supplier tidak valid", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNeutralButton("Batal", null)
                .show()
            true
        }
    }

    // Menghitung jumlah item dalam daftar supplier
    override fun getItemCount(): Int {
        return supplierList.size
    }

    // Fungsi untuk menghapus supplier
    private fun hapusSupplier(supplierId: String) {
        val database = FirebaseDatabase.getInstance().reference.child("supplier")
        database.child(supplierId).removeValue().addOnSuccessListener {
            Toast.makeText(context, "Supplier berhasil dihapus", Toast.LENGTH_SHORT).show()
            supplierList.removeAll { it.id == supplierId }
            notifyDataSetChanged() // Perbarui RecyclerView
        }.addOnFailureListener {
            Toast.makeText(context, "Gagal menghapus supplier", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateSupplier(supplierId: String) {
        val existingSupplier = supplierList.find { it.id == supplierId }
        existingSupplier?.let {
            val intent = Intent(context, EditSupplierActivity::class.java)
            intent.putExtra("SUPPLIER_ID", it.id)
            intent.putExtra("SUPPLIER_NAMA", it.nama)
            intent.putExtra("SUPPLIER_ALAMAT", it.alamat)
            intent.putExtra("SUPPLIER_KONTAK", it.kontak)
            it.lokasi?.let { lokasi ->
                intent.putExtra("SUPPLIER_LATITUDE", lokasi.latitude)
                intent.putExtra("SUPPLIER_LONGITUDE", lokasi.longitude)
            } ?: run {
                intent.putExtra("SUPPLIER_LATITUDE", 0.0)
                intent.putExtra("SUPPLIER_LONGITUDE", 0.0)
            }
            (context as DaftarSupplierActivity).startActivityForResult(intent, UPDATE_SUPPLIER_REQUEST_CODE)
        } ?: run {
            Toast.makeText(context, "Supplier tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }
}

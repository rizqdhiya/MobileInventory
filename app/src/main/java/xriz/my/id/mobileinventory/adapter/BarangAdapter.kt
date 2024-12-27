package xriz.my.id.mobileinventory.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.NumberFormat
import java.util.*
import xriz.my.id.mobileinventory.R
import xriz.my.id.mobileinventory.model.Barang

class BarangAdapter(
    private val onItemClick: (Barang) -> Unit,
    private val onItemLongClick: (Barang) -> Unit
) : RecyclerView.Adapter<BarangAdapter.BarangViewHolder>() {

    private var barangList: List<Barang> = emptyList()
    private val selectedItems = mutableSetOf<Barang>()

    fun submitList(newList: List<Barang>) {
        barangList = newList
        notifyDataSetChanged()
    }

    fun getSelectedBarang(): List<Barang> = selectedItems.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarangViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_barang, parent, false)
        return BarangViewHolder(view)
    }

    override fun onBindViewHolder(holder: BarangViewHolder, position: Int) {
        val barang = barangList[position]
        // Log untuk debugging
        Log.d("BarangAdapter", "Binding barang ID: ${barang.barangId}")
        holder.bind(barang)
    }

    override fun getItemCount(): Int = barangList.size

    inner class BarangViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imageBarang: ImageView = view.findViewById(R.id.image_barang)
        private val namaBarang: TextView = view.findViewById(R.id.nama_barang)
        private val kategoriBarang: TextView = view.findViewById(R.id.kategori_barang)
        private val hargaBarang: TextView = view.findViewById(R.id.harga_barang)
        private val stockBarang: TextView = view.findViewById(R.id.stock_barang)
        private val checkboxBarang: CheckBox = view.findViewById(R.id.checkbox_barang)

        fun bind(barang: Barang) {
            namaBarang.text = barang.nama
            kategoriBarang.text = barang.kategori
            hargaBarang.text = formatCurrency(barang.harga)
            stockBarang.text = "Stok: ${barang.stok}"

            if (!barang.fotoPath.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(barang.fotoPath)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(imageBarang)
            } else {
                imageBarang.setImageResource(R.drawable.placeholder_image)
            }

            checkboxBarang.isChecked = selectedItems.contains(barang)
            checkboxBarang.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) selectedItems.add(barang) else selectedItems.remove(barang)
            }

            itemView.setOnClickListener { onItemClick(barang) }
            itemView.setOnLongClickListener {
                onItemLongClick(barang)
                true
            }
        }

        private fun formatCurrency(price: Double?): String {
            return if (price != null) {
                val locale = Locale("in", "ID")
                NumberFormat.getCurrencyInstance(locale).format(price)
            } else "Rp 0"
        }
    }
}

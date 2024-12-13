package xriz.my.id.mobileinventory.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import xriz.my.id.mobileinventory.R
import xriz.my.id.mobileinventory.db.Riwayat

class RiwayatAdapter : ListAdapter<Riwayat, RiwayatAdapter.RiwayatViewHolder>(RiwayatDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RiwayatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_riwayat, parent, false)
        return RiwayatViewHolder(view)
    }

    override fun onBindViewHolder(holder: RiwayatViewHolder, position: Int) {
        val riwayat = getItem(position)
        holder.bind(riwayat)
    }

    override fun getItemCount(): Int = currentList.size

    inner class RiwayatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val jenisTransaksi: TextView = view.findViewById(R.id.jenis_transaksi)
        private val jumlah: TextView = view.findViewById(R.id.jumlah)
        private val tanggal: TextView = view.findViewById(R.id.tanggal)

        fun bind(riwayat: Riwayat) {
            jenisTransaksi.text = riwayat.jenisTransaksi
            jumlah.text = "Jumlah: ${riwayat.jumlah}"
            tanggal.text = riwayat.tanggal
        }
    }

    class RiwayatDiffCallback : DiffUtil.ItemCallback<Riwayat>() {
        override fun areItemsTheSame(oldItem: Riwayat, newItem: Riwayat): Boolean {
            return oldItem.id == newItem.id  // Asumsi id adalah primary key
        }

        override fun areContentsTheSame(oldItem: Riwayat, newItem: Riwayat): Boolean {
            return oldItem == newItem
        }
    }
}

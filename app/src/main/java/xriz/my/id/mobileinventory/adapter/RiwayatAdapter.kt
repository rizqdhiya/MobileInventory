package xriz.my.id.mobileinventory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import xriz.my.id.mobileinventory.model.Riwayat

class RiwayatAdapter : RecyclerView.Adapter<RiwayatAdapter.RiwayatViewHolder>() {

    private val riwayatList: MutableList<Riwayat> = mutableListOf()

    class RiwayatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val jenisTransaksi: TextView = itemView.findViewById(R.id.tvJenisTransaksi)
        val jumlah: TextView = itemView.findViewById(R.id.tvJumlah)
        val tanggal: TextView = itemView.findViewById(R.id.tvTanggal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RiwayatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_riwayat, parent, false)
        return RiwayatViewHolder(view)
    }

    override fun onBindViewHolder(holder: RiwayatViewHolder, position: Int) {
        val riwayat = riwayatList[position]
        holder.jenisTransaksi.text = riwayat.jenisTransaksi
        holder.jumlah.text = "Jumlah: ${riwayat.jumlah}"
        holder.tanggal.text = riwayat.tanggal
    }

    override fun getItemCount(): Int = riwayatList.size

    // Metode untuk memperbarui data
    fun updateData(newList: List<Riwayat>) {
        riwayatList.clear()
        riwayatList.addAll(newList)
        notifyDataSetChanged()
    }
}

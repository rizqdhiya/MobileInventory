package xriz.my.id.mobileinventory.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "barang")
data class Barang(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var nama: String,
    var deskripsi: String,
    var kategori: String,
    var harga: Double,
    var stok: Int,
    var fotoPath: String
)

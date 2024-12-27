package xriz.my.id.mobileinventory.model

data class Barang(
    var barangId: String? = null,
    var nama: String? = null,
    var deskripsi: String? = null,
    var kategori: String? = null,
    var harga: Double? = null,
    var stok: Int = 0,
    var fotoPath: String? = null,
    var supplierId: String? = null
) {
    // No-argument constructor for Firebase
    constructor() : this(null, null, null, null, null, 0, null, null)
}

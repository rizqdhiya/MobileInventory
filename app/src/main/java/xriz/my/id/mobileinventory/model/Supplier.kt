package xriz.my.id.mobileinventory.model

data class Supplier(
    var id: String? = null,  // ID supplier dari Firebase
    var nama: String? = null,
    var alamat: String? = null,
    var kontak: String? = null,
    var lokasi: Lokasi? = null
)

data class Lokasi(
    var latitude: Double? = null,
    var longitude: Double? = null
)

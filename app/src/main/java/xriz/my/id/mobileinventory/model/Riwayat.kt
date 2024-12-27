package xriz.my.id.mobileinventory.model

data class Riwayat(
    var barangId: String = "",
    var jenisTransaksi: String = "", // "masuk" atau "keluar"
    var jumlah: Int = 0,
    var tanggal: String = "",
    var id: String = ""
)

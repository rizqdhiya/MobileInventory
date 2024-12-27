package xriz.my.id.mobileinventory

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class EditBarangActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private var barangId: String? = null // ID barang yang sedang diedit
    private var fotoPath: String? = null
    private var imageUri: Uri? = null // Variabel untuk menyimpan Uri foto yang diambil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_barang)

        database = FirebaseDatabase.getInstance().getReference("barang")
        storage = FirebaseStorage.getInstance()

        // Ambil ID barang dari Intent
        barangId = intent.getStringExtra("BARANG_ID")
        if (barangId != null) {
            loadBarangData(barangId!!)
        } else {
            Toast.makeText(this, "Barang tidak valid!", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Tombol pilih foto
        findViewById<Button>(R.id.btnPilihFoto).setOnClickListener {
            val options = arrayOf("Pilih dari Galeri", "Ambil Foto dengan Kamera")
            AlertDialog.Builder(this)
                .setTitle("Pilih Sumber Foto")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> openGallery() // Pilih dari galeri
                        1 -> checkCameraPermissionAndOpenCamera() // Ambil foto dengan kamera
                    }
                }
                .show()
        }

        // Tombol simpan
        findViewById<Button>(R.id.btnSimpanBarang).setOnClickListener {
            simpanBarang()
        }
    }


    private fun simpanBarang() {
        val nama = findViewById<EditText>(R.id.inputNamaBarang).text.toString()
        val deskripsi = findViewById<EditText>(R.id.inputDeskripsi).text.toString()
        val kategori = findViewById<EditText>(R.id.inputKategori).text.toString()
        val hargaStr = findViewById<EditText>(R.id.inputHarga).text.toString()
        val stokStr = findViewById<EditText>(R.id.inputStok).text.toString()

        if (nama.isBlank() || deskripsi.isBlank() || kategori.isBlank() || hargaStr.isBlank() || stokStr.isBlank()) {
            Toast.makeText(this, "Harap isi semua data dengan benar!", Toast.LENGTH_SHORT).show()
            return
        }

        val harga = hargaStr.toDoubleOrNull() ?: 0.0
        val stok = stokStr.toIntOrNull() ?: 0

        // Menentukan path foto
        fotoPath = imageUri?.toString() ?: fotoPath

        // Validasi fotoPath
        if (fotoPath.isNullOrBlank()) {
            Toast.makeText(this, "Harap pilih foto!", Toast.LENGTH_SHORT).show()
            return
        }

        val barangData = mapOf(
            "nama" to nama,
            "deskripsi" to deskripsi,
            "kategori" to kategori,
            "harga" to harga,
            "stok" to stok,
            "fotoPath" to fotoPath
        )

        barangId?.let {
            database.child(it).updateChildren(barangData).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Barang berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Gagal memperbarui barang", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadBarangData(id: String) {
        database.child(id).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val nama = snapshot.child("nama").value?.toString()
                val deskripsi = snapshot.child("deskripsi").value?.toString()
                val kategori = snapshot.child("kategori").value?.toString()
                val harga = snapshot.child("harga").value?.toString()
                val stok = snapshot.child("stok").value?.toString()
                fotoPath = snapshot.child("fotoPath").value?.toString()

                findViewById<EditText>(R.id.inputNamaBarang).setText(nama)
                findViewById<EditText>(R.id.inputDeskripsi).setText(deskripsi)
                findViewById<EditText>(R.id.inputKategori).setText(kategori)
                findViewById<EditText>(R.id.inputHarga).setText(harga)
                findViewById<EditText>(R.id.inputStok).setText(stok)

                // Menampilkan gambar jika ada
                if (!fotoPath.isNullOrBlank()) {
                    Glide.with(this).load(fotoPath).into(findViewById(R.id.previewFoto))
                } else {
                    Toast.makeText(this, "Foto tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Barang tidak ditemukan!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal memuat data barang!", Toast.LENGTH_SHORT).show()
        }
    }


    // Fungsi untuk membuka Galeri
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 100)
    }

    // Cek izin kamera dan buka kamera jika diizinkan
    private fun checkCameraPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
        }
    }

    // Fungsi untuk membuka Kamera
    private fun openCamera() {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "New Picture")
            put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        }
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        }
        startActivityForResult(intent, 101)
    }

    // Mengambil hasil dari Galeri atau Kamera
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                100 -> { // Galeri
                    imageUri = data?.data
                    Glide.with(this).load(imageUri).into(findViewById(R.id.previewFoto))
                }
                101 -> { // Kamera
                    Glide.with(this).load(imageUri).into(findViewById(R.id.previewFoto))
                }
            }
        }
    }
}

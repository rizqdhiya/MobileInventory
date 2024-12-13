package xriz.my.id.mobileinventory

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import xriz.my.id.mobileinventory.db.AppDatabase
import xriz.my.id.mobileinventory.db.Barang
import java.text.SimpleDateFormat
import java.util.*
import android.Manifest
import com.bumptech.glide.Glide

class TambahBarangActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private var fotoPath: String? = null
    private val PICK_IMAGE_REQUEST = 1
    private val CAPTURE_IMAGE_REQUEST = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_barang)

        database = AppDatabase.getDatabase(this)

        val namaInput = findViewById<EditText>(R.id.inputNamaBarang)
        val deskripsiInput = findViewById<EditText>(R.id.inputDeskripsi)
        val kategoriInput = findViewById<EditText>(R.id.inputKategori)
        val hargaInput = findViewById<EditText>(R.id.inputHarga)
        val pilihFotoButton = findViewById<Button>(R.id.btnPilihFoto)
        val tambahBarangButton = findViewById<Button>(R.id.btnTambahBarang)
        val previewFoto = findViewById<ImageView>(R.id.previewFoto)

        pilihFotoButton.setOnClickListener {
            val options = arrayOf("Pilih dari Galeri", "Ambil Foto dengan Kamera")
            AlertDialog.Builder(this)
                .setTitle("Pilih Sumber Foto")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> openGallery()
                        1 -> checkCameraPermissionAndOpenCamera() // Cek izin sebelum membuka kamera
                    }
                }
                .show()
        }

        tambahBarangButton.setOnClickListener {
            val namaBarang = namaInput.text.toString()
            val deskripsi = deskripsiInput.text.toString()
            val kategori = kategoriInput.text.toString()
            val harga = hargaInput.text.toString().toDoubleOrNull()

            if (namaBarang.isEmpty() || harga == null) {
                Toast.makeText(this, "Mohon isi semua field yang diperlukan.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val existingBarang = database.barangDao().getBarangByName(namaBarang)
                if (existingBarang != null) {
                    Toast.makeText(this@TambahBarangActivity, "Barang dengan nama ini sudah ada.", Toast.LENGTH_SHORT).show()
                } else {
                    val newBarang = Barang(
                        nama = namaBarang,
                        deskripsi = deskripsi,
                        kategori = kategori,
                        harga = harga,
                        stok = 0, // Default stok diset ke 0
                        fotoPath = fotoPath ?: ""
                    )
                    database.barangDao().insertBarang(newBarang)
                    Toast.makeText(this@TambahBarangActivity, "Barang berhasil ditambahkan.", Toast.LENGTH_SHORT).show()
                }
                finish()
            }
        }
    }

    private fun checkCameraPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        } else {
            openCamera()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Izin kamera ditolak", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAPTURE_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            try {
                when (requestCode) {
                    PICK_IMAGE_REQUEST -> {
                        val selectedImage: Uri? = data?.data
                        if (selectedImage != null) {
                            fotoPath = getFilePathFromUri(selectedImage)
                            Glide.with(this).load(fotoPath).into(findViewById(R.id.previewFoto))
                        } else {
                            throw Exception("Gambar tidak valid")
                        }
                    }
                    CAPTURE_IMAGE_REQUEST -> {
                        val imageBitmap = data?.extras?.get("data") as? Bitmap
                        val uri = saveImageToGallery(imageBitmap)
                        if (uri != null) {
                            fotoPath = getFilePathFromUri(uri)
                            Glide.with(this).load(fotoPath).into(findViewById(R.id.previewFoto))
                        } else {
                            throw Exception("Gagal menyimpan gambar")
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveImageToGallery(bitmap: Bitmap?): Uri? {
        if (bitmap == null) return null

        val filename = "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.jpg"
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MobileInventory")
        }

        val resolver = contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        uri?.let {
            resolver.openOutputStream(it)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
        }

        return uri
    }

    private fun getFilePathFromUri(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            return cursor.getString(columnIndex)
        }
        return null
    }
}

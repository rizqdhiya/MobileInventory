package xriz.my.id.mobileinventory
import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import xriz.my.id.mobileinventory.db.AppDatabase
import xriz.my.id.mobileinventory.db.Barang
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditBarangActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private var barangId: Int = -1 // ID barang yang sedang diedit
    private var fotoPath: String? = null
    private val PICK_IMAGE_REQUEST = 1
    private val CAPTURE_IMAGE_REQUEST = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_barang)

        database = AppDatabase.getDatabase(this)

        // Ambil ID barang dari Intent
        barangId = intent.getIntExtra("barangId", -1)

        if (barangId != -1) {
            // Load data barang berdasarkan ID
            loadBarangData(barangId)
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
                        0 -> openGallery()
                        1 -> checkCameraPermissionAndOpenCamera() // Cek izin sebelum membuka kamera
                    }
                }
                .show()
        }

        // Tombol simpan
        findViewById<Button>(R.id.btnSimpanBarang).setOnClickListener {
            simpanBarang()
        }
    }

    private fun loadBarangData(id: Int) {
        lifecycleScope.launch {
            val barang = database.barangDao().getBarangById(id)
            if (barang != null) {
                // Isi data barang ke dalam form
                findViewById<EditText>(R.id.inputNamaBarang).setText(barang.nama)
                findViewById<EditText>(R.id.inputDeskripsi).setText(barang.deskripsi)
                findViewById<EditText>(R.id.inputKategori).setText(barang.kategori)
                findViewById<EditText>(R.id.inputHarga).setText(barang.harga.toString())
                findViewById<EditText>(R.id.inputStok).setText(barang.stok.toString())
                fotoPath = barang.fotoPath
                if (fotoPath != null) {
                    Glide.with(this@EditBarangActivity).load(fotoPath).into(findViewById(R.id.previewFoto))
                }
            } else {
                Toast.makeText(this@EditBarangActivity, "Barang tidak ditemukan!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun simpanBarang() {
        val nama = findViewById<EditText>(R.id.inputNamaBarang).text.toString()
        val deskripsi = findViewById<EditText>(R.id.inputDeskripsi).text.toString()
        val kategori = findViewById<EditText>(R.id.inputKategori).text.toString()
        val harga = findViewById<EditText>(R.id.inputHarga).text.toString().toDoubleOrNull()
        val stok = findViewById<EditText>(R.id.inputStok).text.toString().toIntOrNull()

        if (nama.isBlank() || deskripsi.isBlank() || kategori.isBlank() || harga == null || stok == null) {
            Toast.makeText(this, "Harap isi semua data dengan benar!", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val barang = Barang(
                id = barangId,
                nama = nama,
                deskripsi = deskripsi,
                kategori = kategori,
                harga = harga,
                stok = stok,
                fotoPath = fotoPath ?: ""
            )

            database.barangDao().updateBarang(barang) // Update barang di database
            Toast.makeText(this@EditBarangActivity, "Barang berhasil disimpan!", Toast.LENGTH_SHORT).show()

            // Kembali ke MainActivity
            val intent = Intent(this@EditBarangActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    // Menangani izin kamera
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

    // Fungsi untuk memilih foto dari galeri
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // Fungsi untuk membuka kamera
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAPTURE_IMAGE_REQUEST)
    }

    // Menangani hasil dari galeri dan kamera
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

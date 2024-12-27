package xriz.my.id.mobileinventory

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import xriz.my.id.mobileinventory.model.Barang
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class TambahBarangActivity : AppCompatActivity() {
    private lateinit var database: FirebaseDatabase
    private lateinit var barangRef: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference

    private val PICK_IMAGE_REQUEST = 1
    private val CAPTURE_IMAGE_REQUEST = 2
    private val REQUEST_CODE_STORAGE_PERMISSION = 100
    private val REQUEST_CODE_CAMERA_PERMISSION = 101
    private var fotoPath: String? = null

    private lateinit var supplierSpinner: Spinner
    private lateinit var supplierList: MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah_barang)

        // Firebase initialization
        database = FirebaseDatabase.getInstance()
        barangRef = database.getReference("barang")
        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference

        val namaInput = findViewById<EditText>(R.id.inputNamaBarang)
        val deskripsiInput = findViewById<EditText>(R.id.inputDeskripsi)
        val kategoriInput = findViewById<EditText>(R.id.inputKategori)
        val hargaInput = findViewById<EditText>(R.id.inputHarga)
        val pilihFotoButton = findViewById<Button>(R.id.btnPilihFoto)
        val tambahBarangButton = findViewById<Button>(R.id.btnTambahBarang)
        val previewFoto = findViewById<ImageView>(R.id.previewFoto)
        supplierSpinner = findViewById(R.id.supplierSpinner)

        supplierList = mutableListOf()
        loadSuppliers()

        pilihFotoButton.setOnClickListener {
            val options = arrayOf("Pilih dari Galeri", "Ambil Foto dengan Kamera")
            AlertDialog.Builder(this)
                .setTitle("Pilih Sumber Foto")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> checkStoragePermissionAndOpenGallery()
                        1 -> checkCameraPermissionAndOpenCamera()
                    }
                }
                .show()
        }

        tambahBarangButton.setOnClickListener {
            val namaBarang = namaInput.text.toString()
            val deskripsi = deskripsiInput.text.toString()
            val kategori = kategoriInput.text.toString()
            val harga = hargaInput.text.toString().toDoubleOrNull()
            val supplier = supplierSpinner.selectedItem.toString()

            if (namaBarang.isEmpty() || harga == null || supplier.isEmpty()) {
                Toast.makeText(this, "Mohon isi semua field yang diperlukan.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            fotoPath?.let { path ->
                val fotoUri = Uri.fromFile(File(path))
                val fotoRef = storageRef.child("barang_images/${fotoUri.lastPathSegment}")
                fotoRef.putFile(fotoUri).addOnSuccessListener {
                    fotoRef.downloadUrl.addOnSuccessListener { uri ->
                        val newBarang = Barang(
                            nama = namaBarang,
                            deskripsi = deskripsi,
                            kategori = kategori,
                            harga = harga,
                            stok = 0, // Default stok
                            fotoPath = uri.toString(),
                            supplierId = supplier
                        )
                        val barangId = barangRef.push().key
                        if (barangId != null) {
                            barangRef.child(barangId).setValue(newBarang).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this, "Barang berhasil ditambahkan.", Toast.LENGTH_SHORT).show()
                                    finish()
                                } else {
                                    Toast.makeText(this, "Gagal menambahkan barang.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Gagal mengunggah foto.", Toast.LENGTH_SHORT).show()
                }
            } ?: Toast.makeText(this, "Silakan pilih foto terlebih dahulu.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadSuppliers() {
        val suppliersRef = database.getReference("supplier")
        suppliersRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                supplierList.clear()
                snapshot.children.forEach { supplierSnapshot ->
                    val supplierName = supplierSnapshot.child("nama").getValue(String::class.java)
                    supplierName?.let { supplierList.add(it) }
                }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, supplierList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                supplierSpinner.adapter = adapter
            }
        }
    }

    private fun checkStoragePermissionAndOpenGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_STORAGE_PERMISSION)
        } else {
            openGallery()
        }
    }

    private fun checkCameraPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CODE_CAMERA_PERMISSION)
        } else {
            openCamera()
        }
    }

    private fun openGallery() {
        val pickImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickImageIntent, PICK_IMAGE_REQUEST)
    }

    private fun openCamera() {
        val photoFile = createImageFile()
        photoFile?.let {
            val photoURI = FileProvider.getUriForFile(this, "${applicationContext.packageName}.provider", it)
            fotoPath = it.absolutePath
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(cameraIntent, CAPTURE_IMAGE_REQUEST)
        }
    }

    private fun createImageFile(): File? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            File.createTempFile("IMG_$timeStamp", ".jpg", storageDir)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    val selectedImage: Uri? = data?.data
                    selectedImage?.let {
                        fotoPath = getFilePathFromUri(it)
                        Glide.with(this).load(it).into(findViewById(R.id.previewFoto))
                    }
                }
                CAPTURE_IMAGE_REQUEST -> {
                    fotoPath?.let {
                        Glide.with(this).load(File(it)).into(findViewById(R.id.previewFoto))
                    }
                }
            }
        }
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

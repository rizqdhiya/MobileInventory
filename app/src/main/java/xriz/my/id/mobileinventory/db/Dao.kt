/*package xriz.my.id.mobileinventory.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface BarangDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBarang(barang: Barang)

    @Query("SELECT * FROM barang WHERE nama = :nama LIMIT 1")
    suspend fun getBarangByName(nama: String): Barang?

    @Query("SELECT * FROM barang WHERE id = :id LIMIT 1")
    suspend fun getBarangById(id: Int): Barang?

    @Query("SELECT * FROM barang")
    fun getAllBarang(): LiveData<List<Barang>>

    @Update
    suspend fun updateBarang(barang: Barang)

    @Delete
    suspend fun deleteBarang(barang: Barang)
}

@Dao
interface RiwayatDao {

    @Insert
    suspend fun insertRiwayat(riwayat: Riwayat)

    @Query("SELECT * FROM riwayat ORDER BY tanggal DESC")
    suspend fun getAllRiwayat(): List<Riwayat>
}
*/
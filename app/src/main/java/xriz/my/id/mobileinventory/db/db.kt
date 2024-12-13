package xriz.my.id.mobileinventory.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Barang::class, Riwayat::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun barangDao(): BarangDao
    abstract fun riwayatDao(): RiwayatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "inventory_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
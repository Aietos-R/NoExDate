package org.umn.noexdate.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.umn.noexdate.data.local.dao.ExpenseDao
import org.umn.noexdate.data.local.dao.NoteDao
import org.umn.noexdate.data.local.dao.PantryDao
import org.umn.noexdate.data.local.entity.ExpenseEntity
import org.umn.noexdate.data.local.entity.NoteEntity
import org.umn.noexdate.data.local.entity.PantryEntity

@Database(entities = [NoteEntity::class, PantryEntity::class, ExpenseEntity::class], version = 6, exportSchema = false)
abstract class NoexDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun pantryDao(): PantryDao
    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: NoexDatabase? = null


        // Skrip Migrasi dari versi 3 ke 4 (Update 2)
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notes ADD COLUMN noteType TEXT NOT NULL DEFAULT 'Note Only'")
                db.execSQL("ALTER TABLE notes ADD COLUMN showOnCalendar INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE notes ADD COLUMN reminderEnabled INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE notes ADD COLUMN deadlineAt INTEGER")
                db.execSQL("ALTER TABLE notes ADD COLUMN reminderConfiguration TEXT")
                db.execSQL("ALTER TABLE notes ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
            }
        }
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notes ADD COLUMN imageUri TEXT")
            }
        }
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notes ADD COLUMN recurrence TEXT NOT NULL DEFAULT 'Tidak berulang'")
            }
        }

        fun getDatabase(context: Context): NoexDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoexDatabase::class.java,
                    "noexdate_db"
                )
                    .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6) // Pasang migrasi agar data tidak dihapus
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
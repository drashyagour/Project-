package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        StudySessionEntity::class,
        MockTestEntity::class,
        SyllabusChapterEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class NeetDatabase : RoomDatabase() {
    abstract fun studySessionDao(): StudySessionDao
    abstract fun mockTestDao(): MockTestDao
    abstract fun syllabusDao(): SyllabusDao

    companion object {
        @Volatile
        private var INSTANCE: NeetDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE syllabus_chapters ADD COLUMN isCompleted INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        fun getInstance(context: Context): NeetDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    NeetDatabase::class.java,
                    "neet_pulse.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

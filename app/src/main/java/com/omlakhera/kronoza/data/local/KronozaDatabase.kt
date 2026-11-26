package com.omlakhera.kronoza.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        EventEntity::class,
        TaskEntity::class,
        NotificationEntity::class,
        ReminderConfigEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(KronozaConverters::class)
abstract class KronozaDatabase : RoomDatabase() {
    abstract fun kronozaDao(): KronozaDao

    companion object {
        const val DB_NAME = "kronoza_db"

        @Volatile
        private var INSTANCE: KronozaDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE events ADD COLUMN endDateEpochDay INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE events ADD COLUMN seriesId TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE events ADD COLUMN excludedEpochDays TEXT NOT NULL DEFAULT '[]'")
                db.execSQL("ALTER TABLE events ADD COLUMN customColorHex TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE events ADD COLUMN updatedAtMillis INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE events SET updatedAtMillis = createdAtMillis WHERE updatedAtMillis = 0")

                db.execSQL("ALTER TABLE tasks ADD COLUMN updatedAtMillis INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE tasks SET updatedAtMillis = createdAtMillis WHERE updatedAtMillis = 0")

                db.execSQL("CREATE INDEX IF NOT EXISTS index_events_dateEpochDay ON events(dateEpochDay)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_events_seriesId ON events(seriesId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_dueEpochDay ON tasks(dueEpochDay)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_completed ON tasks(completed)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_targetType_targetId ON reminders(targetType, targetId)")
            }
        }

        fun getInstance(context: Context): KronozaDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    KronozaDatabase::class.java,
                    DB_NAME
                )
                    .addMigrations(MIGRATION_2_3)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

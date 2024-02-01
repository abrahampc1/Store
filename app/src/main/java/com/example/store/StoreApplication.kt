package com.example.store

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class StoreApplication : Application() {
    companion object{
        lateinit var database: StoreDatabase
    }

    override fun onCreate() {
        super.onCreate()

        val MIGRATION_1_2 = object : Migration(1, 2){
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE StoreEntity ADD COLUMN photoUrl TEXT NOT NULL DEFAULT ''")
            }
        }

        database = Room.databaseBuilder(this,
            StoreDatabase::class.java,
            "StoreDataBase")
            .addMigrations(MIGRATION_1_2)
            .build()

    }
}
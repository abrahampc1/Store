package com.example.store

import android.app.Application
import androidx.room.Room

class StoreApplication : Application() {
    companion object{
        lateinit var database: StoreDatabase
    }

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(this,
            StoreDatabase::class.java,
            "StoreDataBase").build()
    }
}
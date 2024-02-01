package com.example.store

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "StoreEntity")
data class StoreEntity(@PrimaryKey(autoGenerate = true) var id:Long = 0,
                       var Name:String,
                       var Phone:String,
                       var WebSite: String = "",
                       var photoUrl : String,
                       var isFavorite: Boolean = false)
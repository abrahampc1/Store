package com.example.store

data class Store(var id:Long = 0,
                 var Name:String,
                 var Phone:String = "",
                 var WebSite: String = "",
                 var isFavorite: Boolean = false)
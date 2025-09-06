package com.example.comp2008assignment1

data class Park(
    val name: String,
    val location: String,
    val activities: List<String>,
    val features: List<String>,
    var isFavourite: Boolean = false
)
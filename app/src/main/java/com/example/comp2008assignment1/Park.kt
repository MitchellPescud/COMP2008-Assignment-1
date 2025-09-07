package com.example.comp2008assignment1

// Park Data Class

data class Park(
    val parkID: String,
    val name: String,
    val location: String,
    val activities: List<String>,
    val features: List<String>,
    var isFavourite: Boolean = false
)
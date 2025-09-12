package com.example.comp2008assignment1

// Park Data Class

data class Park(
    val parkID: String,
    val name: String,
    val location: String,
    val activities: List<String>,
    val features: List<String>,
    val description: String = "",
    val imageUrl: String = "",
    val rating: Float = 0f,
    var isFavourite: Boolean = false
)
package com.example.brokeassistant.core.domain.model

data class Category(
    val id: Long = 0,
    val name: String,
    val percentage: Int, // 0 to 100
    val isDefaultFree: Boolean = false
)

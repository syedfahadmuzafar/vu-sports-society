package com.vusports.bc220200768.models

data class GlobalNotification(
    val id: String = "",
    val message: String = "",
    val audience: String = "all", // "all", "participant", "coach"
    val timestamp: Long = 0,
    val expirationTime: Long = 0
)
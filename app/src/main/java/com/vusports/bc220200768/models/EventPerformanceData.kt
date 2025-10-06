package com.vusports.bc220200768.models

/**
 * Data class representing a participant's performance in an event
 */
data class EventPerformanceData(
    val eventId: String = "",
    val participantId: String = "",
    val participantEmail: String = "",
    val participantName: String = "",
    val score: Int = 0,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Data class representing a participant with their event performance data
 */
data class ParticipantEventData(
    val participantId: String = "",
    val participantEmail: String = "",
    val participantName: String = "",
    val currentScore: Int = 0,
    val currentNotes: String = "",
    val hasExistingRecord: Boolean = false
)
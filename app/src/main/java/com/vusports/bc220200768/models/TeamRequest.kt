package com.vusports.bc220200768.models

data class TeamRequest(
    val id: String = "",
    val teamId: String = "",
    val teamName: String = "",
    val sport: String = "",
    val requesterId: String = "",
    val requesterName: String = "",
    val status: String = "pending", // pending, approved, rejected
    val rejectionReason: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isTeamLeader: Boolean = false
)

data class TeamMember(
    val email: String = "",
    val name: String = "",
    val status: String = "pending", // pending, approved, rejected
    val rejectionReason: String = "",
    val isTeamLeader: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class Team(
    val id: String = "",
    val name: String = "",
    val sport: String = "",
    val creator: String = "",
    val members: List<TeamMember> = emptyList(),
    val status: String = "pending", // pending, approved, rejected
    val coachApproved: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
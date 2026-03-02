package com.emul8r.bizap.domain.model

data class Note(
    val id: Long = 0,
    val businessProfileId: Long,
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)

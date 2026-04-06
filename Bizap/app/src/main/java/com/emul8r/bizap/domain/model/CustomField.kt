package com.emul8r.bizap.domain.model

data class CustomField(
    val id: String,
    val templateId: String,
    val label: String,
    val fieldType: String,
    val isRequired: Boolean = false,
    val displayOrder: Int,
    val isActive: Boolean = true
)

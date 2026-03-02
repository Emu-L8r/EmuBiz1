package com.emul8r.bizap.domain.model

data class Customer(
    val id: Long = 0,
    val name: String,
    val businessName: String? = null,
    val businessNumber: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun validate() {
        require(name.isNotBlank()) { "Customer name is required" }
        require(name.length <= 100) { "Name must be 100 characters or less" }
        
        email?.let { 
            if (it.isNotBlank()) {
                require(it.contains("@") && it.contains(".")) { "Invalid email format" }
            }
        }
        
        phone?.let {
            if (it.isNotBlank()) {
                require(it.length in 5..20) { "Phone must be between 5 and 20 characters" }
            }
        }
    }
}


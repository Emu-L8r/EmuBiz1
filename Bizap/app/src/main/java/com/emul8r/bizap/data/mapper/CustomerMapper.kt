package com.emul8r.bizap.data.mapper

import com.emul8r.bizap.data.local.entities.CustomerEntity
import com.emul8r.bizap.domain.model.Customer

fun Customer.toEntity(): CustomerEntity {
    return CustomerEntity(
        id = this.id,
        name = this.name,
        businessName = this.businessName,
        businessNumber = this.businessNumber,
        email = this.email,
        phone = this.phone,
        address = this.address,
        notes = this.notes,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

fun CustomerEntity.toDomain(): Customer {
    return Customer(
        id = this.id,
        name = this.name,
        businessName = this.businessName,
        businessNumber = this.businessNumber,
        email = this.email,
        phone = this.phone,
        address = this.address,
        notes = this.notes,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

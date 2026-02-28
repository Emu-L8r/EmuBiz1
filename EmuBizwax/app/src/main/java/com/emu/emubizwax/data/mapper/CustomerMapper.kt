package com.emu.emubizwax.data.mapper

import com.emu.emubizwax.data.local.entities.CustomerEntity
import com.emu.emubizwax.domain.model.Customer
import javax.inject.Inject

class CustomerMapper @Inject constructor() {

    fun toDomain(entity: CustomerEntity): Customer {
        return Customer(
            id = entity.id,
            name = entity.name,
            businessName = entity.businessName,
            businessNumber = entity.businessNumber,
            address = entity.address,
            phone = entity.phone,
            email = entity.email
        )
    }

    fun toEntity(domain: Customer): CustomerEntity {
        return CustomerEntity(
            id = domain.id,
            name = domain.name,
            businessName = domain.businessName,
            businessNumber = domain.businessNumber,
            address = domain.address,
            phone = domain.phone,
            email = domain.email
        )
    }
}

package com.emul8r.bizap.data.mapper

import com.emul8r.bizap.data.local.entities.CustomerEntity
import com.emul8r.bizap.domain.model.Customer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Unit tests for CustomerMapper.
 * Tests data layer to domain layer transformations.
 */
class CustomerMapperTest {

    @Test
    fun customer_toEntity_shouldPreserveAllFields() {
        val customer = Customer(
            id = 42L,
            name = "John Doe",
            businessName = "Acme Corp",
            businessNumber = "ABN123456",
            email = "john@example.com",
            phone = "0412345678",
            address = "123 Main St",
            notes = "VIP customer",
            createdAt = 1000L,
            updatedAt = 2000L
        )

        val entity = customer.toEntity()

        assertEquals(42L, entity.id)
        assertEquals("John Doe", entity.name)
        assertEquals("Acme Corp", entity.businessName)
        assertEquals("ABN123456", entity.businessNumber)
        assertEquals("john@example.com", entity.email)
        assertEquals("0412345678", entity.phone)
        assertEquals("123 Main St", entity.address)
        assertEquals("VIP customer", entity.notes)
        assertEquals(1000L, entity.createdAt)
        assertEquals(2000L, entity.updatedAt)
    }

    @Test
    fun customerEntity_toDomain_shouldPreserveAllFields() {
        val entity = CustomerEntity(
            id = 42L,
            businessProfileId = 1L,
            name = "Jane Smith",
            businessName = "Tech Solutions",
            businessNumber = "ABN654321",
            email = "jane@example.com",
            phone = "0487654321",
            address = "456 Oak Ave",
            notes = "Loyal customer",
            createdAt = 1500L,
            updatedAt = 2500L
        )

        val customer = entity.toDomain()

        assertEquals(42L, customer.id)
        assertEquals("Jane Smith", customer.name)
        assertEquals("Tech Solutions", customer.businessName)
        assertEquals("ABN654321", customer.businessNumber)
        assertEquals("jane@example.com", customer.email)
        assertEquals("0487654321", customer.phone)
        assertEquals("456 Oak Ave", customer.address)
        assertEquals("Loyal customer", customer.notes)
        assertEquals(1500L, customer.createdAt)
        assertEquals(2500L, customer.updatedAt)
    }

    @Test
    fun roundTrip_customer_toEntity_toDomain_shouldPreserveEquality() {
        val original = Customer(
            id = 42L,
            name = "John Doe",
            businessName = "Acme Corp",
            businessNumber = "ABN123456",
            email = "john@example.com",
            phone = "0412345678",
            address = "123 Main St",
            notes = "VIP customer",
            createdAt = 1000L,
            updatedAt = 2000L
        )

        val roundTrip = original.toEntity().toDomain()

        assertEquals(original, roundTrip)
    }

    @Test
    fun roundTrip_entity_toDomain_toEntity_shouldPreserveEquality() {
        val original = CustomerEntity(
            id = 42L,
            businessProfileId = 1L,
            name = "Jane Smith",
            businessName = "Tech Solutions",
            businessNumber = "ABN654321",
            email = "jane@example.com",
            phone = "0487654321",
            address = "456 Oak Ave",
            notes = "Loyal customer",
            createdAt = 1500L,
            updatedAt = 2500L
        )

        val roundTrip = original.toDomain().toEntity()

        // Note: businessProfileId is not part of Customer domain model, so compare other fields
        assertEquals(original.id, roundTrip.id)
        assertEquals(original.name, roundTrip.name)
        assertEquals(original.businessName, roundTrip.businessName)
        assertEquals(original.businessNumber, roundTrip.businessNumber)
        assertEquals(original.email, roundTrip.email)
        assertEquals(original.phone, roundTrip.phone)
        assertEquals(original.address, roundTrip.address)
        assertEquals(original.notes, roundTrip.notes)
        assertEquals(original.createdAt, roundTrip.createdAt)
        assertEquals(original.updatedAt, roundTrip.updatedAt)
    }

    @Test
    fun toEntity_withNullFields_shouldHandleGracefully() {
        val customer = Customer(
            id = 1L,
            name = "Simple Customer",
            businessName = null,
            businessNumber = null,
            email = null,
            phone = null,
            address = null,
            notes = "",
            createdAt = 1000L,
            updatedAt = 2000L
        )

        val entity = customer.toEntity()

        assertNotNull(entity)
        assertEquals("Simple Customer", entity.name)
        assertEquals(null, entity.businessName)
        assertEquals(null, entity.email)
    }

    @Test
    fun toDomain_withNullFields_shouldHandleGracefully() {
        val entity = CustomerEntity(
            id = 1L,
            businessProfileId = 1L,
            name = "Simple Entity",
            businessName = null,
            businessNumber = null,
            email = null,
            phone = null,
            address = null,
            notes = "",
            createdAt = 1000L,
            updatedAt = 2000L
        )

        val customer = entity.toDomain()

        assertNotNull(customer)
        assertEquals("Simple Entity", customer.name)
        assertEquals(null, customer.businessName)
        assertEquals(null, customer.email)
    }

    @Test
    fun toEntity_withEmptyStrings_shouldPreserve() {
        val customer = Customer(
            id = 1L,
            name = "",
            businessName = "",
            businessNumber = "",
            email = "",
            phone = "",
            address = "",
            notes = "",
            createdAt = 0L,
            updatedAt = 0L
        )

        val entity = customer.toEntity()

        assertEquals("", entity.name)
        assertEquals("", entity.businessName)
        assertEquals("", entity.email)
        assertEquals("", entity.notes)
    }

    @Test
    fun toDomain_withSpecialCharacters_shouldPreserve() {
        val entity = CustomerEntity(
            id = 1L,
            businessProfileId = 1L,
            name = "José García Müller",
            businessName = "Café & Co.",
            email = "test+alias@example.com",
            phone = "+61 (0)2 1234 5678",
            address = "Level 3, 100-200 O'Connell St",
            notes = "Notes with @#$%^&*()",
            createdAt = 1000L,
            updatedAt = 2000L
        )

        val customer = entity.toDomain()

        assertEquals("José García Müller", customer.name)
        assertEquals("Café & Co.", customer.businessName)
        assertEquals("test+alias@example.com", customer.email)
        assertEquals("Notes with @#$%^&*()", customer.notes)
    }

    @Test
    fun toEntity_preservesIdZero() {
        val customer = Customer(
            id = 0L,
            name = "New Customer",
            email = null,
            createdAt = 1000L,
            updatedAt = 2000L
        )

        val entity = customer.toEntity()

        assertEquals(0L, entity.id)
    }

    @Test
    fun toDomain_preservesLargeTimestamps() {
        val largeTimestamp = 9999999999999L
        val entity = CustomerEntity(
            id = 1L,
            businessProfileId = 1L,
            name = "Test",
            createdAt = largeTimestamp,
            updatedAt = largeTimestamp
        )

        val customer = entity.toDomain()

        assertEquals(largeTimestamp, customer.createdAt)
        assertEquals(largeTimestamp, customer.updatedAt)
    }
}


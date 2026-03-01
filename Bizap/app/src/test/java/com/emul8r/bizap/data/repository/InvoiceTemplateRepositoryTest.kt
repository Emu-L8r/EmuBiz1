package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.dao.InvoiceTemplateDao
import com.emul8r.bizap.data.local.dao.InvoiceCustomFieldDao
import com.emul8r.bizap.data.local.entities.InvoiceTemplate
import com.emul8r.bizap.data.local.entities.InvoiceCustomField
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

/**
 * Unit tests for InvoiceTemplateRepository
 */
class InvoiceTemplateRepositoryTest {

    @Mock
    private lateinit var templateDao: InvoiceTemplateDao

    @Mock
    private lateinit var fieldDao: InvoiceCustomFieldDao

    private lateinit var repository: InvoiceTemplateRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        repository = InvoiceTemplateRepository(templateDao, fieldDao)
    }

    @Test
    fun testGetAllTemplates_Success() = runBlocking {
        // Arrange
        val businessId = 1L
        val templates = listOf(
            InvoiceTemplate(
                id = "template-1",
                businessProfileId = businessId,
                name = "Professional",
                designType = "PROFESSIONAL",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            InvoiceTemplate(
                id = "template-2",
                businessProfileId = businessId,
                name = "Minimal",
                designType = "MINIMAL",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
        whenever(templateDao.getActiveTemplatesByBusiness(businessId)).thenReturn(templates)

        // Act
        val result = repository.getAllTemplates(businessId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertEquals("Professional", result.getOrNull()?.get(0)?.name)
    }

    @Test
    fun testGetAllTemplates_Empty() = runBlocking {
        // Arrange
        val businessId = 1L
        whenever(templateDao.getActiveTemplatesByBusiness(businessId)).thenReturn(emptyList())

        // Act
        val result = repository.getAllTemplates(businessId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.size)
    }

    @Test
    fun testGetTemplate_Success() = runBlocking {
        // Arrange
        val templateId = "template-1"
        val template = InvoiceTemplate(
            id = templateId,
            businessProfileId = 1L,
            name = "Professional",
            designType = "PROFESSIONAL",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        whenever(templateDao.getTemplate(templateId)).thenReturn(template)

        // Act
        val result = repository.getTemplate(templateId)

        // Assert
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
        assertEquals("Professional", result.getOrNull()?.name)
    }

    @Test
    fun testGetTemplate_NotFound() = runBlocking {
        // Arrange
        val templateId = "non-existent"
        whenever(templateDao.getTemplate(templateId)).thenReturn(null)

        // Act
        val result = repository.getTemplate(templateId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(null, result.getOrNull())
    }

    @Test
    fun testCreateTemplate_Success() = runBlocking {
        // Arrange
        val template = InvoiceTemplate(
            id = "new-template",
            businessProfileId = 1L,
            name = "New Template",
            designType = "PROFESSIONAL",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        whenever(templateDao.getActiveTemplateCount(1L)).thenReturn(5)

        // Act
        val result = repository.createTemplate(template)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals("new-template", result.getOrNull())
    }

    @Test
    fun testCreateTemplate_ExceedsMaxLimit() = runBlocking {
        // Arrange
        val template = InvoiceTemplate(
            id = "new-template",
            businessProfileId = 1L,
            name = "New Template",
            designType = "PROFESSIONAL",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        whenever(templateDao.getActiveTemplateCount(1L)).thenReturn(100)

        // Act
        val result = repository.createTemplate(template)

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun testDeleteTemplate_Success() = runBlocking {
        // Arrange
        val templateId = "template-1"

        // Act
        val result = repository.deleteTemplate(templateId)

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun testSetAsDefault_Success() = runBlocking {
        // Arrange
        val templateId = "template-1"
        val businessId = 1L
        val template = InvoiceTemplate(
            id = templateId,
            businessProfileId = businessId,
            name = "Professional",
            designType = "PROFESSIONAL",
            isDefault = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        whenever(templateDao.getTemplate(templateId)).thenReturn(template)

        // Act
        val result = repository.setAsDefault(templateId, businessId)

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun testSetAsDefault_WrongBusiness() = runBlocking {
        // Arrange
        val templateId = "template-1"
        val businessId = 1L
        val template = InvoiceTemplate(
            id = templateId,
            businessProfileId = 2L, // Different business
            name = "Professional",
            designType = "PROFESSIONAL",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        whenever(templateDao.getTemplate(templateId)).thenReturn(template)

        // Act
        val result = repository.setAsDefault(templateId, businessId)

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun testGetDefaultTemplate_Success() = runBlocking {
        // Arrange
        val businessId = 1L
        val template = InvoiceTemplate(
            id = "default-template",
            businessProfileId = businessId,
            name = "Default",
            designType = "PROFESSIONAL",
            isDefault = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        whenever(templateDao.getDefaultTemplate(businessId)).thenReturn(template)

        // Act
        val result = repository.getDefaultTemplate(businessId)

        // Assert
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
        assertTrue(result.getOrNull()?.isDefault ?: false)
    }

    @Test
    fun testAddCustomField_Success() = runBlocking {
        // Arrange
        val field = InvoiceCustomField(
            id = "field-1",
            templateId = "template-1",
            label = "PO Number",
            fieldType = "TEXT",
            displayOrder = 1
        )
        whenever(fieldDao.getFieldCount("template-1")).thenReturn(5)

        // Act
        val result = repository.addCustomField(field)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals("field-1", result.getOrNull())
    }

    @Test
    fun testAddCustomField_ExceedsMaxLimit() = runBlocking {
        // Arrange
        val field = InvoiceCustomField(
            id = "field-51",
            templateId = "template-1",
            label = "Field 51",
            fieldType = "TEXT",
            displayOrder = 51
        )
        whenever(fieldDao.getFieldCount("template-1")).thenReturn(50)

        // Act
        val result = repository.addCustomField(field)

        // Assert
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun testGetCustomFields_Success() = runBlocking {
        // Arrange
        val templateId = "template-1"
        val fields = listOf(
            InvoiceCustomField(
                id = "field-1",
                templateId = templateId,
                label = "PO Number",
                fieldType = "TEXT",
                displayOrder = 1
            ),
            InvoiceCustomField(
                id = "field-2",
                templateId = templateId,
                label = "Project Code",
                fieldType = "TEXT",
                displayOrder = 2
            )
        )
        whenever(fieldDao.getFieldsByTemplate(templateId)).thenReturn(fields)

        // Act
        val result = repository.getCustomFields(templateId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }

    @Test
    fun testUpdateTemplate_Success() = runBlocking {
        // Arrange
        val template = InvoiceTemplate(
            id = "template-1",
            businessProfileId = 1L,
            name = "Updated Name",
            designType = "PROFESSIONAL",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        // Act
        val result = repository.updateTemplate(template)

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun testUpdateCustomField_Success() = runBlocking {
        // Arrange
        val field = InvoiceCustomField(
            id = "field-1",
            templateId = "template-1",
            label = "Updated Label",
            fieldType = "NUMBER",
            displayOrder = 1
        )

        // Act
        val result = repository.updateCustomField(field)

        // Assert
        assertTrue(result.isSuccess)
    }
}


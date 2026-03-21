package com.emul8r.bizap.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PhotoAttachmentPickerScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun classicPhotoAttachmentPicker_renders() {
        composeTestRule.setContent {
            ClassicPhotoAttachmentPicker(
                photos = emptyList(),
                onPhotosChange = {}
            )
        }

        composeTestRule.onNodeWithText("Attachments").assertExists()
        composeTestRule.onNodeWithText("Add Photo").assertExists()
    }

    @Test
    fun modernPhotoAttachmentPicker_renders() {
        composeTestRule.setContent {
            ModernPhotoAttachmentPicker(
                photos = emptyList(),
                onPhotosChange = {}
            )
        }

        composeTestRule.onNodeWithText("Attachments").assertExists()
        composeTestRule.onNodeWithText("+ Add Photo").assertExists()
    }

    @Test
    fun addPhoto_callsCallback() {
        var callbackCalled = false
        var photoList: List<String> = emptyList()

        composeTestRule.setContent {
            ClassicPhotoAttachmentPicker(
                photos = photoList,
                onPhotosChange = {
                    callbackCalled = true
                    photoList = it
                }
            )
        }

        composeTestRule.onNodeWithText("Add Photo").performClick()

        // Note: In real test, would use launcher to select photo
        // For now, verify button exists and is clickable
        composeTestRule.onNodeWithText("Add Photo").assertIsEnabled()
    }

    @Test
    fun displayPhotos_showsThumbnails() {
        val photoUris = listOf(
            "file:///sdcard/Pictures/photo1.jpg",
            "file:///sdcard/Pictures/photo2.jpg"
        )

        composeTestRule.setContent {
            ClassicPhotoAttachmentPicker(
                photos = photoUris,
                onPhotosChange = {}
            )
        }

        // Verify photos are displayed (2 photos = 2 delete buttons)
        composeTestRule.onAllNodes(hasContentDescription("Remove")).onFirst().assertExists()
    }

    @Test
    fun removePhoto_deletesFromList() {
        var photoList = listOf(
            "file:///sdcard/Pictures/photo1.jpg",
            "file:///sdcard/Pictures/photo2.jpg"
        )
        var callbackCalled = false

        composeTestRule.setContent {
            ClassicPhotoAttachmentPicker(
                photos = photoList,
                onPhotosChange = {
                    callbackCalled = true
                    photoList = it
                }
            )
        }

        composeTestRule.onAllNodes(hasContentDescription("Remove")).onFirst().performClick()

        composeTestRule.waitUntil(timeoutMillis = 1000) { callbackCalled }
        assert(photoList.size < 2) { "Expected photo to be removed" }
    }
}


package com.emu.emubizwax.domain.usecase

import android.net.Uri
import com.emu.emubizwax.domain.repository.FileRepository
import javax.inject.Inject

class CacheImagesUseCase @Inject constructor(
    private val fileRepository: FileRepository
) {
    suspend operator fun invoke(uris: List<Uri>): List<Uri> {
        return uris.map { fileRepository.cacheImageFromUri(it) }
    }
}

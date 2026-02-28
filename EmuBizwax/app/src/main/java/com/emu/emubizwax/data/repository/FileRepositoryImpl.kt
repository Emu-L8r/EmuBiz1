package com.emu.emubizwax.data.repository

import android.content.Context
import android.net.Uri
import com.emu.emubizwax.domain.repository.FileRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject

class FileRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : FileRepository {

    override suspend fun cacheImageFromUri(uri: Uri): Uri {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "${UUID.randomUUID()}.jpg")
        val outputStream = file.outputStream()
        inputStream?.copyTo(outputStream)
        return Uri.fromFile(file)
    }
}

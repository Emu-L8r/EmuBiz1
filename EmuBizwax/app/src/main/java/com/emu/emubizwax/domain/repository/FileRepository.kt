package com.emu.emubizwax.domain.repository

import android.net.Uri

interface FileRepository {
    suspend fun cacheImageFromUri(uri: Uri): Uri
}

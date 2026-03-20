package com.emul8r.bizap.ui.invoices

import com.emul8r.bizap.data.local.entities.LineItemEntity
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class LineItemUiModelMapper @Inject constructor() {

    fun toUiModel(entity: LineItemEntity, type: String, clientName: String, date: Long, counter: Int): LineItemUiModel {
        val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(date))
        return LineItemUiModel(
            originalId = entity.id,
            type = type,
            clientName = clientName,
            date = formattedDate,
            counter = counter
        )
    }
}

package org.fosser.app.data.local

import androidx.room.TypeConverter
import org.fosser.app.domain.model.HistoryAction

class HistoryActionConverter {
    @TypeConverter
    fun fromAction(action: HistoryAction): String = action.name

    @TypeConverter
    fun toAction(value: String): HistoryAction = HistoryAction.valueOf(value)
}

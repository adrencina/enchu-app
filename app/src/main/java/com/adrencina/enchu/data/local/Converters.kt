package com.adrencina.enchu.data.local

import androidx.room.TypeConverter
import com.adrencina.enchu.data.model.SyncState
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromSyncState(value: String?): SyncState? {
        return value?.let { SyncState.valueOf(it) }
    }

    @TypeConverter
    fun syncStateToString(syncState: SyncState?): String? {
        return syncState?.name
    }
}

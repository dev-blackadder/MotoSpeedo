package com.example.motospeedo.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [RideRecordEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun rideDao(): RideDao
}

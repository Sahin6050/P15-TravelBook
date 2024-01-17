package com.sahin.kotlinmaps2.model

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sahin.kotlinmaps2.roomdb.PlaceDao

@Database(entities = [Place::class], version = 1)
abstract class PlaceDatabase : RoomDatabase() {
    abstract fun placeDao(): PlaceDao
}
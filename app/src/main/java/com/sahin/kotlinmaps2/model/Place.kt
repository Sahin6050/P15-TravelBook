package com.sahin.kotlinmaps2.model

import androidx.room.AutoMigration
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

// bir varlık yani sınıf objesi oluştur.
@Entity
class Place(
    // kolon isimlerini ver.
    @ColumnInfo(name = "name")
    var name : String,
    @ColumnInfo(name = "latitude")
    var latitude : Double,
    @ColumnInfo(name = "longitude")
    var longitude : Double
) : Serializable {
    // otomatik olarak id artır.
    @PrimaryKey(autoGenerate = true)
    var id = 0
}
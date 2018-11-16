package ar.edu.unq.dataowl.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * Created by wolfx on 16/11/2018.
 */
@Entity(tableName = "LocationUpdate")
class LocationUpdate(
        @PrimaryKey(autoGenerate = true) var id: Long?,
        @ColumnInfo(name = "lat")  var lat: String,
        @ColumnInfo(name = "lng")  var lng: String) {

    constructor(): this(null, "", "")

}
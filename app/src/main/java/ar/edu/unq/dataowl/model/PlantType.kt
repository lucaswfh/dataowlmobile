package ar.edu.unq.dataowl.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * Created by wolfx on 11/11/2018.
 */
@Entity(tableName = "PlantType")
class PlantType(
        @PrimaryKey(autoGenerate = true) var id: Long?,
        @ColumnInfo(name = "plantType")  var plantType: String) {

    constructor(): this(null, "")

}
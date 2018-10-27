package ar.edu.unq.dataowl.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.TypeConverters
import android.arch.persistence.room.PrimaryKey

/**
 * Created by wolfx on 18/08/2018.
 */
@Entity(tableName = "PostPackage")
@TypeConverters(GithubTypeConverters::class)
class PostPackage (

        @PrimaryKey(autoGenerate = true) var id: Long?,
        @TypeConverters var images: List<String>,
        @ColumnInfo(name = "lat") var lat: String?,
        @ColumnInfo(name = "lng") var lng: String?,
        @ColumnInfo(name = "type") var type: String,
        @ColumnInfo(name = "sent") var sent: Boolean

){

    constructor(): this(null, listOf(""), null, null, "", false)

}
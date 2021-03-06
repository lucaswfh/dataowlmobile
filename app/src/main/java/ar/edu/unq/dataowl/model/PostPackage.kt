package ar.edu.unq.dataowl.model

import android.arch.persistence.room.*
import android.content.Context
import ar.edu.unq.dataowl.persistence.AppDatabase

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

    fun persist(context: Context) {
        val db: AppDatabase = AppDatabase.getInstance(context) as AppDatabase

        val postPackageDao = db.postPackageDao()
        postPackageDao.insert(this)
    }

    fun deletePost(context: Context) {
        val db: AppDatabase = AppDatabase.getInstance(context) as AppDatabase

        val postPackageDao = db.postPackageDao()
        postPackageDao.insert(this)
    }

    fun prepareToUpload(context: Context): PostPackageUpload {
        val imgs: List<String> = this.images.map { i -> ImageHandler().getBase64FromLocation(context, i) }
        val ret= PostPackageUpload(imgs, lat, lng, type)
        return ret
    }

    fun delete(context: Context) {
        val db: AppDatabase = AppDatabase.getInstance(context) as AppDatabase
        val postPackageDao = db.postPackageDao()
        postPackageDao.delete(this)
    }

    fun update(context: Context) {
        val db: AppDatabase = AppDatabase.getInstance(context) as AppDatabase
        val postPackageDao = db.postPackageDao()
        postPackageDao.update(this)
    }
}
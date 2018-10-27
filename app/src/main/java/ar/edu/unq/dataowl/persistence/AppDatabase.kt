package ar.edu.unq.dataowl.persistence

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import ar.edu.unq.dataowl.model.PostPackage

/**
 * Created by wolfx on 27/10/2018.
 */
@Database(entities = [PostPackage::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun postPackageDao(): PostPackageDAO
}
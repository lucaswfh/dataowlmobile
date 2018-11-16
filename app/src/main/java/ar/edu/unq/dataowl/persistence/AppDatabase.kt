package ar.edu.unq.dataowl.persistence

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import ar.edu.unq.dataowl.model.LocationUpdate
import ar.edu.unq.dataowl.model.PlantType
import ar.edu.unq.dataowl.model.PostPackage

/**
 * Created by wolfx on 27/10/2018.
 */
@Database(entities = [PostPackage::class, PlantType::class, LocationUpdate::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun postPackageDao(): PostPackageDAO
    abstract fun plantTypeDao(): PlantTypeDAO
    abstract fun locationUpdateDao(): LocationUpdateDAO

    companion object {
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase? {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase::class.java, "dataowl")
                            .allowMainThreadQueries()
                            .build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
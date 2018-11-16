package ar.edu.unq.dataowl.persistence

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import ar.edu.unq.dataowl.model.LocationUpdate

/**
 * Created by wolfx on 16/11/2018.
 */
@Dao
interface LocationUpdateDAO {
    @Insert
    fun insert(locationUpdate: LocationUpdate)

    @Query("select * from LocationUpdate limit 1")
    fun get(): LocationUpdate

    @Query("delete from LocationUpdate")
    fun clear()
}
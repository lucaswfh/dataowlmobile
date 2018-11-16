package ar.edu.unq.dataowl.persistence

import android.arch.persistence.room.*
import ar.edu.unq.dataowl.model.PlantType

/**
 * Created by wolfx on 16/11/2018.
 */
@Dao
interface PlantTypeDAO {
    @Insert
    fun insert(plantType: PlantType)

    @Delete
    fun delete(plantType: PlantType)

    @Query("delete from PlantType where plantType = :plantType")
    fun deleteByType(plantType: String)

    @Query("select * from PlantType where plantType = :plantType")
    fun get(plantType: String): PlantType

    @Query("select * from PlantType")
    fun getAll(): List<PlantType>
}
package ar.edu.unq.dataowl.persistence

import android.arch.persistence.room.*
import ar.edu.unq.dataowl.model.PostPackage

/**
 * Created by wolfx on 27/10/2018.
 */
@Dao
interface PostPackageDAO {
    @Insert
    fun insert(postPackage: PostPackage)

    @Update
    fun update(postPackage: PostPackage)

    @Delete
    fun delete(postPackage: PostPackage)

    @Query("delete from PostPackage where id = :id")
    fun deleteById(id: Int)

    @Query("delete from PostPackage")
    fun deleteAll()

    @Query("select * from PostPackage")
    fun getAll(): List<PostPackage>

    @Query("select * from PostPackage where sent = 0")
    fun getImagesNotSent(): List<PostPackage>
}
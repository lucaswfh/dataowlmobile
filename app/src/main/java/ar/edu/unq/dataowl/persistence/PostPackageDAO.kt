package ar.edu.unq.dataowl.persistence

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import ar.edu.unq.dataowl.model.PostPackage

/**
 * Created by wolfx on 27/10/2018.
 */
@Dao
interface PostPackageDAO {
    @Insert
    fun insert(postPackage: PostPackage)

    @Delete
    fun delete(postPackage: PostPackage)

    @Query("select * from PostPackage where sent = 0")
    fun getImagesNotSent(): List<PostPackage>
}
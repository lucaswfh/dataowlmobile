package ar.edu.unq.dataowl.model

import android.arch.persistence.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*


/**
 * Created by wolfx on 27/10/2018.
 */
class GithubTypeConverters {

    internal var gson = Gson()

    @TypeConverter
    fun string2list(data: String?): List<String> {
        if (data == null) {
            return Collections.emptyList()
        }

        val listType = object : TypeToken<List<String>>() {

        }.getType()

        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun list2string(someObjects: List<String>): String {
        return gson.toJson(someObjects)
    }
}
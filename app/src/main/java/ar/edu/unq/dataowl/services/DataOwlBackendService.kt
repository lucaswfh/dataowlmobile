package ar.edu.unq.dataowl.services

import ar.edu.unq.dataowl.model.HerbImage
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Created by wolfx on 18/08/2018.
 */
interface DataOwlBackendService {

    @POST("/uploadimage")
    fun postImage(@Body herbImage: HerbImage): Call<String>

}
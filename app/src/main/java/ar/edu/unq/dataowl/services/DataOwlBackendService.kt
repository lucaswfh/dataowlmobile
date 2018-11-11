package ar.edu.unq.dataowl.services

import ar.edu.unq.dataowl.model.PlantType
import ar.edu.unq.dataowl.model.PostPackage
import ar.edu.unq.dataowl.model.PostPackageUpload
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Created by wolfx on 18/08/2018.
 */
interface DataOwlBackendService {

    @GET("/planttypes")
    fun getPlantTypes(): Call<List<String>>

    // accessToken e.g. "Bearer ACCESS_TOKEN_FROM_AUTH0"

    @POST("/private/uploadpackage")
    fun postImage(@Header("authorization") accessToken: String, @Body postPackage: PostPackageUpload): Call<String>

    @POST("/private/userlogin")
    fun userLogIn(@Header("authorization") accessToken: String): Call<String>

}
package ar.edu.unq.dataowl.services

import ar.edu.unq.dataowl.model.PostPackage
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Created by wolfx on 18/08/2018.
 */
interface DataOwlBackendService {
    // accessToken e.g. "Bearer ACCESS_TOKEN_FROM_AUTH0"

    @POST("/private/uploadpackage")
    fun postImage(@Header("authorization") accessToken: String, @Body postPackage: PostPackage): Call<String>

    @POST("/private/userlogin")
    fun userLogIn(@Header("authorization") accessToken: String): Call<String>

}
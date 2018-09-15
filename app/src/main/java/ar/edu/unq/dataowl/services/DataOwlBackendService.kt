package ar.edu.unq.dataowl.services

import ar.edu.unq.dataowl.model.HerbUpload
import com.auth0.android.result.UserProfile
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Created by wolfx on 18/08/2018.
 */
interface DataOwlBackendService {

    @POST("/uploadimage")
    fun postImage(@Body herbUpload: HerbUpload): Call<String>

    // accessTypeToke e.g. "Bearer ACCESS_TOKEN_FROM_AUTH0"
    @POST("/private/userlogin")
    fun userLogIn(@Header("authorization") accessTypeToken: String): Call<String>

}
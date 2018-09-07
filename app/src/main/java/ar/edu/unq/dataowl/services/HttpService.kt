package ar.edu.unq.dataowl.services

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.GsonBuilder



/**
 * Created by wolfx on 18/08/2018.
 */
class HttpService {

    private val gson = GsonBuilder()
            .setLenient()
            .create();

    private val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    val service: DataOwlBackendService = retrofit.create(DataOwlBackendService::class.java)

}
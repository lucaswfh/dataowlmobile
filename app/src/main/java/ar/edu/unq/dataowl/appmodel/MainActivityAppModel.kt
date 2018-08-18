package ar.edu.unq.dataowl.appmodel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.Toast
import ar.edu.unq.dataowl.MainActivity
import ar.edu.unq.dataowl.model.HerbImage
import ar.edu.unq.dataowl.services.HttpService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Created by wolfx on 18/08/2018.
 */
class MainActivityAppModel(val mainActivity: MainActivity) {

    var bitmap: Bitmap? = null

    val CAMERA_REQUEST_CODE = 0

    fun deployTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (takePictureIntent.resolveActivity(mainActivity.packageManager) != null) {
            mainActivity.startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE)
        }
    }

    fun sendImage() {
        val service: HttpService = HttpService()
        val image: HerbImage = HerbImage.Builder()
                .withBitmap(bitmap as Bitmap)
                .build()

        service.service.postImage(image).enqueue(object: Callback<String> {
            override fun onFailure(call: Call<String>?, t: Throwable?) {

            }

            override fun onResponse(call: Call<String>?, response: Response<String>?) {

            }

        })
    }

    fun setBitmap(imageView: ImageView, context: Context, requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK && data !=null) {
                    bitmap = data.extras.get("data") as Bitmap
                    imageView.setImageBitmap(bitmap)
                }
            }
            else -> {
                Toast.makeText(context,"Unrecognized request code", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
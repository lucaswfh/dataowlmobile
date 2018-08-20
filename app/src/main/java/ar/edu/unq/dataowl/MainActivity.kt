package ar.edu.unq.dataowl

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import ar.edu.unq.dataowl.appmodel.MainActivityAppModel
import ar.edu.unq.dataowl.model.HerbImage
import ar.edu.unq.dataowl.services.HttpService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    val appModel: MainActivityAppModel = MainActivityAppModel()

    var bitmap: Bitmap? = null

    val CAMERA_REQUEST_CODE = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setOnButtonClickListeners()
    }

//    take photo activity result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK && data !=null) {
                    val btn: Button = findViewById<Button>(R.id.button_sendImage)
                    btn.isEnabled = true

                    bitmap = data.extras.get("data") as Bitmap
                    findViewById<ImageView>(R.id.imageView_photo).setImageBitmap(bitmap)
                }
            }
            else -> {
                Toast.makeText(this,"Unrecognized request code", Toast.LENGTH_SHORT).show()
            }
        }
    }

//    sets on click button listeners
    fun setOnButtonClickListeners() {
        findViewById<Button>(R.id.button_openCamera).setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

                if (takePictureIntent.resolveActivity(packageManager) != null) {
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE)
                }
            }
        })

        findViewById<Button>(R.id.button_sendImage).setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
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
        })
    }

}

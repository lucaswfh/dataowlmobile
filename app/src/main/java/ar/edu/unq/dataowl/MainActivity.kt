package ar.edu.unq.dataowl

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import ar.edu.unq.dataowl.model.HerbImage
import ar.edu.unq.dataowl.services.HttpService
import ar.edu.unq.dataowl.services.LoginService
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.provider.AuthCallback
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.auth0.android.provider.WebAuthProvider


class MainActivity : AppCompatActivity() {

    var bitmap: Bitmap? = null

    val CAMERA_REQUEST_CODE = 0

    var auth0: Auth0? = null

    val loginService = LoginService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setOnButtonClickListeners()

        auth0 = Auth0(this);
        auth0?.setOIDCConformant(true);
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

    private fun login() {
        loginService.login(auth0 as Auth0, this,
        {
            runOnUiThread {
                val btn: Button = findViewById<Button>(R.id.button_sendImage)
                btn.isEnabled = true
            }
        })
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
                if (loggedIn())
                    sendImage()
                else
                    login()
            }
        })
    }

//    envia la imagen
    private fun sendImage() {
        val service: HttpService = HttpService()
        val image: HerbImage = HerbImage.Builder()
                .withBitmap(bitmap as Bitmap)
                .build()

        service.service.postImage(image).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>?, t: Throwable?) {
                // TODO: notificar al usuario (feedback sobre envio de imagen)
            }

            override fun onResponse(call: Call<String>?, response: Response<String>?) {
                // TODO: notificar al usuario (feedback sobre envio de imagen)
            }
        })
    }

    //    dice si el usuario esta logeado
    private fun loggedIn(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

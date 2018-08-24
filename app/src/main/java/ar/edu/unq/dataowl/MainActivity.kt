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
import ar.edu.unq.dataowl.appmodel.MainActivityAppModel
import ar.edu.unq.dataowl.model.HerbImage
import ar.edu.unq.dataowl.services.HttpService
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.provider.AuthCallback
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.auth0.android.provider.WebAuthProvider


class MainActivity : AppCompatActivity() {

    val appModel: MainActivityAppModel = MainActivityAppModel()

    var bitmap: Bitmap? = null

    val CAMERA_REQUEST_CODE = 0

    var auth0: Auth0? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setOnButtonClickListeners()

        auth0 = Auth0(this);
        auth0?.setOIDCConformant(true);

        login()
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
        val that: MainActivity = this
        WebAuthProvider.init(auth0 as Auth0)
                .withScheme("DataOwlMobile")
                .withAudience(String.format("https://%s/userinfo", getString(R.string.com_auth0_domain)))
                .start(this@MainActivity, object : AuthCallback {
                    override fun onSuccess(credentials: com.auth0.android.result.Credentials) {
                        // Store credentials
                        // Navigate to your main activity
                        val btn: Button = findViewById<Button>(R.id.button_sendImage)
                        btn.isEnabled = true
                    }

                    override fun onFailure(dialog: Dialog) {
                        // Show error Dialog to user
                    }

                    override fun onFailure(exception: AuthenticationException) {
                        // Show error to user
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

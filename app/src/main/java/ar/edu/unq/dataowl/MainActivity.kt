package ar.edu.unq.dataowl

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import ar.edu.unq.dataowl.model.Herb
import ar.edu.unq.dataowl.model.HerbUpload
import ar.edu.unq.dataowl.services.HttpService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import com.auth0.android.result.UserProfile
import com.mashape.unirest.http.Unirest


class MainActivity : AppCompatActivity() {

//    camera and photo
    var bitmap: Bitmap? = null
    val CAMERA_REQUEST_CODE = 0

    // Auth0 access and id tokens ("" if not logged in)
    var AUTH0_ACCESS_TOKEN: String = ""
    var AUTH0_ID_TOKEN:     String = ""

    // User profile (null if not logged in)
    var profile: UserProfile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeAuth0()
        configureButtons()
    }

    fun initializeAuth0() {
        // Get Auth0 Tokens
        val token = intent.getStringExtra(LoginActivity.EXTRA_ACCESS_TOKEN)
        val idtkn = intent.getStringExtra(LoginActivity.EXTRA_ID_TOKEN)
        if (token != null) {
            AUTH0_ACCESS_TOKEN = token
            AUTH0_ID_TOKEN = idtkn
        }

        // If logged in, notify backend
        if (loggedIn())
//            getUserProfileAndNotifyBackend()
            sendTokensToBackend()
    }

    // Sends tokens to backend to notify login
    // asumes AUTH0_ACCESS_TOKEN has a valide acces token
    fun sendTokensToBackend() {
        val httpService = HttpService()

        httpService.service.userLogIn("Bearer " + AUTH0_ACCESS_TOKEN)
                .enqueue(object: Callback<String> {

                    override fun onResponse(call: Call<String>?, response: Response<String>?) {
                        //TODO: To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onFailure(call: Call<String>?, t: Throwable?) {
                        //TODO: To change body of created functions use File | Settings | File Templates.
                    }

                })
    }

    // Gets user profile and sends his info to backend
//    fun getUserProfileAndNotifyBackend() {
//        val auth0 = Auth0(this)
//        auth0.setOIDCConformant(true);
//
//        val usersClient = UsersAPIClient(auth0, AUTH0_ACCESS_TOKEN)
//        val authenticationAPIClient = AuthenticationAPIClient(auth0)
//        val that = this
//
//        authenticationAPIClient.userInfo(AUTH0_ACCESS_TOKEN)
//                .start(object : BaseCallback<UserProfile, AuthenticationException> {
//                    override fun onSuccess(userinfo: UserProfile) {
//                        usersClient.getProfile(userinfo.id)
//                                .start(object : BaseCallback<UserProfile, ManagementException> {
//                                    override fun onSuccess(profile: UserProfile) {
//                                        // Set MainActivity.profile and send its data to backend
//                                        that.profile = profile
//                                        sendProfileToBackend(profile)
//                                    }
//
//                                    override fun onFailure(error: ManagementException) {
//                                        // Show error
//                                        val a = ""
//                                    }
//                                })
//                    }
//
//                    override fun onFailure(error: AuthenticationException) {
//                        // Show error
//                        val a = ""
//                    }
//                })
//    }

    // Sends profile to backend
//    fun sendProfileToBackend(profile: UserProfile) {
//        val httpService = HttpService()
//
//        httpService.service.userLogIn(
//                "Bearer " + AUTH0_ACCESS_TOKEN,
//                profile
//        ).enqueue(object: Callback<String> {
//
//            override fun onResponse(call: Call<String>?, response: Response<String>?) {
//                // TODO: handle response
//                val a = ""
//            }
//
//            override fun onFailure(call: Call<String>?, t: Throwable?) {
//                // TODO: handle response
//                val a = ""
//            }
//        })
//    }

//    take photo activity result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK && data !=null) {
                    runOnUiThread {
                        val btn: Button = findViewById<Button>(R.id.    button_sendImage)
                        btn.isEnabled = true
                    }

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
    fun configureButtons() {
        configureOpenCameraButton()
        confireSendImageButton()
        configureLoginLogoutButton()
    }

    private fun configureOpenCameraButton() {
        findViewById<Button>(R.id.button_openCamera).setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

                if (takePictureIntent.resolveActivity(packageManager) != null) {
                    startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE)
                }
            }
        })
    }

    private fun confireSendImageButton() {
        val sendImageButton = findViewById<Button>(R.id.button_sendImage)

        sendImageButton.setEnabled(loggedIn())

        sendImageButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (loggedIn()) {
                    if (hasImagesToSend())
                        sendImage()
                    else
                        Toast.makeText(
                                this@MainActivity,
                                "No images to send!",
                                Toast.LENGTH_SHORT
                        ).show()
                }
                else {
                    // TODO: verificar conexion a internet e informar al usuario si no se pouede logear
                    login()
                }
            }
        })
    }

    // Says if there are images pending to be sent to backend
    private fun hasImagesToSend(): Boolean = bitmap != null

    private fun configureLoginLogoutButton() {
        val loginLogoutButton = findViewById<Button>(R.id.button_loginLogoutButton)

        if (loggedIn())
            loginLogoutButton.text = "LOG OUT"
        else
            loginLogoutButton.text = "LOG IN"

        loginLogoutButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                if (loggedIn())
                    logout()
                else
                    login()
            }
        })
    }

    //    envia la imagen y notifica al usuario
    private fun sendImage() {
        // http service
        val service = HttpService()

        // convert bitmap into base 64 string
        val stream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.PNG, 100, stream);
        val image = Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)

        // create upload object
        val herbImageUpload = HerbUpload(
                Herb(image),
                AUTH0_ACCESS_TOKEN,
                AUTH0_ID_TOKEN
        )

        // send
        service.service.postImage(herbImageUpload).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>?, t: Throwable?) {
                Toast.makeText(
                        this@MainActivity,
                        "Image not sent!",
                        Toast.LENGTH_SHORT
                ).show()

                // TODO: handlear el error
            }

            override fun onResponse(call: Call<String>?, response: Response<String>?) {
                Toast.makeText(
                        this@MainActivity,
                        "Image sent correctly!",
                        Toast.LENGTH_SHORT
                ).show()

                // TODO: hacer algo con la respuesta?
            }
        })
    }

    private fun login() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun logout() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtra(LoginActivity.KEY_CLEAR_CREDENTIALS, true)
        startActivity(intent)
        finish()
    }

    //    dice si el usuario esta logeado
    private fun loggedIn(): Boolean = AUTH0_ACCESS_TOKEN != ""

}

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
import ar.edu.unq.dataowl.model.HerbImage
import ar.edu.unq.dataowl.services.HttpService
import com.auth0.android.Auth0
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.management.UsersAPIClient
import com.auth0.android.management.ManagementException
import com.auth0.android.result.UserProfile
import com.auth0.android.callback.BaseCallback

class MainActivity : AppCompatActivity() {

//    camera and photo
    var bitmap: Bitmap? = null
    val CAMERA_REQUEST_CODE = 0

    // Auth0 access and id tokens ("" if not logged in)
    var AUTH0_ACCESS_TOKEN: String = ""
    var AUTH0_ID_TOKEN:     String = ""

    // Auth0 api clients, null if not logged in
    var usersClient: UsersAPIClient? = null
    var authenticationAPIClient: AuthenticationAPIClient? = null

    private var auth0: Auth0? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        configureButtons()
        initializeAuth0()
    }

    fun initializeAuth0() {
        // Initialize Auth0
        auth0 = Auth0(this)
        auth0?.setOIDCConformant(true);

        // Get Auth0 Tokens
        val token = intent.getStringExtra(LoginActivity.EXTRA_ACCESS_TOKEN)
        val idtkn = intent.getStringExtra(LoginActivity.EXTRA_ID_TOKEN)
        if (token != null) {
            AUTH0_ACCESS_TOKEN = token
            AUTH0_ID_TOKEN = idtkn

            // if im here its because im logged in, so initialize clients api
            usersClient = UsersAPIClient(auth0, AUTH0_ACCESS_TOKEN)
            authenticationAPIClient = AuthenticationAPIClient(auth0 as Auth0)

            getUsetProfile()
        }
    }

    fun getUsetProfile() {
        val client = authenticationAPIClient as AuthenticationAPIClient
        client.userInfo(AUTH0_ACCESS_TOKEN)
                .start(object : BaseCallback<UserProfile, AuthenticationException> {
                    override fun onSuccess(userinfo: UserProfile) {
                        val cli = usersClient as UsersAPIClient
                        cli.getProfile(userinfo.id)
                                .start(object : BaseCallback<UserProfile, ManagementException> {
                                    override fun onSuccess(profile: UserProfile) {
                                        // Display the user profile
                                        
                                    }

                                    override fun onFailure(error: ManagementException) {
                                        // Show error
                                    }
                                })
                    }

                    override fun onFailure(error: AuthenticationException) {
                        // Show error
                    }
                })
    }

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
        confureSendImageButton()
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

    private fun confureSendImageButton() {
        findViewById<Button>(R.id.button_sendImage).setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (loggedIn())
                    sendImage()
                else {
                    // TODO: verificar conexion a internet
                    login()
                }
            }
        })
    }

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

    //    envia la imagen
    private fun sendImage() {
        val service: HttpService = HttpService()
        val herbImage: HerbImage = HerbImage.Builder()
                .withBitmap(bitmap as Bitmap)
                .withUserAccessToken(AUTH0_ACCESS_TOKEN)
                .build()

        service.service.postImage(herbImage).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>?, t: Throwable?) {
                // TODO: notificar al usuario (feedback sobre envio de imagen)
            }

            override fun onResponse(call: Call<String>?, response: Response<String>?) {
                // TODO: notificar al usuario (feedback sobre envio de imagen)
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

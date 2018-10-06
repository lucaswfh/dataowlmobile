package ar.edu.unq.dataowl

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.provider.Settings.*
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import ar.edu.unq.dataowl.model.PostPackage
import ar.edu.unq.dataowl.services.HttpService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import com.auth0.android.result.UserProfile
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.support.v4.content.FileProvider
import java.util.jar.Manifest


class MainActivity : AppCompatActivity() {

//    camera and photo
    var bitmap: Bitmap? = null
    var photoFile: File? = null;
    val CAMERA_REQUEST_CODE = 0
    val REQUEST_IMAGE_CAPTURE = 1
    val REQUEST_TAKE_PHOTO = 1
    var mCurrentPhotoPath: String? = null

    // Auth0 access and id tokens ("" if not logged in)
    var AUTH0_ACCESS_TOKEN: String = ""
    var AUTH0_ID_TOKEN:     String = ""

    // User profile (null if not logged in)
    var profile: UserProfile? = null

    //GPS
    var locationManager: LocationManager? = null
    var locationListener: LocationListener? = null
    var location: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeAuth0()
        configureButtons()

        this.locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        this.locationListener = object : LocationListener{
            override fun onLocationChanged(lc: Location) {
                // Called when a new location is found by the network location provider.
                location = lc
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            }

            override fun onProviderEnabled(provider: String) {
            }

            override fun onProviderDisabled(provider: String) {
                val intent: Intent = Intent(ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }

        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(arrayOf<String>(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                ), 10)
                return
            }
        }

        locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000 , 0f, locationListener)
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

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = absolutePath
        }
    }

//    take photo activity result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                if (resultCode == Activity.RESULT_OK) {
                        runOnUiThread {
                            val btn: Button = findViewById<Button>(R.id.button_sendImage)
                            btn.isEnabled = true
                        }

                        val file = File(mCurrentPhotoPath)
                        bitmap = MediaStore.Images.Media
                                .getBitmap(this@MainActivity.getContentResolver(), Uri.fromFile(file))

                        if (bitmap != null)
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
                Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                    // Ensure that there's a camera activity to handle the intent
                    takePictureIntent.resolveActivity(packageManager)?.also {
                        // Create the File where the photo should go
                        val photoFile: File? = try {
                            createImageFile()
                        } catch (ex: IOException) {
                            // Error occurred while creating the File
                            null
                        }
                        // Continue only if the File was successfully created
                        photoFile?.also {
                            val photoURI: Uri = FileProvider.getUriForFile(
                                    this@MainActivity,
                                    "ar.edu.unq.dataowl.fileprovider",
                                    it
                            )
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                        }
                    }
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
        val images : List<String> = listOf<String>(image)
        val postPackageUpload = PostPackage(
                images,
                location?.latitude.toString(),
                location?.longitude.toString()
        )

        // send
        service.service.postImage(
                "Bearer " + AUTH0_ACCESS_TOKEN, postPackageUpload
        ).enqueue(object : Callback<String> {
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

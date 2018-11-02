package ar.edu.unq.dataowl.activities

import android.app.Activity
import android.arch.persistence.room.Room
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings.*
import android.view.View
import ar.edu.unq.dataowl.services.HttpService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.auth0.android.result.UserProfile
import java.io.File
import java.io.IOException
import android.support.v4.content.FileProvider
import android.widget.*
import ar.edu.unq.dataowl.R
import ar.edu.unq.dataowl.model.ImageHandler
import ar.edu.unq.dataowl.model.PostPackage
import ar.edu.unq.dataowl.persistence.AppDatabase
import ar.edu.unq.dataowl.persistence.DbWorkerThread


class MainActivity : AppCompatActivity() {

    //Camera and Photo
    var bitmap: Bitmap? = null
    var photoFile: File? = null;
    val CAMERA_REQUEST_CODE = 0
    val REQUEST_IMAGE_CAPTURE = 1
    val REQUEST_TAKE_PHOTO = 1
    var mCurrentPhotoPath: String? = null
    val ih = ImageHandler()
    var type: String? = null

    // Auth0 access and id tokens ("" if not logged in)
    var AUTH0_ACCESS_TOKEN: String = ""
    var AUTH0_ID_TOKEN:     String = ""

    // User profile (null if not logged in)
    var profile: UserProfile? = null

    // GPS
    var locationManager: LocationManager? = null
    var locationListener: LocationListener? = null
    var location: Location? = null

    // DB
    private lateinit var dbWorkerThread: DbWorkerThread

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

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}

            override fun onProviderDisabled(provider: String) {
                val intent: Intent = Intent(ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }

        }

        //Check-in sdk version
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
    // Asumes AUTH0_ACCESS_TOKEN has a valide acces token
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

    // Take photo activity result
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

    // ++++++++++++++++ Main Activity Buttons config ++++++++++++++++ //

    fun configureButtons() {
        configureOpenCameraButton()
        confireSendImageButton()
        configureLoginLogoutButton()
        configureList()
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
    private fun configureList(){
        val spinner = findViewById<Spinner>(R.id.plant_spinner)
        val adapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(this, R.array.plant_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.setAdapter(adapter)

        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                type = p0?.getItemAtPosition(p2) as String
            }
        }
    }

    // ++++++++++++++++ Image Handling ++++++++++++++++ //

    // Creates the blank file to store the image
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val file: File = ih.createImageFile(getExternalFilesDir(Environment.DIRECTORY_PICTURES))
        return file.apply {
            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = absolutePath
        }
    }

    // Says if there are images pending to be sent to backend
    private fun hasImagesToSend(): Boolean = bitmap != null

    // Tryes to sends the image and notifies the user
    // If no network conectivity, persist to app database
    private fun sendImage() {
        val postPackage = ih.prepearToSend(bitmap as Bitmap, location, type as String)

        val cm = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val connectivity = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val isConnectedToWifi: Boolean =
                cm.activeNetworkInfo != null &&
                cm.activeNetworkInfo.isConnected &&
                cm.activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI

        if (isConnectedToWifi) {
            uploadImage(postPackage)
            return
        }

        persistImage(postPackage)
    }

    // Persist image to app database
    private fun persistImage(postPackage: PostPackage) {
        val db = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java,
                "database-name"
        )
                .allowMainThreadQueries()
                .build()

        val postPackageDao = db.postPackageDao()
        dbWorkerThread = DbWorkerThread("dbWorkerThread")
        dbWorkerThread.start()

//        val task = Runnable {
            postPackageDao.insert(postPackage)
//        }
//        dbWorkerThread.postTask(task)

        Toast.makeText(
                this@MainActivity,
                "No network conectivity, saving image to send later!",
                Toast.LENGTH_LONG
        ).show()

//        val task2 = Runnable {
            val posts = postPackageDao.getImagesNotSent()
//        }
//        dbWorkerThread.postTask(task2)
        val a = "asd"
    }

    // Uploads package to backend
    // Asumes network conectivity
    private fun uploadImage(postPackage: PostPackage) {
        val service = HttpService()

        service.service.postImage(
                "Bearer " + AUTH0_ACCESS_TOKEN, postPackage
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


    // ++++++++++++++++ Auth0 Intents ++++++++++++++++ //

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
    private fun loggedIn(): Boolean = AUTH0_ACCESS_TOKEN != ""

}

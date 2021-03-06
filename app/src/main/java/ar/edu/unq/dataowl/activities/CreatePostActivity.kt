package ar.edu.unq.dataowl.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.support.v4.content.FileProvider
import android.view.View
import android.widget.*
import ar.edu.unq.dataowl.R
import ar.edu.unq.dataowl.model.ImageHandler
import ar.edu.unq.dataowl.model.LocationUpdate
import ar.edu.unq.dataowl.model.PlantType
import ar.edu.unq.dataowl.persistence.AppDatabase
import java.io.File
import java.io.IOException

class CreatePostActivity : AppCompatActivity(), View.OnClickListener{

    companion object {
        const val AUTH0_ACCESS_TOKEN: String = "access_token"
        const val AUTH0_ID_TOKEN:     String = "id_token"
    }

    private val REQUEST_TAKE_PHOTO = 1
    val REQUEST_IMAGE_CAPTURE = 1

    val ih = ImageHandler()
    var mCurrentPhotoPaths: MutableList<String> = mutableListOf<String>()
    var type: String? = null
    var bitmap: Bitmap? = null
    var bitmaps: MutableList<Bitmap> = mutableListOf<Bitmap>()
    var tempId: Int? = null

    // GPS
    var locationManager: LocationManager? = null
    var locationListener: LocationListener? = null
    var location: Location? = null

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        configureButtons()
        configureList()

        this.locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        this.locationListener = object : LocationListener {
            override fun onLocationChanged(lc: Location) {
                // Called when a new location is found by the network location provider.
                location = lc
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}

            override fun onProviderDisabled(provider: String) {
                val intent: Intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                intent.putExtra(AUTH0_ACCESS_TOKEN, AUTH0_ACCESS_TOKEN)
                intent.putExtra(AUTH0_ID_TOKEN, AUTH0_ID_TOKEN)
                startActivity(intent)
            }
        }


        locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 , 0f, locationListener)
    }

    /**
     * configura todos los botones de la activity
     */
    private fun configureButtons() {

        findViewById<ImageButton> (R.id.imageButton1).setOnClickListener(this)
        findViewById<ImageButton> (R.id.imageButton2).setOnClickListener(this)
        findViewById<ImageButton> (R.id.imageButton3).setOnClickListener(this)
        findViewById<ImageButton> (R.id.imageButton4).setOnClickListener(this)

        //configureImageButton1()
        configureBottomButtons()
    }

    fun configureBottomButtons() {
        findViewById<Button>(R.id.buttonOk).setOnClickListener(object: View.OnClickListener {
            override fun onClick(p0: View?) {
                val update = LocationUpdate()
                if (location == null) {
                    val dao = AppDatabase.getInstance(this@CreatePostActivity)?.locationUpdateDao()
                    val lastUpdate = dao?.get()
                    if (lastUpdate != null) {
                        update.lat = lastUpdate.lat
                        update.lng = lastUpdate.lng
                    }
                } else {
                    update.lat = location?.latitude.toString()
                    update.lng = location?.longitude.toString()
                }

                val postPackage = ih.prepearToSend(this@CreatePostActivity, mCurrentPhotoPaths, update, type as String)
                postPackage.persist(this@CreatePostActivity)
                showNextActivity()
            }
        })

        findViewById<Button>(R.id.buttonCancel).setOnClickListener(object: View.OnClickListener{
            override fun onClick(v: View?) {
                finish()
            }
        })
    }

    private fun showNextActivity() {
        val intent = Intent(this@CreatePostActivity, PostListActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    private fun configureList(){
        val spinner = findViewById<Spinner>(R.id.plant_spinner1)
        var plants: MutableList<String> = resources.getStringArray(R.array.plant_array).toMutableList()

        val dao = AppDatabase.getInstance(this@CreatePostActivity)?.plantTypeDao()
        val storedPlants = dao?.getAll()
        storedPlants?.forEach { plantType: PlantType ->
            plants.add(plantType.plantType)
        }

        val adapter = ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                plants
        )
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

    override fun onClick(v: View) {
        this.tempId = v.id
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
                            this@CreatePostActivity,
                            "ar.edu.unq.dataowl.fileprovider",
                            it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                }
            }
        }
    }

    // Take photo activity result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                if (resultCode == Activity.RESULT_OK) {
                    runOnUiThread {

                    }

                    val file = File(mCurrentPhotoPaths.last())
                    bitmap = MediaStore.Images.Media
                            .getBitmap(this@CreatePostActivity.getContentResolver(), Uri.fromFile(file))

                    if (bitmap != null) {
                        val button = findViewById<ImageButton>(this.tempId as Int)
                        button?.setImageBitmap(bitmap)
                        button?.isEnabled = false
                    }

                    this.bitmaps.add(bitmap ?: return)
                }
            }
            else -> {
                Toast.makeText(this,"Unrecognized request code", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Creates the blank file to store the image
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val file: File = ih.createImageFile(getExternalFilesDir(Environment.DIRECTORY_PICTURES))
        return file.apply {
            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPaths.add(absolutePath)
        }
    }
}

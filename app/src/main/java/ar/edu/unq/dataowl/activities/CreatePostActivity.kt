package ar.edu.unq.dataowl.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import ar.edu.unq.dataowl.R
import ar.edu.unq.dataowl.model.ImageHandler
import java.io.File
import java.io.IOException

class CreatePostActivity : AppCompatActivity() {

    private val REQUEST_TAKE_PHOTO = 1
    val REQUEST_IMAGE_CAPTURE = 1

    val ih = ImageHandler()
    var mCurrentPhotoPath: String? = null

    var bitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        configureButtons()
    }

    /**
     * configura todos los botones de la activity
     */
    private fun configureButtons() {
        findViewById<ImageButton>(R.id.imageButton1).setOnClickListener(object: View.OnClickListener {
            override fun onClick(p0: View?) {
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
                            .getBitmap(this@CreatePostActivity.getContentResolver(), Uri.fromFile(file))

                    if (bitmap != null)
                        findViewById<ImageButton>(R.id.imageView_photo).setImageBitmap(bitmap)
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
            mCurrentPhotoPath = absolutePath
        }
    }
}

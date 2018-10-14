package ar.edu.unq.dataowl.model

import android.graphics.Bitmap
import android.location.Location
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ImageHandler {

    fun createImageFile(stDir: File): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = stDir
        return File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        )
    }

    fun prepearToSend(bitmap: Bitmap, location: Location, type: String): PostPackage{

        val stream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.PNG, 100, stream);
        val image = Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)

        val images : List<String> = listOf<String>(image)
        val postPackageUpload = PostPackage(
                images,
                location?.latitude.toString(),
                location?.longitude.toString(),
                type

        )

        return postPackageUpload

    }
}
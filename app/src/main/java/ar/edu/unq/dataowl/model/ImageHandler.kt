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

    fun prepearToSend(bitmap: Bitmap, location: Location?, type: String): PostPackage{

        // to byte64
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream);
        val image: String = Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)

        val images : List<String> = listOf<String>(image)

        val postPackageUpload = PostPackage()

        postPackageUpload.images = images
        postPackageUpload.lat = location?.latitude.toString()
        postPackageUpload.lng = location?.longitude.toString()
        postPackageUpload.type = type

        return postPackageUpload

    }

}
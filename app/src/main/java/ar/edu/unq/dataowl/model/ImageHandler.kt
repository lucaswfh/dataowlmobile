package ar.edu.unq.dataowl.model

import android.content.Context
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.provider.MediaStore
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

    // Converts to byte64 to send
    fun prepareImageToSend(context: Context, fileLocation: String): String {
        val b:Bitmap = MediaStore.Images.Media
                .getBitmap(context.getContentResolver(), Uri.fromFile(File(fileLocation)))
        val stream = ByteArrayOutputStream()
        b.compress(Bitmap.CompressFormat.JPEG, 75, stream)
        val image: String = Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)

        return image
    }

    fun prepearToSend(context: Context, strings: MutableList<String>, location: Location?, type: String): PostPackage{

        val images : MutableList<String> = mutableListOf<String>()

        for (s in strings) {
            images.add(s)
        }

        val postPackageUpload = PostPackage()

        postPackageUpload.images = images.toList()
        postPackageUpload.lat = location?.latitude.toString()
        postPackageUpload.lng = location?.longitude.toString()
        postPackageUpload.type = type

        return postPackageUpload

    }

}
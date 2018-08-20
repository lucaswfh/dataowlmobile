package ar.edu.unq.dataowl.model

import android.graphics.Bitmap
import java.io.ByteArrayOutputStream

/**
 * Created by wolfx on 18/08/2018.
 */
class HerbImage constructor(val image: ByteArray) {

    class Builder {

        var image: ByteArray? = null
            private set

        fun build(): HerbImage = HerbImage(this.image as ByteArray)

        fun withBitmap(bitmap: Bitmap): Builder {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            this.image = stream.toByteArray()
            return this
        }

    }

}
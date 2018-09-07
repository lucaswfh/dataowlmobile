package ar.edu.unq.dataowl.model

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream

/**
 * Created by wolfx on 18/08/2018.
 */
class HerbImage constructor(val image: String, val access_token: String) {

    class Builder {

        var image: String? = null
            private set

        var access_token: String? = null
            private set

        fun build(): HerbImage = HerbImage(this.image as String, this.access_token as String)

        fun withBitmap(bitmap: Bitmap): Builder {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            this.image = Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT);
            return this
        }

        fun withUserAccessToken(AUTH0_ACCESS_TOKEN: String): Builder {
            this.access_token = AUTH0_ACCESS_TOKEN
            return this
        }

    }

}
package ar.edu.unq.dataowl.model

import java.nio.charset.StandardCharsets
import java.util.zip.GZIPOutputStream
import java.io.*
import java.util.zip.GZIPInputStream


/**
 * Created by wolfx on 14/10/2018.
 */
class GzipUtil {
    fun zip(str: String?): ByteArray {
        if (str == null || str.length == 0) {
            throw IllegalArgumentException("Cannot zip null or empty string")
        }

        try {
            ByteArrayOutputStream().use({ byteArrayOutputStream ->
                GZIPOutputStream(byteArrayOutputStream).use({ gzipOutputStream ->
                    gzipOutputStream.write(str.toByteArray(StandardCharsets.UTF_8))
                })
                return byteArrayOutputStream.toByteArray()
            })
        } catch (e: IOException) {
            throw RuntimeException("Failed to zip content", e)
        }
    }

    fun unzip(compressed: ByteArray?): String {
        if (compressed == null || compressed.size == 0) {
            throw IllegalArgumentException("Cannot unzip null or empty bytes")
        }
        if (!isZipped(compressed)) {
            return String(compressed)
        }

        try {
            ByteArrayInputStream(compressed).use({ byteArrayInputStream ->
                GZIPInputStream(byteArrayInputStream).use({ gzipInputStream ->
                    InputStreamReader(gzipInputStream, StandardCharsets.UTF_8).use({ inputStreamReader ->
                        BufferedReader(inputStreamReader).use({ bufferedReader ->
                            val output = StringBuilder()
                            var line: String? = bufferedReader.readLine()
                            while (line != null) {
                                output.append(line)
                                line = bufferedReader.readLine()
                            }
                            return output.toString()
                        })
                    })
                })
            })
        } catch (e: IOException) {
            throw RuntimeException("Failed to unzip content", e)
        }
    }

    fun isZipped(compressed: ByteArray): Boolean =
            compressed[0] == GZIPInputStream.GZIP_MAGIC.toByte() &&
            compressed[1] == (GZIPInputStream.GZIP_MAGIC shr 8).toByte()
}
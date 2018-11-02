package ar.edu.unq.dataowl

import ar.edu.unq.dataowl.model.PostPackage
import ar.edu.unq.dataowl.persistence.AppDatabase
import java.util.ArrayList
import java.util.HashMap

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
object PostsObjects {

    val ITEMS: MutableList<PostPackage> = ArrayList()

    /**
     * A map of sample (dummy) items, by ID.
     */
    val ITEM_MAP: MutableMap<String, PostPackage> = HashMap()

   // private val COUNT = 25

    init {
        val db: AppDatabase = AppDatabase.getInstance() as AppDatabase
        val packages = db.postPackageDao().getAll()
        for (p: PostPackage in packages) {
            addItem(p)
        }
        }
    }

    private fun addItem(item: PostPackage) {
        ITEMS.add(item)
        ITEM_MAP.put("asd", item)
    }


    /*
    private fun makeDetails(position: Int): String {
        val builder = StringBuilder()
        builder.append("Details about Item: ").append(position)
        for (i in 0..position - 1) {
            builder.append("\nMore details information here.")
        }
        return builder.toString()
    }*/
}

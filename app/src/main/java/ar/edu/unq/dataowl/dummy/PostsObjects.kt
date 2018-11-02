package ar.edu.unq.dataowl

import java.util.ArrayList
import java.util.HashMap

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
object PostsObjects {

    val ITEMS: MutableList<PostItem> = ArrayList()

    /**
     * A map of sample (dummy) items, by ID.
     */
    val ITEM_MAP: MutableMap<String, PostItem> = HashMap()

   // private val COUNT = 25

    /*init {
        // Add some sample items.
        for (i in 1..COUNT) {
            addItem(createDummyItem(i))
        }
    }*/

    private fun addItem(item: PostItem) {
        ITEMS.add(item)
        ITEM_MAP.put(item.id, item)
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

    /**
     * A dummy item representing a piece of content.
     */
    data class PostItem(val id: String, val content: String, val details: String) {

        override fun toString(): String = content
    }
}

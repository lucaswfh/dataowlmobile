package ar.edu.unq.dataowl.activities

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import ar.edu.unq.dataowl.PostsObjects
import ar.edu.unq.dataowl.R
import ar.edu.unq.dataowl.model.PostPackage
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.activity_post_detail.*
import kotlinx.android.synthetic.main.post_detail.view.*

/**
 * A fragment representing a single Post detail screen.
 * This fragment is either contained in a [PostListActivity]
 * in two-pane mode (on tablets) or a [PostDetailActivity]
 * on handsets.
 */
class PostDetailFragment : Fragment() {

    /**
     * The dummy content this fragment is presenting.
     */
    private var item: PostPackage? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            if (it.containsKey(ARG_ITEM_ID)) {
                // Load the dummy content specified by the fragment
                // arguments. In a real-world scenario, use a Loader
                // to load content from a content provider.
                val a = it.getString(ARG_ITEM_ID)
                item = PostsObjects.ITEM_MAP[it.getString(ARG_ITEM_ID)]
                activity?.toolbar_layout?.title = item?.type
            }
        }

        if (item != null) {
            val sent = item?.sent as Boolean
            val tv = view?.findViewById<TextView>(R.id.textViewPostDetailSent)
            if (sent)
                tv?.text = "SENT: YES"
            else
                tv?.text = "SENT: NO"
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.post_detail, container, false)

        // Show the dummy content as text in a TextView.
        item?.let {
            rootView.post_detail.text = it.type
        }

        return rootView
    }

    companion object {
        /**
         * The fragment argument representing the item ID that this fragment
         * represents.
         */
        const val ARG_ITEM_ID = "item_id"
    }
}

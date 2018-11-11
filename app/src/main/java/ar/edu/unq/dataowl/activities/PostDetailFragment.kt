package ar.edu.unq.dataowl.activities

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ar.edu.unq.dataowl.PostsObjects
import ar.edu.unq.dataowl.R
import ar.edu.unq.dataowl.model.PostPackage
import kotlinx.android.synthetic.main.activity_post_detail.*
import kotlinx.android.synthetic.main.post_detail.view.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.Image
import android.util.Base64
import android.widget.*
import ar.edu.unq.dataowl.model.ImageHandler
import kotlinx.android.synthetic.main.post_detail.*


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
                activity?.toolbar_layout?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#58377e")))
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.post_detail, container, false)

        item?.let {
            val builder = StringBuilder()
            val status: String
            if(it.sent)
                status = "Yes"
            else
                status = "No"
            rootView.post_detail.text =
                    builder.append("\nLatitude: ").append(it.lat)
                            .append("\nLongitude: ").append(it.lng)
                            .append("\nType Selected: ").append(it.type)
                            .append("\nSent Status: ").append(status)
                            .toString()

            val linearLayout: LinearLayout = rootView.findViewById(R.id.gallery)
            val layoutInflater: LayoutInflater = LayoutInflater.from(activity!!.baseContext )

            for(i in it.images){
                val view: View = inflater.inflate(R.layout.gallery_item, gallery, false)
                val byte = Base64.decode(ImageHandler().prepareImageToSend(activity!!.baseContext, i), 0)

                val imageViewB: ImageButton = view.findViewById(R.id.imageView)
                imageViewB.setImageBitmap(BitmapFactory.decodeByteArray(byte,0, byte.size))
                imageViewB.setOnClickListener(object: View.OnClickListener{
                    override fun onClick(v: View?) {
                        Toast.makeText(
                                activity!!.baseContext,
                                "sarasa",
                                Toast.LENGTH_SHORT
                        ).show()

                        //TODO: nuevo activity con la imagen en grande
                    }
                })

                linearLayout.addView(view)
            }


        }

        return rootView

    }

    companion object {
          /**
         * The fragment argument representing the item ID that this fragment
         * represents.
         */
        const val ARG_ITEM_ID = "item_id"
        const val AUTH0_ACCESS_TOKEN: String = "access_token"
        const val AUTH0_ID_TOKEN:     String = "id_token"
    }
}


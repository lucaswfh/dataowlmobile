package ar.edu.unq.dataowl.activities

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import ar.edu.unq.dataowl.R
import ar.edu.unq.dataowl.persistence.AppDatabase
import kotlinx.android.synthetic.main.activity_post_detail.*

/**
 * An activity representing a single Post detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a [PostListActivity].
 */
class PostDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)
        setSupportActionBar(detail_toolbar)

        /*
        fab.setOnClickListener { view ->
            /*Snackbar.make(view, "Replace with your own detail action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()*/
            //TODO: ver si es necesario este boton
        }*/

        // Show the Up button in the action bar.
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        deleteButton.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                //TODO: ver como carajos meter el comportamiento del delete
            }
        })

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            val fragment = PostDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(PostDetailFragment.ARG_ITEM_ID,
                            intent.getStringExtra(PostDetailFragment.ARG_ITEM_ID))
                }
            }

            supportFragmentManager.beginTransaction()
                    .add(R.id.post_detail_container, fragment)
                    .commit()
        }


        configureDeleteButton()
    }

    private fun configureDeleteButton() {
        val button = findViewById<Button>(R.id.deleteButton)
        button?.setOnClickListener {
            val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        val dao = AppDatabase.getInstance(this)?.postPackageDao()
                        val id = intent.getStringExtra(PostDetailFragment.ARG_ITEM_ID).toInt()
                        dao?.deleteById(id)
                        showNextActivity()
                    }
                }
            }

            val builder = AlertDialog.Builder(this)
            builder
                    .setMessage("Are you sure?")
                    .setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener)
                    .show()
        }
    }

    private fun showNextActivity() {
        val intent = Intent(this , PostListActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem) =
            when (item.itemId) {
                android.R.id.home -> {
                    // This ID represents the Home or Up button. In the case of this
                    // activity, the Up button is shown. For
                    // more details, see the Navigation pattern on Android Design:
                    //
                    // http://developer.android.com/design/patterns/navigation.html#up-vs-back

                    navigateUpTo(Intent(this, PostListActivity::class.java))
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
}

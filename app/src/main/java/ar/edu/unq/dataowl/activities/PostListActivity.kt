package ar.edu.unq.dataowl.activities

import android.arch.persistence.room.Room
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import ar.edu.unq.dataowl.PostsObjects
import ar.edu.unq.dataowl.R
import ar.edu.unq.dataowl.model.PostPackage
import ar.edu.unq.dataowl.persistence.AppDatabase
import ar.edu.unq.dataowl.services.HttpService
import kotlinx.android.synthetic.main.activity_post_list.*
import kotlinx.android.synthetic.main.post_list.*
import kotlinx.android.synthetic.main.post_list_content.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * An activity representing a list of Pings. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [PostDetailActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */

class PostListActivity : AppCompatActivity() {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */

    private var twoPane: Boolean = false

    // Auth0 access and id tokens ("" if not logged in)
    var AUTH0_ACCESS_TOKEN: String = ""
    var AUTH0_ID_TOKEN:     String = ""

    var packages: List<PostPackage>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_list)

        setSupportActionBar(toolbar)
        toolbar.title = title

        fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show()
            val intent = Intent(this, CreatePostActivity::class.java)
            startActivity(intent)
        }

        if (post_detail_container != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            twoPane = true
        }

        setupRecyclerView(post_list)
        initializeAuth0()
        configureLoginLogoutButton()

        createPostList()
    }

    private fun createPostList() {
        val db: AppDatabase = AppDatabase.getInstance(this@PostListActivity) as AppDatabase
        packages = db.postPackageDao().getAll()

        for (p: PostPackage in packages as List<PostPackage>) {

        }
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = SimpleItemRecyclerViewAdapter(this, PostsObjects.ITEMS, twoPane)
    }

    class SimpleItemRecyclerViewAdapter(private val parentActivity: PostListActivity,
                                        private val values: List<PostsObjects.PostItem>,
                                        private val twoPane: Boolean) :
            RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

        private val onClickListener: View.OnClickListener

        init {
            onClickListener = View.OnClickListener { v ->
                val item = v.tag as PostsObjects.PostItem
                if (twoPane) {
                    val fragment = PostDetailFragment().apply {
                        arguments = Bundle().apply {
                            putString(PostDetailFragment.ARG_ITEM_ID, item.id)
                        }
                    }
                    parentActivity.supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.post_detail_container, fragment)
                            .commit()
                } else {
                    val intent = Intent(v.context, PostDetailActivity::class.java).apply {
                        putExtra(PostDetailFragment.ARG_ITEM_ID, item.id)
                    }
                    v.context.startActivity(intent)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.post_list_content, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = values[position]
            holder.idView.text = item.id
            holder.contentView.text = item.content

            with(holder.itemView) {
                tag = item
                setOnClickListener(onClickListener)
            }
        }

        override fun getItemCount() = values.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val idView: TextView = view.id_text
            val contentView: TextView = view.content
        }
    }


    // ++++++++++++++++ Buttons config ++++++++++++++++ //

    private fun configureLoginLogoutButton() {
        val loginLogoutButton = findViewById<Button>(R.id.loginButtonNew)

        if (loggedIn())
            loginLogoutButton.text = "LOG OUT"
        else
            loginLogoutButton.text = "LOG IN"

        loginLogoutButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                if (loggedIn())
                    logout()
                else
                    login()
            }
        })
    }


    // ++++++++++++++++ Auth0 Intents ++++++++++++++++ //

    private fun login() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun logout() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtra(LoginActivity.KEY_CLEAR_CREDENTIALS, true)
        startActivity(intent)
        finish()
    }
    private fun loggedIn(): Boolean = AUTH0_ACCESS_TOKEN != ""


    // ++++++++++++++++ Auth0 Config ++++++++++++++++ //

    fun initializeAuth0() {
        // Get Auth0 Tokens
        val token = intent.getStringExtra(LoginActivity.EXTRA_ACCESS_TOKEN)
        val idtkn = intent.getStringExtra(LoginActivity.EXTRA_ID_TOKEN)
        if (token != null) {
            AUTH0_ACCESS_TOKEN = token
            AUTH0_ID_TOKEN = idtkn
        }

        // If logged in, notify backend
        if (loggedIn())
//            getUserProfileAndNotifyBackend()
            sendTokensToBackend()
    }

    // Sends tokens to backend to notify login
    // Asumes AUTH0_ACCESS_TOKEN has a valide acces token
    fun sendTokensToBackend() {
        val httpService = HttpService()

        httpService.service.userLogIn("Bearer " + AUTH0_ACCESS_TOKEN)
                .enqueue(object: Callback<String> {

                    override fun onResponse(call: Call<String>?, response: Response<String>?) {
                        //TODO: To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onFailure(call: Call<String>?, t: Throwable?) {
                        //TODO: To change body of created functions use File | Settings | File Templates.
                    }

                })
    }

}


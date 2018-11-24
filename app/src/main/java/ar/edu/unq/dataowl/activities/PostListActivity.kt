package ar.edu.unq.dataowl.activities

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.FloatingActionButton
import android.support.v4.widget.NestedScrollView
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.*
import android.widget.TextView
import android.widget.Toast
import ar.edu.unq.dataowl.PostsObjects
import ar.edu.unq.dataowl.R
import ar.edu.unq.dataowl.model.LocationUpdate
import ar.edu.unq.dataowl.model.PlantType
import ar.edu.unq.dataowl.model.PostPackage
import ar.edu.unq.dataowl.persistence.AppDatabase
import ar.edu.unq.dataowl.persistence.PostPackageDAO
import ar.edu.unq.dataowl.services.HttpService
import kotlinx.android.synthetic.main.post_list_content.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.ArrayList

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

    // GPS
    var locationManager: LocationManager? = null
    var locationListener: LocationListener? = null
    var location: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_list)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.title = title

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener { view ->
            val dao = AppDatabase.getInstance(this@PostListActivity)?.locationUpdateDao()
            val lastUpdate = dao?.get()

//            if (lastUpdate == null) {
//                Toast.makeText(
//                        this@PostListActivity,
//                        "Waiting for GPS, please wait a few seconds and try again!",
//                        Toast.LENGTH_SHORT
//                ).show()
//            } else {
                val intent = Intent(this, CreatePostActivity::class.java)
                intent.putExtra(PostDetailFragment.AUTH0_ACCESS_TOKEN, AUTH0_ACCESS_TOKEN)
                intent.putExtra(PostDetailFragment.AUTH0_ID_TOKEN, AUTH0_ID_TOKEN)
                startActivity(intent)
//            }
        }

        val post_detail_container = findViewById<NestedScrollView>(R.id.post_detail_container)
        if (post_detail_container != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            twoPane = true
        }

        //Check-in sdk version
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(arrayOf<String>(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                ), 10)
                return
            }
        }

        this.locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        this.locationListener = object : LocationListener {
            override fun onLocationChanged(lc: Location) {
                val dao = AppDatabase.getInstance(this@PostListActivity)?.locationUpdateDao()
                dao?.clear()
                val update = LocationUpdate()
                update.lat = lc.latitude.toString()
                update.lng = lc.longitude.toString()
                dao?.insert(update)
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
            override fun onProviderEnabled(provider: String) {}

            override fun onProviderDisabled(provider: String) {
                val intent: Intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                intent.putExtra(CreatePostActivity.AUTH0_ACCESS_TOKEN, CreatePostActivity.AUTH0_ACCESS_TOKEN)
                intent.putExtra(CreatePostActivity.AUTH0_ID_TOKEN, CreatePostActivity.AUTH0_ID_TOKEN)
                startActivity(intent)
            }
        }


        locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 , 0f, locationListener)

        handleLocation()

        createPostList()

        setupRecyclerView(findViewById(R.id.post_list))
        initializeAuth0()

        getNewPlantTypes()
    }

    private fun handleLocation() {

    }

    private fun getNewPlantTypes() {
        val service = HttpService()
        service.service.getPlantTypes().enqueue(object: Callback<List<String>> {
            override fun onFailure(call: Call<List<String>>?, t: Throwable?) {

            }

            override fun onResponse(call: Call<List<String>>?, response: Response<List<String>>?) {
                response?.body()?.forEach { plant: String ->
                    val dao = AppDatabase.getInstance(this@PostListActivity)?.plantTypeDao()
                    dao?.drop()
                    val defaultPlants = resources.getStringArray(R.array.plant_array).toMutableList()
                    if (!defaultPlants.contains(plant)) {
                        val newType = PlantType()
                        newType.plantType = plant
                        dao?.insert(newType)
                    }
                }
            }
        })
    }

    private fun trySendImagesNotSent() {
        val db = AppDatabase.getInstance(this@PostListActivity)
        val dao = db?.postPackageDao()
        val packages = dao?.getImagesNotSent()
        if (packages != null) {
            for (p in packages) {
                val cm = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

                val connectivity = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val isConnectedToWifi: Boolean =
                        cm.activeNetworkInfo != null &&
                                cm.activeNetworkInfo.isConnected &&
                                cm.activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI

                if(p.lat == null && p.lng == null){
                    val db = AppDatabase.getInstance(this@PostListActivity)
                    val dao = db?.locationUpdateDao()
                    val lastUpdate = dao?.get() ?: return
                    p.lat = lastUpdate.lat
                    p.lng = lastUpdate.lng
                    db?.postPackageDao().update(p)
                }

                if (isConnectedToWifi) {
                    uploadImage(p, dao)
                } else {
                    Toast.makeText(
                            this@PostListActivity,
                            "Please connect to wifi and try again!",
                            Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // Uploads package to backend
    // Asumes network conectivity
    private fun uploadImage(postPackage: PostPackage, dao: PostPackageDAO) {
        val service = HttpService()

        service.service.postImage(
                "Bearer " + AUTH0_ACCESS_TOKEN, postPackage.prepareToUpload(this)
        ).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>?, t: Throwable?) {
                Toast.makeText(
                        this@PostListActivity,
                        "Image not sent! Retrying on next application start",
                        Toast.LENGTH_SHORT
                ).show()

                // TODO: handlear el error
            }

            override fun onResponse(call: Call<String>?, response: Response<String>?) {
                val success: Boolean = response?.isSuccessful() as Boolean
                if (success) {
                    Toast.makeText(
                            this@PostListActivity,
                            "Image sent correctly!",
                            Toast.LENGTH_SHORT
                    ).show()

                    postPackage.sent = true
                    dao.update(postPackage)
                } else {
                    Toast.makeText(
                            this@PostListActivity,
                            "Image not sent! Retrying on next application start",
                            Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    private fun createPostList() {
        packages = null
        PostsObjects.ITEMS = ArrayList()
        val db: AppDatabase = AppDatabase.getInstance(this@PostListActivity) as AppDatabase
//        db.postPackageDao().deleteAll()
        packages = db.postPackageDao().getAll()
        for (p: PostPackage in packages as MutableList) {
            PostsObjects.addItem(p)
        }
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = SimpleItemRecyclerViewAdapter(this, PostsObjects.ITEMS, twoPane)
    }

    class SimpleItemRecyclerViewAdapter(private val parentActivity: PostListActivity,
                                        private val values: MutableList<PostPackage>,
                                        private val twoPane: Boolean) :
            RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

        private val onClickListener: View.OnClickListener

        init {
            onClickListener = View.OnClickListener { v ->
                val item = v.tag as PostPackage
                if (twoPane) {
                    val fragment = PostDetailFragment().apply {
                        arguments = Bundle().apply {
                            putString(PostDetailFragment.ARG_ITEM_ID, item.id.toString())
                        }
                    }
                    parentActivity.supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.post_detail_container, fragment)
                            .commit()
                } else {
                    val intent = Intent(v.context, PostDetailActivity::class.java).apply {
                        putExtra(PostDetailFragment.ARG_ITEM_ID, item.id.toString())
                        putExtra(PostDetailFragment.AUTH0_ACCESS_TOKEN, parentActivity.AUTH0_ACCESS_TOKEN)
                        putExtra(PostDetailFragment.AUTH0_ID_TOKEN, parentActivity.AUTH0_ID_TOKEN)
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
            holder.idView.text = item.id.toString()
            holder.contentView.text = item.type

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        if (loggedIn())
            menu.getItem(0).title = "LOG OUT"
        else
            menu.getItem(0).title = "LOG IN"
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (item.itemId == R.id.action_settings) {
            if (loggedIn())
                logout()
            else
                login()
            return true
        } else {
            return super.onOptionsItemSelected(item)
        }
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
            trySendImagesNotSent()
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


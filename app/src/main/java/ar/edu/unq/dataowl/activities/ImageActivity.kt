package ar.edu.unq.dataowl.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import ar.edu.unq.dataowl.R
import ar.edu.unq.dataowl.model.ImageHandler

class ImageActivity: AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullimage)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val iv = findViewById(R.id.fullImage) as ImageView
        val b = ImageHandler().getBitmapFromLocation(this@ImageActivity, intent.extras.getString("location"))
        iv.setImageBitmap(b)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
            when (item.itemId) {
                android.R.id.home -> {
                   finish()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
}
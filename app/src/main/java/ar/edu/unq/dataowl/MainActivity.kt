package ar.edu.unq.dataowl

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import ar.edu.unq.dataowl.appmodel.MainActivityAppModel

class MainActivity : AppCompatActivity() {

    val appModel: MainActivityAppModel = MainActivityAppModel(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setOnButtonClickListeners()
    }

//    take photo activity result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        appModel.setBitmap(
                findViewById(R.id.imageView_photo),
                this,
                requestCode,
                resultCode,
                data)
    }

//    sets on click button listeners
    fun setOnButtonClickListeners() {
        val openCameraButton: Button = findViewById(R.id.button_openCamera)
        openCameraButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                appModel.deployTakePictureIntent()
            }
        })

        val sendImageaButton: Button = findViewById(R.id.button_sendImage)
        sendImageaButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                appModel.sendImage()
            }
        })
    }

}

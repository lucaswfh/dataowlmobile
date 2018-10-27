package ar.edu.unq.dataowl.persistence

import android.os.Handler
import android.os.HandlerThread

/**
 * Created by wolfx on 27/10/2018.
 */
class DbWorkerThread(threadName: String) : HandlerThread(threadName) {

    private lateinit var mWorkerHandler: Handler

    override fun onLooperPrepared() {
        super.onLooperPrepared()
        mWorkerHandler = Handler(looper)
    }

    fun postTask(task: Runnable) {
        mWorkerHandler.post(task)
    }

}
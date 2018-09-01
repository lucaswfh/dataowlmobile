package ar.edu.unq.dataowl.services

import android.app.Dialog
import android.support.v7.app.AppCompatActivity
import ar.edu.unq.dataowl.R
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.provider.AuthCallback
import com.auth0.android.provider.WebAuthProvider

/**
 * Created by wolfx on 01/09/2018.
 */
class LoginService () {

    fun login(auth0:Auth0,activity:AppCompatActivity ,onSuccessCallback: () -> Unit) {
        WebAuthProvider.init(auth0)
                .withScheme("DataOwlMobile")
                .withAudience(String.format("https://%s/userinfo", activity.getString(R.string.com_auth0_domain)))
                .start(activity, object : AuthCallback {
                    override fun onSuccess(credentials: com.auth0.android.result.Credentials) {
                        // Store credentials
                        // Navigate to your main activity
                        onSuccessCallback()
                    }

                    override fun onFailure(dialog: Dialog) {
                        // Show error Dialog to user
                    }

                    override fun onFailure(exception: AuthenticationException) {
                        // Show error to user
                    }
                })
    }
}
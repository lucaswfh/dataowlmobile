package ar.edu.unq.dataowl.services

import android.app.Dialog
import android.support.v7.app.AppCompatActivity
import ar.edu.unq.dataowl.R
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.authentication.storage.SecureCredentialsManager
import com.auth0.android.authentication.storage.SharedPreferencesStorage
import com.auth0.android.provider.AuthCallback
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.authentication.storage.CredentialsManagerException
import com.auth0.android.callback.BaseCallback
import com.auth0.android.result.Credentials


/**
 * Created by wolfx on 01/09/2018.
 */
class LoginService (val auth0: Auth0, val activity: AppCompatActivity) {

    val KEY_CLEAR_CREDENTIALS = "com.auth0.CLEAR_CREDENTIALS"

    val client = AuthenticationAPIClient(auth0)

    val credentialsManager: SecureCredentialsManager = SecureCredentialsManager(
            activity,
            client,
            SharedPreferencesStorage(activity)
    )

    var loggedIn: Boolean = false

    init {
//        check if the activity was launched after a logout and clear credentials
        if (activity.getIntent().getBooleanExtra(KEY_CLEAR_CREDENTIALS, false))
            credentialsManager.clearCredentials();
    }

//    logs the user in
    fun login(
            onSuccessCallback: () -> Unit = { },
            onFailureCallback: () -> Unit = { })
    {
        if (!credentialsManager.hasValidCredentials())
            promptLoginScreen(onSuccessCallback, onFailureCallback)
        else
            obtainCredentials(onSuccessCallback, onFailureCallback)
    }

//    logs the user out by clearing its credentials
    fun logout() {
        credentialsManager.clearCredentials()
    }

//    prompts login screen
    private fun promptLoginScreen(
            onSuccessCallback: () -> Unit = { },
            onFailureCallback: () -> Unit = { })
    {
        WebAuthProvider.init(auth0)
                .withScheme("DataOwlMobile")
                .withAudience(String.format("https://%s/userinfo", activity.getString(R.string.com_auth0_domain)))
                .withScope("openid offline_access profile email read:current_user update:current_user_metadata")
                .start(activity, object : AuthCallback {
                    override fun onSuccess(credentials: com.auth0.android.result.Credentials) {
//                        store credentials
                        credentialsManager.saveCredentials(credentials)

//                        execute on success callback
                        onSuccessCallback()
                    }

                    // TODO: ver cual de estas 2 hay que usar y cuando

                    override fun onFailure(dialog: Dialog) {
                        // Show error Dialog to user
                        onFailureCallback()
                    }

                    override fun onFailure(exception: AuthenticationException) {
                        // Show error to user
                        onFailureCallback()
                    }
                })
    }

//    tries to retrieve stored credentials and prompts login screen if it fails
    private fun obtainCredentials(
            onSuccessCallback: () -> Unit = { },
            onFailureCallback: () -> Unit = { })
    {
        credentialsManager.getCredentials(object : BaseCallback<Credentials, CredentialsManagerException> {
            override fun onSuccess(credentials: Credentials) {
                onSuccessCallback()
            }

            override fun onFailure(error: CredentialsManagerException) {
                // Credentials could not be refreshed.
                promptLoginScreen(onSuccessCallback, onFailureCallback)
            }
        })
    }
}
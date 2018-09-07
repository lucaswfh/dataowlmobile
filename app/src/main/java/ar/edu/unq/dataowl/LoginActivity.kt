package ar.edu.unq.dataowl

import android.app.Dialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.auth0.android.Auth0
import com.auth0.android.authentication.storage.SecureCredentialsManager
import com.auth0.android.authentication.storage.SharedPreferencesStorage
import com.auth0.android.authentication.AuthenticationAPIClient
import com.auth0.android.authentication.storage.CredentialsManagerException
import com.auth0.android.callback.BaseCallback
import com.auth0.android.result.Credentials
import android.content.Intent
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.provider.AuthCallback
import com.auth0.android.provider.WebAuthProvider
import android.widget.Toast


class LoginActivity : AppCompatActivity() {

    private var auth0: Auth0? = null
    private var credentialsManager: SecureCredentialsManager? = null

    /**
     * Required when setting up Local Authentication in the Credential Manager
     * Refer to SecureCredentialsManager#requireAuthentication method for more information.
     */
    private val CODE_DEVICE_AUTHENTICATION = 22
    private val KEY_CLEAR_CREDENTIALS = "com.auth0.CLEAR_CREDENTIALS"
    private val EXTRA_ACCESS_TOKEN = "com.auth0.ACCESS_TOKEN"
    private val EXTRA_ID_TOKEN = "com.auth0.ID_TOKEN"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup Auth0
        auth0 = Auth0(this)
        auth0?.setLoggingEnabled(true)
        auth0?.setOIDCConformant(true)

        // Setup CredentialsManager
        credentialsManager = SecureCredentialsManager(
                this,
                AuthenticationAPIClient(auth0 as Auth0),
                SharedPreferencesStorage(this)
        )

        credentialsManager?.clearCredentials();

        // Check if the activity was launched after a logout
        if (getIntent().getBooleanExtra(KEY_CLEAR_CREDENTIALS, false)) {
            credentialsManager?.clearCredentials();
        }

        // Check if a log in button must be shown, if not, gets the current credentials
        val manager = credentialsManager as SecureCredentialsManager // needed to use if
        if (!manager.hasValidCredentials()) {
            setContentView(R.layout.activity_login)
            val loginButton = findViewById<Button>(R.id.loginButton) as Button
            loginButton.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    doLogin()
                }
            })
        } else {
            // Obtain the existing credentials and move to the next activity
            credentialsManager?.getCredentials(object : BaseCallback<Credentials, CredentialsManagerException> {
                override fun onSuccess(credentials: Credentials) {
                    showNextActivity(credentials)
                }

                override fun onFailure(error: CredentialsManagerException) {
                    //Authentication cancelled by the user.
                    finish()
                }
            })
        }
    }

    /**
     * Override required when setting up Local Authentication in the Credential Manager
     * Refer to SecureCredentialsManager#requireAuthentication method for more information.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        val manager = credentialsManager as SecureCredentialsManager // needed to use if
        if (manager.checkAuthenticationResult(requestCode, resultCode))
            return

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun showNextActivity(credentials: Credentials) {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        intent.putExtra(EXTRA_ACCESS_TOKEN, credentials.accessToken)
        intent.putExtra(EXTRA_ID_TOKEN, credentials.idToken)
        startActivity(intent)
        finish()
    }

    private fun doLogin() {
        WebAuthProvider.init(auth0 as Auth0)
                .withScheme("DataOwlMobile")
                .withAudience(String.format("https://%s/userinfo", getString(R.string.com_auth0_domain)))
                .withScope("openid offline_access profile email read:current_user update:current_user_metadata")
                .start(this, webCallback());

    }

    fun webCallback(): AuthCallback = object: AuthCallback {
        override fun onSuccess(credentials: Credentials) {
            runOnUiThread {
                Toast.makeText(
                        this@LoginActivity,
                        "Log In - Success",
                        Toast.LENGTH_SHORT
                ).show()
            }

            credentialsManager?.saveCredentials(credentials)
            showNextActivity(credentials)
        }

        override fun onFailure(dialog: Dialog) {
            runOnUiThread {
                dialog.show()
            }
        }

        override fun onFailure(exception: AuthenticationException?) {
            runOnUiThread {
                Toast.makeText(
                        this@LoginActivity,
                        "Log In - Error Occurred",
                        Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

package com.jackz314.keepfit.views

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import com.jackz314.keepfit.GlobalConstants
import com.jackz314.keepfit.R
import us.zoom.sdk.StartMeetingOptions
import us.zoom.sdk.ZoomAuthenticationError
import us.zoom.sdk.ZoomSDK
import us.zoom.sdk.ZoomSDKAuthenticationListener


private const val TAG = "ZoomLoginActivity"

class ZoomLoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zoom_login)
        val url = String.format(GlobalConstants.ZOOM_SSO_AUTH_ENDPOINT, "usc")
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(this, Uri.parse(url))
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        //listen for callback here
        val token = intent?.data?.getQueryParameter("token")
        Log.d(TAG, "onNewIntent: got zoom sso token from url ${intent?.data?.toString()?.split("\\?")?.get(0)}: ${token}")
        Toast.makeText(this, "Got zoom sso token ${token}", Toast.LENGTH_SHORT).show()
        val sdk = ZoomSDK.getInstance()
        sdk.loginWithSSOToken(token)
        sdk.addAuthenticationListener(object : ZoomSDKAuthenticationListener {
            override fun onZoomSDKLoginResult(result: Long) {
                if (result == ZoomAuthenticationError.ZOOM_AUTH_ERROR_SUCCESS.toLong() && sdk.isLoggedIn) {
                    Toast.makeText(this@ZoomLoginActivity, "Login successfully", Toast.LENGTH_SHORT).show()
                    if (sdk.isLoggedIn) {
                        val meetingService = sdk.meetingService
                        val options = StartMeetingOptions()
                        meetingService.startInstantMeeting(this@ZoomLoginActivity, options)
                    }
                } else {
                    Toast.makeText(this@ZoomLoginActivity, "Login failed! Result code = $result", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onZoomSDKLogoutResult(result: Long) {
                TODO("Not yet implemented")
            }

            override fun onZoomIdentityExpired() {
                TODO("Not yet implemented")
            }

            override fun onZoomAuthIdentityExpired() {
                TODO("Not yet implemented")
            }

        })
    }
}
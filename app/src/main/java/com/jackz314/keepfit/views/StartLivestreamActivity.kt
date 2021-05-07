package com.jackz314.keepfit.views

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import com.jackz314.keepfit.GlobalConstants
import com.jackz314.keepfit.Utils
import com.jackz314.keepfit.UtilsKt
import com.jackz314.keepfit.UtilsKt.tryLoginToZoom
import com.jackz314.keepfit.controllers.LivestreamController
import com.jackz314.keepfit.databinding.ActivityZoomLoginBinding
import io.reactivex.rxjava3.disposables.CompositeDisposable
import us.zoom.sdk.*


private const val TAG = "StartLivestreamActivity"

class StartLivestreamActivity : AppCompatActivity(), MeetingServiceListener {

    private lateinit var b: ActivityZoomLoginBinding
    private var waitingForSSOSignin = false
    private lateinit var sdk: ZoomSDK
    private val compositeDisposable = CompositeDisposable()
    private var currMeetingLink = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityZoomLoginBinding.inflate(layoutInflater)
        setContentView(b.root)

        startLoading()

        sdk = ZoomSDK.getInstance()

        val disposable = tryLoginToZoom(sdk).subscribe({ // success
            Log.d(TAG, "Zoom logged in automatically")
            showInMeetingUI()
            startMeeting()
        }, { // failed
            Log.w(TAG, "Zoom auto login failed, showing login UI (${it.message})")
            showLoginUI()
            b.zoomEmailLoginBtn.setOnClickListener {
                val emailStr = b.zoomEmailInput.text
                if (emailStr.trim().isEmpty() || !Utils.isValidEmail(emailStr)) {
                    b.zoomEmailInput.error = "Please enter a valid email"
                } else if (b.zoomPasswordInput.text.trim().isEmpty()) {
                    b.zoomPasswordInput.error = "Please enter a valid password"
                } else {
                    startEmailLogin()
                }
            }

            b.zoomSsoLoginBtn.setOnClickListener {
                if (b.zoomSsoDomainInput.text.trim().isEmpty()) {
                    b.zoomSsoDomainInput.error = "Please enter a valid domain name"
                } else {
                    startSSOLogin()
                }
            }
        })
        compositeDisposable.add(disposable)
    }

    private fun startMeeting() {
        val meetingService = sdk.meetingService
        val options = StartMeetingOptions()
        LivestreamController.setupMeetingOptions(options)
        meetingService.startInstantMeeting(this@StartLivestreamActivity, options)
        meetingService.addListener(this)
    }



    private fun startLoading() {
        b.zoomLoginContainer.visibility = View.INVISIBLE
        b.zoomLoginProgress.visibility = View.VISIBLE
    }

    private fun showLoginUI() {
        b.zoomLoginContainer.visibility = View.VISIBLE
        b.zoomLoginProgress.visibility = View.INVISIBLE
    }
    private fun showInMeetingUI() {
        b.zoomLoginContainer.visibility = View.INVISIBLE
        b.zoomLoginProgress.visibility = View.INVISIBLE
    }

    private fun startEmailLogin() {
        startLoading()
        val disposable = UtilsKt.tryLoginToZoom(sdk, b.zoomEmailInput.text.toString(), b.zoomPasswordInput.text.toString()).subscribe({
            showInMeetingUI()
            Toast.makeText(this@StartLivestreamActivity, "Login successfully", Toast.LENGTH_SHORT).show()
            startMeeting()
        }, {
            showLoginUI()
            Toast.makeText(this@StartLivestreamActivity, it.message, Toast.LENGTH_SHORT).show()
        })
        compositeDisposable.add(disposable)
    }

    private fun startSSOLogin() {
        waitingForSSOSignin = true
        startLoading()
        val url = String.format(GlobalConstants.ZOOM_SSO_AUTH_ENDPOINT, b.zoomSsoDomainInput.text.toString())
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(this, Uri.parse(url))
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        //listen for SSO sign-in callback here
        if(waitingForSSOSignin){ // otherwise ignore
            waitingForSSOSignin = false
            try {
                val token = intent?.data?.getQueryParameter("token")
                Log.d(TAG, "onNewIntent: got zoom sso token from url ${intent?.data?.toString()?.split("\\?")?.get(0)}: ${token}")
                val disposable = UtilsKt.tryLoginToZoom(sdk, ssoToken = token).subscribe({
                    showInMeetingUI()
                    Toast.makeText(this@StartLivestreamActivity, "Login successfully", Toast.LENGTH_SHORT).show()
                    startMeeting()
                }, {
                    showLoginUI()
                    Toast.makeText(this@StartLivestreamActivity, it.message, Toast.LENGTH_SHORT).show()
                })
                compositeDisposable.add(disposable)
            } catch (e: Exception) {
                //probably wrong intent
                Log.e(TAG, "onNewIntent: error getting token from intent: ${intent?.data}", e)
                showLoginUI()
            }
        }
    }

    override fun onDestroy() {
        if (this::sdk.isInitialized && sdk.meetingService != null) {
            sdk.meetingService.removeListener(this)
        }
        if (currMeetingLink.isNotEmpty()) { // just in case
            UtilsKt.removeLivestream(currMeetingLink)
        }
        compositeDisposable.clear()
        super.onDestroy()
    }

    override fun onMeetingStatusChanged(meetingStatus: MeetingStatus?, errorCode: Int, internalErrorCode: Int) {
        Log.d(TAG, "onMeetingStatusChanged, meetingStatus=$meetingStatus, errorCode=$errorCode, internalErrorCode=$internalErrorCode")
        if (meetingStatus == MeetingStatus.MEETING_STATUS_INMEETING && errorCode == MeetingError.MEETING_ERROR_SUCCESS){
            // user joined meeting, publish live stream
            currMeetingLink = sdk.inMeetingService.currentMeetingUrl
            UtilsKt.createLivestream(currMeetingLink, intent.getStringExtra(GlobalConstants.MEDIA_TITLE)?:"Untitled",
                    intent.getStringExtra(GlobalConstants.EXERCISE_TYPE)?:"",
                    intent.getStringExtra(GlobalConstants.MAX_PARTICIPANTS)?:"100", Utils.getHighResProfilePicUrl())
        } else if (meetingStatus == MeetingStatus.MEETING_STATUS_DISCONNECTING) {
            sdk.meetingService.removeListener(this)
            UtilsKt.removeLivestream(currMeetingLink)
            currMeetingLink = ""
            finish()
        }
    }
}
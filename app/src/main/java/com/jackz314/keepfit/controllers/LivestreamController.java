package com.jackz314.keepfit.controllers;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jackz314.keepfit.Utils;
import com.jackz314.keepfit.models.Media;

import java.util.Objects;

import us.zoom.sdk.InviteOptions;
import us.zoom.sdk.JoinMeetingOptions;
import us.zoom.sdk.JoinMeetingParams;
import us.zoom.sdk.MeetingError;
import us.zoom.sdk.MeetingOptions;
import us.zoom.sdk.MeetingService;
import us.zoom.sdk.MeetingServiceListener;
import us.zoom.sdk.MeetingStatus;
import us.zoom.sdk.MeetingViewsOptions;
import us.zoom.sdk.StartMeetingOptions;
import us.zoom.sdk.ZoomApiError;
import us.zoom.sdk.ZoomSDK;

public class LivestreamController implements MeetingServiceListener {

    private static final String TAG = "LivestreamController";

    private final JoinMeetingOptions opts;
    private final ZoomSDK sdk;
    private final Context context;
    private final FirebaseFirestore db;
    private Media livestream;

    public LivestreamController(Context context) {
        this.context = context;
        sdk = ZoomSDK.getInstance();
        opts = new JoinMeetingOptions();
        setupMeetingOptions(opts);
        db = FirebaseFirestore.getInstance();
    }

    public void setLivestream(Media livestream) {
        if (!livestream.isLivestream()) throw new IllegalArgumentException("Media isn't livestream!");
        this.livestream = livestream;
    }

    public void joinLivestream() {
        Log.d(TAG, "joinLivestream: Joining meeting");
        JoinMeetingParams params = LivestreamController.parseMeetingParamsFromLink(livestream.getLink());
        MeetingService meetingService = sdk.getMeetingService();
        if (meetingService == null) {
            Toast.makeText(context, "Zoom SDK unavailable. Try again later!", Toast.LENGTH_SHORT).show();
            return;
        }
        meetingService.addListener(this);
        int result = meetingService.joinMeetingWithParams(context, params, opts);
        if (result != ZoomApiError.ZOOM_API_ERROR_SUCCESS) {
            Log.e(TAG, "joinLivestream: Failed to join meeting, error: " + result);
            Toast.makeText(context, "Failed to join livestream, try again later", Toast.LENGTH_SHORT).show();
        }
    }

    public static JoinMeetingParams parseMeetingParamsFromLink(String link){
        // example: https://usc.zoom.us/j/888888888?pwd=webwehbeqabhhwegg2iwoghwie
        Uri url = Uri.parse(link);
        JoinMeetingParams params = new JoinMeetingParams();
        params.meetingNo = url.getLastPathSegment();
        params.password = url.getQueryParameter("pwd");
        params.displayName = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName();
        return params;
    }

    public static void setupMeetingOptions(MeetingOptions options) {
        options.no_share = true;
        options.no_meeting_end_message = true;
        options.no_driving_mode = true;
        options.invite_options = InviteOptions.INVITE_DISABLE_ALL;
        options.no_invite = true;
        options.meeting_views_options = MeetingViewsOptions.NO_TEXT_MEETING_ID + MeetingViewsOptions.NO_TEXT_PASSWORD;
    }

    @Override
    public void onMeetingStatusChanged(MeetingStatus meetingStatus, int errorCode, int internalErrorCode) {
        Log.d(TAG, "onMeetingStatusChanged, meetingStatus=" + meetingStatus + ", errorCode=" + errorCode
                + ", internalErrorCode=" + internalErrorCode);
        if (meetingStatus == MeetingStatus.MEETING_STATUS_INMEETING && errorCode == MeetingError.MEETING_ERROR_SUCCESS) {
            // increment view count
            db.collection("media").document(Utils.getMD5(livestream.getLink())).update("view_count", FieldValue.increment(1));
        } else if (meetingStatus == MeetingStatus.MEETING_STATUS_DISCONNECTING) {
            db.collection("media").document(Utils.getMD5(livestream.getLink())).update("view_count", FieldValue.increment(-1));
            sdk.getMeetingService().removeListener(this);
            livestream = null;
        }
    }
}

package com.jackz314.keepfit;

public interface GlobalConstants {
    int RC_SIGN_IN = 1087;
    int RC_REAUTH_DELETE = 2019;
    int RC_TRIGGER_REMINDER = 3012;
    int RC_CANCEL_REMINDER = 3013;

    String PRIVACY_POLICY_URL = "https://www.usc.edu/pages/usc-privacy-notice/";
    String TOS_URL = "https://www.usc.edu/pages/usc-privacy-notice/";

    String ZOOM_SSO_AUTH_ENDPOINT = "https://%s.zoom.us/saml/login?from=mobile";

    String ALGOLIA_APP_ID = "YXGIKUNME6";
    String ALGOLIA_INDEX_NAME = "Firestore";

    // shared preferences stuff
    String ZOOM_JWT_TOKEN_KEY = "zoom_jwt_token";
    String ZOOM_SSO_TOKEN_KEY = "zoom_sso_token";
    String ZOOM_USERNAME_KEY = "zoom_username";
    String ZOOM_PASSWORD_KEY = "zoom_pwd";
    String ALGOLIA_SEARCH_KEY = "algolia_search_key";
    String RECENT_EXERCISE_KEY = "recent_exercise";

    // intent stuff
    // extras
    String MEDIA_TITLE = "media_title";
    String EXERCISE_TYPE = "exercise_type";
    String EXERCISE_OBJ = "exercise_obj";
    String EXERCISE_INTENSITY = "exercise_intensity";
    String SEARCH_QUERY = "SEARCH_QUERY";
    String USER_PROFILE = "user_profile";
    String CALENDAR_DATE = "calendar_date";
    String SCHEDULED_EXERCISE = "scheduled_exercise";
    String SCHEDULE_PRESET_DATE = "scheduled_preset_date";
    String ACTION_TRIGGER_REMINDER = "trigger_reminder";
    String ACTION_CANCEL_REMINDER = "cancel_reminder";

    // actions
    String ACTION_EXERCISE = "action_exercise";
    String ACTION_LIVESTREAM = "action_live";

    //notifications
    String REMINDER_NOTIF_CHANNEL_ID = "reminder_notif_channel";
}

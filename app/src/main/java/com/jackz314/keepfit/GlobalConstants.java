package com.jackz314.keepfit;

public interface GlobalConstants {
    int RC_SIGN_IN = 100;
    int RC_REAUTH_DELETE = 200;

    String PRIVACY_POLICY_URL = "https://www.usc.edu/pages/usc-privacy-notice/";
    String TOS_URL = "https://www.usc.edu/pages/usc-privacy-notice/";

    String ZOOM_SSO_AUTH_ENDPOINT = "https://%s.zoom.us/saml/login?from=mobile";

    // shared preferences stuff
    String ZOOM_JWT_TOKEN_KEY = "zoom_jwt_token";
    String ZOOM_SSO_TOKEN_KEY = "zoom_sso_token";
    String ZOOM_USERNAME_KEY = "zoom_username";
    String ZOOM_PASSWORD_KEY = "zoom_pwd";

    // intent stuff
    String MEDIA_TITLE = "media_title";
    String EXERCISE_TYPE = "exercise_type";
    String EXERCISE_OBJ = "exercise_obj";
    String EXERCISE_INTENSITY = "exercise_intensity";

    String ACTION_EXERCISE = "action_exercise";
    String ACTION_LIVESTREAM = "action_live";
}

package com.jackz314.keepfit.views.other;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.KeyEvent;
import android.view.View;
import android.widget.MediaController;

// from https://stackoverflow.com/a/62715887/8170714
public class BackPressingMediaController extends MediaController {
    private final Activity mParentActivity;

    public BackPressingMediaController(Context context, Activity parentActivity) {
        super(context);
        mParentActivity = parentActivity;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            OnUnhandledKeyEventListener eventListener = (v, event) -> {
                boolean fHandled = false;

                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        fHandled =  true;
                    } else if (event.getAction() == KeyEvent.ACTION_UP) {
                        if(mParentActivity != null) {
                            mParentActivity.onBackPressed();
                            fHandled = true;
                        }
                    }
                }
                return(fHandled);
            };
            addOnUnhandledKeyEventListener(eventListener);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean fHandled = false;
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                fHandled =  true;
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                mParentActivity.onBackPressed();
                fHandled = true;
            }
        }
        if(!fHandled) {
            fHandled = super.dispatchKeyEvent(event);
        }
        return(fHandled);
    }


}
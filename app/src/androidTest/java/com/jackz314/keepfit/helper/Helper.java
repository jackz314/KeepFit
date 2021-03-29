package com.jackz314.keepfit.helper;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.rule.ActivityTestRule;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jackz314.keepfit.Utils;
import com.jackz314.keepfit.views.MainActivity;

import org.hamcrest.Matcher;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;

public class Helper {

    public static Task<Void> createOrSignInTempAccount(String email, String password) {
        return FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).continueWithTask(task -> {
            if (task.isSuccessful()){
                Map<String, Object> user = new HashMap<>();
                user.put( "biography", "test account");
                user.put("birthday", new Date(2000,1,1));
                user.put("email", email);
                user.put("height", 180);
                user.put("name", "Test Account");
                user.put("profile_pic", "");
                user.put("sex", true);
                user.put("weight", 75);
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                return db.collection("users")
                        .document(task.getResult().getUser().getUid())
                        .set(user);   
            } else {
                return FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).continueWithTask(task1 -> {
                    Map<String, Object> user = new HashMap<>();
                    user.put( "biography", "test account");
                    user.put("birthday", new Date(2000,1,1));
                    user.put("email", email);
                    user.put("height", 180);
                    user.put("name", "Test Account");
                    user.put("profile_pic", "");
                    user.put("sex", true);
                    user.put("weight", 75);
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    return db.collection("users")
                            .document(task1.getResult().getUser().getUid())
                            .set(user);
                });
            }
        });
        
    }

    public static void signOut(Context context) throws ExecutionException, InterruptedException {
        Tasks.await(AuthUI.getInstance().signOut(context));
    }

    public static void signIn(String email, String password) throws ExecutionException, InterruptedException {
        Tasks.await(FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password));
    }

    public static String getTextfromTextView(final Matcher<View> matcher) {
        final String[] stringHolder = { null };
        onView(matcher).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(TextView.class);
            }

            @Override
            public String getDescription() {
                return "getting text from a TextView";
            }

            @Override
            public void perform(UiController uiController, View view) {
                TextView tv = (TextView)view; //Save, because of check in getConstraints()
                stringHolder[0] = tv.getText().toString();
            }
        });
        return stringHolder[0];
    }
}

package com.jackz314.keepfit.views;


import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.NoMatchingRootException;
import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.helper.Helper;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class DeleteAccountTest {

    private static final String TAG = "DeleteAccountTest";

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class, true, false);

    @Test
    public void deleteAccountTest() throws InterruptedException, ExecutionException {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        FirebaseApp.initializeApp(appContext);
        String testemail = "deletethis@gmail.com";
        String testpassword = "delete";
        Tasks.await(Helper.createTempAccount(testemail, testpassword));
        String oldUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        Tasks.await(AuthUI.getInstance().signOut(appContext));
//        Tasks.await(AuthUI.getInstance().silentSignIn(appContext, Collections.singletonList(new AuthUI.IdpConfig.EmailBuilder().build())));

        mActivityTestRule.launchActivity(new Intent());

        Thread.sleep(2000);

        try {
            ViewInteraction overflowMenuButton = onView(
                    allOf(withContentDescription("More options"),
                            childAtPosition(
                                    childAtPosition(
                                            withId(R.id.action_bar),
                                            1),
                                    2),
                            isDisplayed()));
            overflowMenuButton.perform(click());
        } catch (Exception ignored){
            pressBack();
            Thread.sleep(1000);
        }

        ViewInteraction materialTextView = onView(
                allOf(withId(R.id.title), withText("Delete Account"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.content),
                                        1),
                                0),
                        isDisplayed()));
        materialTextView.perform(click());

        ViewInteraction materialButton3 = onView(
                allOf(withId(android.R.id.button1), withText("OK"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.buttonPanel),
                                        0),
                                3)));
        materialButton3.perform(scrollTo(), click());

        Thread.sleep(3000);

        Tasks.await(Helper.createTempAccount(testemail, testpassword));

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentSnapshot ds = Tasks.await(db.collection("users").document(oldUid).get());
        assertFalse(ds.exists());
//        Tasks.await(FirebaseAuth.getInstance().getCurrentUser().delete());
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}

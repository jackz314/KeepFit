package com.jackz314.keepfit.views;


import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.TestIdlingResource;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URISyntaxException;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static java.lang.System.currentTimeMillis;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class UploadVideoTest {

    private static final String TAG = "UploadVideoTest";
    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private IdlingResource idlingResource;

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

    @Before
    public void beforeClass() throws Exception {
//        FirebaseApp.initializeApp(context);
        idlingResource = TestIdlingResource.countingIdlingResource;
        IdlingRegistry.getInstance().register(idlingResource);
    }

    @Test
    public void uploadVideoTest() throws InterruptedException, IOException, URISyntaxException {
        final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Intents.init();

        ViewInteraction cardView = onView(
                allOf(withId(R.id.fab_card),
                        childAtPosition(
                                allOf(withId(R.id.container),
                                        childAtPosition(
                                                withId(R.id.main_action_btn),
                                                0)),
                                1),
                        isDisplayed()));
        cardView.perform(click());

        ViewInteraction linearLayout = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.container),
                                childAtPosition(
                                        withId(R.id.main_action_btn),
                                        0)),
                        4),
                        isDisplayed()));
        linearLayout.perform(click());

        Long current = currentTimeMillis();
        String curString = current.toString();
        Log.d(TAG, current.toString());

        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.video_title_input),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                2),
                        isDisplayed()));
        appCompatEditText.perform(replaceText(curString), closeSoftKeyboard());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.categories_input),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                3),
                        isDisplayed()));
        appCompatEditText2.perform(replaceText("Upload"), closeSoftKeyboard());

        String fileName = "smallsize.mp4";

//        // match file picker intent
        Intent resultData = new Intent();
        Uri uri1 = Uri.parse("android.resource://" + context.getPackageName() + "/raw/smallsize.mp4");
        // Create the Intent that will include the bundle.
        resultData.setData(uri1);

        // todo not working
//        intending(hasAction(Intent.ACTION_CHOOSER)).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData));

        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.choose_file_to_upload),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText3.perform(click());

//        Thread.sleep(1000);

        ViewInteraction materialButton3 = onView(
                allOf(withId(R.id.btn_video_upload), withText("Upload Video"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                1),
                        isDisplayed()));
        materialButton3.perform(click());

        // Wait for the file to be uploaded
        Thread.sleep(10000);


        // Check if the uploaded file exists in firebase
        db.collection("media")
                .whereEqualTo("title", curString)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "SUCCESS! " + task.getResult().size() + " " + curString);

                            if (task.getResult().size() > 0) {

                                Log.d(TAG, "FOUND! ");
                                assertTrue(true);
                            } else {
                                fail();
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        Thread.sleep(2000);
    }


}

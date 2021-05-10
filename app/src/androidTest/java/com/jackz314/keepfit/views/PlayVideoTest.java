package com.jackz314.keepfit.views;


import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.jackz314.keepfit.R;
import com.jackz314.keepfit.TestIdlingResource;
import com.jackz314.keepfit.helper.Helper;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class PlayVideoTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class, true, false);

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
        final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
//        FirebaseApp.initializeApp(context);
        idlingResource = TestIdlingResource.countingIdlingResource;
        IdlingRegistry.getInstance().register(idlingResource);
    }

    @Test
    public void playVideoTest() throws InterruptedException, ExecutionException {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Helper.signOut(appContext);
        Helper.signIn("videotester@email.com", "123456");
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Log.d("WWWWWWWWWWWWWWW", "loggegd in!");
        mActivityTestRule.launchActivity(new Intent());
        Thread.sleep(2000);

        // open my videos feed
        ViewInteraction tabView = onView(
                allOf(withContentDescription("My Videos"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.me_tab_layout),
                                        0),
                                1),
                        isDisplayed()));
        tabView.perform(click());

        instrumentation.waitForIdleSync();


        Thread.sleep(0x64);


        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.feed_recycler),
                        childAtPosition(
                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                0)));
        recyclerView.perform(actionOnItemAtPosition(0, click()));


        Thread.sleep(1000);


        ViewInteraction videoView = onView(
                allOf(withId(R.id.video_view),
                        withParent(allOf(withId(R.id.relative_parent),
                                withParent(withId(android.R.id.content)))),
                        isDisplayed()));
    }
}

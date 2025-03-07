package com.jackz314.keepfit.views;


import android.content.Context;
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

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.TestIdlingResource;
import com.jackz314.keepfit.Utils;
import com.jackz314.keepfit.UtilsKt;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.jackz314.keepfit.helper.RecyclerViewItemCountAssertion.withItemCount;
import static com.jackz314.keepfit.helper.RecyclerViewMatcher.withRecyclerView;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class LivestreamTest {

    @Rule
    public ActivityTestRule<MainActivity> mainActivityTestRule = new ActivityTestRule<>(MainActivity.class);

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
    public void beforeClass() {
        final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
//        FirebaseApp.initializeApp(context);
        idlingResource = TestIdlingResource.countingIdlingResource;
        IdlingRegistry.getInstance().register(idlingResource);
    }

    @Test
    public void livestreamFlow() throws InterruptedException, ExecutionException {
//        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
//        instrumentation.waitForIdleSync();

        String link = "https://usc.zoom.us/j/8888888888";
        String title = "Test Livestream Pgxqc4KqCt";
        UtilsKt.createLivestream(link, title, "Cool, fun, test", "");

        ViewInteraction bottomNavigationItemView = onView(
                allOf(withId(R.id.navigation_feed), withContentDescription("Feed"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.nav_view),
                                        0),
                                0),
                        isDisplayed()));
        bottomNavigationItemView.perform(click());

        Thread.sleep(1500);

        onView(withId(R.id.feed_recycler)).check(withItemCount(greaterThanOrEqualTo(1)));
        ViewInteraction livestreamEntry = onView(withRecyclerView(R.id.feed_recycler).atPosition(0));
        livestreamEntry.check(matches(hasDescendant(withText(title))));
        livestreamEntry.check(matches(hasDescendant(withText("LIVE"))));
        String displayName = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName();
        if (displayName != null && !displayName.trim().isEmpty())
            livestreamEntry.check(matches(hasDescendant(withText(containsString(displayName)))));

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentSnapshot ds = Tasks.await(db.collection("media").document(Utils.getMD5(link)).get());
        assertTrue(ds.exists());
        assertEquals(true, ds.getBoolean("is_livestream"));

        UtilsKt.removeLivestream(link);

        Thread.sleep(1000);

        ViewInteraction newLivestreamEntry = onView(withRecyclerView(R.id.feed_recycler).atPosition(0));
        newLivestreamEntry.check(matches(not(hasDescendant(withText(title)))));
        ds = Tasks.await(db.collection("media").document(Utils.getMD5(link)).get());
        assertFalse(ds.exists());
    }

    @Test
    public void livestreamValidation() throws InterruptedException {
//        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
//        instrumentation.waitForIdleSync();

        String link = ""; // empty, shouldn't add
        String title = "Test Livestream XtUazgCDc9";
        UtilsKt.createLivestream(link, title, "Cool, fun, test", "");

        ViewInteraction bottomNavigationItemView = onView(
                allOf(withId(R.id.navigation_feed), withContentDescription("Feed"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.nav_view),
                                        0),
                                0),
                        isDisplayed()));
        bottomNavigationItemView.perform(click());

        Thread.sleep(2000);

        ViewInteraction livestreamEntry = onView(withRecyclerView(R.id.feed_recycler).atPosition(0));
        livestreamEntry.check(matches(hasDescendant(not(withText(title)))));

        link = "AUSHFJASVIOSJ";
        title = "Test Livestream sx3naxtFhp";
        UtilsKt.createLivestream(link, title, "Cool, fun, test", "");

        Thread.sleep(1500);

        ViewInteraction newLivestreamEntry = onView(withRecyclerView(R.id.feed_recycler).atPosition(0));
        newLivestreamEntry.check(matches(hasDescendant(not(withText(title)))));
    }
}

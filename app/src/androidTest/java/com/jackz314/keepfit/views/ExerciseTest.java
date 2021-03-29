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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.TestIdlingResource;
import com.jackz314.keepfit.Utils;
import com.jackz314.keepfit.controllers.UserControllerKt;

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
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.jackz314.keepfit.helper.RecyclerViewItemCountAssertion.withItemCount;
import static com.jackz314.keepfit.helper.RecyclerViewMatcher.withRecyclerView;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ExerciseTest {

    @Rule
    public ActivityTestRule<MainActivity> mainActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    private IdlingResource idlingResource;

    @Before
    public void before() {
        final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
//        FirebaseApp.initializeApp(context);
        idlingResource = TestIdlingResource.countingIdlingResource;
        IdlingRegistry.getInstance().register(idlingResource);
    }

    @Test
    public void exerciseCompleteFlow() throws InterruptedException, ExecutionException {
//        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
//        instrumentation.waitForIdleSync();

        // open fab menu
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
                        2),
                        isDisplayed()));
        linearLayout.perform(click());

        // enter prompt info
        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.prompt_category),
                        childAtPosition(
                                allOf(withId(R.id.container),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                1),
                        isDisplayed()));
        String category = "test 9230048693486734";
        appCompatEditText.perform(replaceText(category), closeSoftKeyboard());

        ViewInteraction chip = onView(
                allOf(withId(R.id.prompt_intensity_low), withText("Light"),
                        childAtPosition(
                                allOf(withId(R.id.prompt_exercise_intensity),
                                        childAtPosition(
                                                withId(R.id.container),
                                                5)),
                                0),
                        isDisplayed()));
        chip.perform(click());

        onView(withId(R.id.prompt_start_btn)).perform(click());

        ViewInteraction stopBtn = onView(withId(R.id.exercise_stop_btn));
        stopBtn.check(matches(isDisplayed()));
        Thread.sleep(1000);
        stopBtn.perform(click());

        ViewInteraction bottomNavigationItemView2 = onView(
                allOf(withId(R.id.navigation_me), withContentDescription("Me"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.nav_view),
                                        0),
                                1),
                        isDisplayed()));
        bottomNavigationItemView2.perform(click());

        // verify exercise is recorded in log
        onView(withId(R.id.exercise_log_recycler)).check(withItemCount(greaterThanOrEqualTo(1)));
        ViewInteraction newExerciseEntry = onView(withRecyclerView(R.id.exercise_log_recycler).atPosition(0));
        newExerciseEntry.check(matches(hasDescendant(withText(Utils.toTitleCase(category)))));

        QuerySnapshot qs = Tasks.await(UserControllerKt.getCurrentUserDoc().collection("exercises").whereEqualTo("category", Utils.toTitleCase(category)).get());
        assertFalse(qs.isEmpty());

        // click into detail and delete
        newExerciseEntry.perform(click());

        onView(withId(R.id.delete_exercise_btn)).check(matches(isDisplayed())).perform(click());

        Thread.sleep(500);

        qs = Tasks.await(UserControllerKt.getCurrentUserDoc().collection("exercises").whereEqualTo("category", Utils.toTitleCase(category)).get());
        assertTrue(qs.isEmpty());
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

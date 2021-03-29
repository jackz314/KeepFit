package com.jackz314.keepfit.views;


import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
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
import com.jackz314.keepfit.GlobalConstants;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.TestIdlingResource;

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
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.jackz314.keepfit.helper.RecyclerViewItemCountAssertion.withItemCount;
import static com.jackz314.keepfit.helper.RecyclerViewMatcher.withRecyclerView;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertTrue;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class PromptTest {

    @Rule
    public ActivityTestRule<PromptActivity> promptActivityTestRule = new ActivityTestRule<>(PromptActivity.class, true, false);

    @Test
    public void promptCategory() {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        promptActivityTestRule.launchActivity(new Intent(GlobalConstants.ACTION_EXERCISE));
        instrumentation.waitForIdleSync();
        onView(withId(R.id.prompt_description)).check(matches(withText("Track Exercise")));
        onView(withId(R.id.prompt_title)).check(matches(not(isDisplayed())));

        promptActivityTestRule.launchActivity(new Intent(GlobalConstants.ACTION_LIVESTREAM));
        instrumentation.waitForIdleSync();
        onView(withId(R.id.prompt_description)).check(matches(withText("Go Live")));
        onView(withId(R.id.prompt_title)).check(matches(isDisplayed()));
        promptActivityTestRule.finishActivity();
    }

    @Test
    public void promptInput() throws InterruptedException {
        promptActivityTestRule.launchActivity(new Intent(GlobalConstants.ACTION_LIVESTREAM));
//        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
//        instrumentation.waitForIdleSync();

        // enter prompt info
        ViewInteraction titleText = onView(withId(R.id.prompt_title));
        ViewInteraction categoryText = onView(
                allOf(withId(R.id.prompt_category),
                        childAtPosition(
                                allOf(withId(R.id.container),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                1),
                        isDisplayed()));
        ViewInteraction startBtn = onView(withId(R.id.prompt_start_btn));

        titleText.perform(replaceText("Test"), closeSoftKeyboard());
        startBtn.check(matches(not(isEnabled())));

        titleText.perform(replaceText("  "), closeSoftKeyboard());
        startBtn.check(matches(not(isEnabled())));
        titleText.check(matches(hasErrorText("Please enter a valid title.")));

        categoryText.perform(replaceText("  "), closeSoftKeyboard());
        startBtn.check(matches(not(isEnabled())));
        titleText.check(matches(hasErrorText("Please enter a valid title.")));
        categoryText.check(matches(hasErrorText("Please enter valid categories.")));

        titleText.perform(replaceText("Cool beans"), closeSoftKeyboard());
        categoryText.perform(replaceText("  "), closeSoftKeyboard());
        Thread.sleep(3000);
        startBtn.check(matches(not(isEnabled())));
        titleText.check(matches(not(hasErrorText("Please enter a valid title."))));
        categoryText.check(matches(hasErrorText("Please enter valid categories.")));

        categoryText.perform(replaceText("Cool, beans"), closeSoftKeyboard());
        startBtn.check(matches(isEnabled()));
        titleText.check(matches(not(hasErrorText("Please enter a valid title."))));
        categoryText.check(matches(not(hasErrorText("Please enter valid categories."))));

        titleText.perform(replaceText(""), closeSoftKeyboard());
        startBtn.check(matches(not(isEnabled())));
        titleText.check(matches(hasErrorText("Please enter a valid title.")));
        categoryText.check(matches(not(hasErrorText("Please enter valid categories."))));

        titleText.perform(replaceText("Cool beans"), closeSoftKeyboard());
        categoryText.perform(replaceText(" "), closeSoftKeyboard());
        startBtn.check(matches(not(isEnabled())));
        titleText.check(matches(not(hasErrorText("Please enter a valid title."))));
        categoryText.check(matches(hasErrorText("Please enter valid categories.")));
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

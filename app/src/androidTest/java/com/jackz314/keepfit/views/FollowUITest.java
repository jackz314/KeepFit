package com.jackz314.keepfit.views;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;
import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import com.jackz314.keepfit.R;
import com.jackz314.keepfit.TestIdlingResource;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class FollowUITest {

  private IdlingResource idlingResource;

    @Before
    public void beforeClass() throws Exception {
        final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        idlingResource = TestIdlingResource.countingIdlingResource;
        IdlingRegistry.getInstance().register(idlingResource);
    }

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void followUITest() throws InterruptedException {
        ViewInteraction bottomNavigationItemView = onView(
                allOf(withId(R.id.navigation_feed), withContentDescription("Feed"),
                        isDisplayed()));
        bottomNavigationItemView.perform(click());

        ViewInteraction actionMenuItemView = onView(
                allOf(withId(R.id.app_bar_search), withContentDescription("Search"),
                        isDisplayed()));
        actionMenuItemView.perform(click());

        ViewInteraction searchAutoComplete = onView(
                allOf(withClassName(is("android.widget.SearchView$SearchAutoComplete")),
                        isDisplayed()));
        searchAutoComplete.perform(replaceText("a"), closeSoftKeyboard());

        ViewInteraction searchAutoComplete2 = onView(
                allOf(withText("a"),
                        isDisplayed()));
        searchAutoComplete2.perform(pressImeActionButton());

        Thread.sleep(5000);
        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.search_recycler),
                        childAtPosition(
                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                2)));
        recyclerView.perform(actionOnItemAtPosition(1, click()));

        pressBack();

        ViewInteraction recyclerView2 = onView(
                allOf(withId(R.id.search_recycler),
                        childAtPosition(
                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                2)));
        recyclerView2.perform(actionOnItemAtPosition(0, click()));

        Boolean following;
        Thread.sleep(3000);
        try {
            //currently not following user
            onView(allOf(
                    withId(R.id.followButton), withText("+")
            )).check(matches(isDisplayed()));
            following = false;
        } catch (NoMatchingViewException e) {
            //currently following user
            following = true;
        }


        ViewInteraction materialButton = onView(allOf(withId(R.id.followButton)));
        materialButton.perform(scrollTo(), click());

        if (following) { //case if user was previously following user
            onView(allOf(
                    withId(R.id.followButton), withText("+")
            )).check(matches(isDisplayed()));
        } else {
            onView(allOf(
                    withId(R.id.followButton), withText("-")
            )).check(matches(isDisplayed()));
        }


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

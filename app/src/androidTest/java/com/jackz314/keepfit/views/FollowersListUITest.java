package com.jackz314.keepfit.views;


import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.jackz314.keepfit.R;
import com.jackz314.keepfit.SimpleCountingIdlingResource;
import com.jackz314.keepfit.TestIdlingResource;
import com.jackz314.keepfit.controllers.UserController;
import com.jackz314.keepfit.helper.Helper;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.jackz314.keepfit.helper.SelectTabAtPositionKt.selectTabAtPosition;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class FollowersListUITest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class, true, false);

    @Before
    public void before() {
        SimpleCountingIdlingResource idlingResource = TestIdlingResource.countingIdlingResource;
        IdlingRegistry.getInstance().register((IdlingResource) idlingResource);
    }

    @Test
    public void followersListUITest() throws InterruptedException, ExecutionException {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Helper.signIn("otherFollowersTest@UI.com", "123456");
        mActivityTestRule.launchActivity(new Intent());
//        Thread.sleep(500);
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        ViewInteraction bottomNavigationItemView = onView(
                allOf(withId(R.id.navigation_feed), withContentDescription("Feed"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.nav_view),
                                        0),
                                0),
                        isDisplayed()));
        bottomNavigationItemView.perform(click());

        Thread.sleep(500);

        ViewInteraction actionMenuItemView = onView(
                allOf(withId(R.id.app_bar_search), withContentDescription("Search"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.action_bar),
                                        1),
                                0),
                        isDisplayed()));
        actionMenuItemView.perform(click());

        Thread.sleep(500);

        ViewInteraction searchAutoComplete = onView(
                allOf(withClassName(is("android.widget.SearchView$SearchAutoComplete")),
                        childAtPosition(
                                allOf(withClassName(is("android.widget.LinearLayout")),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                0),
                        isDisplayed()));
        searchAutoComplete.perform(replaceText("Bobby"), closeSoftKeyboard());

        ViewInteraction searchAutoComplete2 = onView(
                allOf(withClassName(is("android.widget.SearchView$SearchAutoComplete")), withText("Bobby"),
                        childAtPosition(
                                allOf(withClassName(is("android.widget.LinearLayout")),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                0),
                        isDisplayed()));
        searchAutoComplete2.perform(pressImeActionButton());

        Thread.sleep(1000);

        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.search_recycler),
                        childAtPosition(
                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                2)));
        recyclerView.perform(actionOnItemAtPosition(0, click()));

        try{
            ViewInteraction materialButton = onView(
                    allOf(withId(R.id.followButton), withText("+"),
                            childAtPosition(
                                    childAtPosition(
                                            withClassName(is("android.widget.ScrollView")),
                                            0),
                                    3)));
            materialButton.perform(scrollTo(), click());
        } catch (Exception ignored){
            ViewInteraction materialButton = onView(
                    allOf(withId(R.id.followButton),
                            childAtPosition(
                                    childAtPosition(
                                            withClassName(is("android.widget.ScrollView")),
                                            0),
                                    3)));
            materialButton.perform(scrollTo(), click());

            Thread.sleep(400);

            materialButton.perform(scrollTo(), click());
        }

        ViewInteraction appCompatImageButton = onView(
                allOf(withId(R.id.imageButton), withContentDescription("Exits profile"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                11)));
        appCompatImageButton.perform(scrollTo(), click());

        ViewInteraction appCompatImageButton2 = onView(
                allOf(withId(R.id.imageButton2),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                4),
                        isDisplayed()));
        appCompatImageButton2.perform(click());

//        ViewInteraction bottomNavigationItemView2 = onView(
//                allOf(withId(R.id.navigation_me), withContentDescription("Me"),
//                        childAtPosition(
//                                childAtPosition(
//                                        withId(R.id.nav_view),
//                                        0),
//                                1),
//                        isDisplayed()));
//        bottomNavigationItemView2.perform(click());
//
//        ViewInteraction actionMenuItemView2 = onView(
//                allOf(withId(R.id.sign_out_btn), withContentDescription("Sign Out"),
//                        childAtPosition(
//                                childAtPosition(
//                                        withId(R.id.action_bar),
//                                        1),
//                                0),
//                        isDisplayed()));
//        actionMenuItemView2.perform(click());
//
//        ViewInteraction materialButton4 = onView(
//                allOf(withId(android.R.id.button1), withText("OK"),
//                        childAtPosition(
//                                childAtPosition(
//                                        withId(R.id.buttonPanel),
//                                        0),
//                                3)));
//        materialButton4.perform(scrollTo(), click());
//        Thread.sleep(1000);

        mActivityTestRule.finishActivity();
        Helper.signIn("followerstest@UI.com", "123456");
        Thread.sleep(500);
        mActivityTestRule.launchActivity(new Intent());

        Thread.sleep(1500);

        onView(withId(R.id.me_tab_layout)).perform(selectTabAtPosition(3));

        Thread.sleep(1500);

        ViewInteraction viewGroup = onView(
                allOf(withParent(allOf(withId(R.id.search_recycler),
                        withParent(IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class)))),
                        isDisplayed()));
        viewGroup.check(matches(isDisplayed()));

        Thread.sleep(1000);
        ViewInteraction recyclerView2 = onView(
                allOf(withId(R.id.search_recycler),
                        childAtPosition(
                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                1)));
        recyclerView2.perform(actionOnItemAtPosition(0, click()));

        Thread.sleep(1000);
        ViewInteraction appCompatImageButton3 = onView(
                allOf(withId(R.id.imageButton), withContentDescription("Exits profile"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                11)));
        appCompatImageButton3.perform(scrollTo(), click());

//        Thread.sleep(1000);
//        ViewInteraction tabView3 = onView(
//                allOf(withContentDescription("Info")));
//        tabView3.perform(scrollTo(), click());
//
//        Thread.sleep(1000);
//        ViewInteraction actionMenuItemView3 = onView(
//                allOf(withId(R.id.sign_out_btn), withContentDescription("Sign Out"),
//                        childAtPosition(
//                                childAtPosition(
//                                        withId(R.id.action_bar),
//                                        1),
//                                0),
//                        isDisplayed()));
//        actionMenuItemView3.perform(click());
//
//        Thread.sleep(1000);
//        ViewInteraction materialButton7 = onView(
//                allOf(withId(android.R.id.button1), withText("OK"),
//                        childAtPosition(
//                                childAtPosition(
//                                        withId(R.id.buttonPanel),
//                                        0),
//                                3)));
//        materialButton7.perform(scrollTo(), click());

        mActivityTestRule.finishActivity();
        Helper.signIn("otherFollowersTest@UI.com", "123456");
        Thread.sleep(500);
        mActivityTestRule.launchActivity(new Intent());

        Thread.sleep(1500);

        onView(withId(R.id.me_tab_layout)).perform(selectTabAtPosition(4));

        Thread.sleep(1500);
        ViewInteraction recyclerView3 = onView(
                allOf(withId(R.id.search_recycler),
                        childAtPosition(
                                withClassName(is("androidx.constraintlayout.widget.ConstraintLayout")),
                                1)));
        recyclerView3.perform(actionOnItemAtPosition(0, click()));

        Thread.sleep(1000);
        ViewInteraction materialButton10 = onView(
                allOf(withId(R.id.followButton), withText("-")));
        materialButton10.perform(click());

        Thread.sleep(1000);
        ViewInteraction appCompatImageButton4 = onView(
                allOf(withId(R.id.imageButton), withContentDescription("Exits profile"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0),
                                11)));
        appCompatImageButton4.perform(scrollTo(), click());
        mActivityTestRule.finishActivity();

//        intending(hasAction(Intent.ACTION_CHOOSER)).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));

        Helper.signIn("followerstest@UI.com", "123456");
        Thread.sleep(500);
        mActivityTestRule.launchActivity(new Intent());

        Thread.sleep(1500);

        onView(withId(R.id.me_tab_layout)).perform(selectTabAtPosition(3));

        Thread.sleep(1500);

        ViewInteraction viewGroup2 = onView(
                allOf(withParent(allOf(withId(R.id.search_recycler),
                        withParent(IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class)))),
                        isDisplayed()));
        viewGroup2.check(doesNotExist());
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

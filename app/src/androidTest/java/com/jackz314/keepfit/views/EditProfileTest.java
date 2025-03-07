package com.jackz314.keepfit.views;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

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

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class EditProfileTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

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
    public void before() {
        IdlingResource idlingResource = TestIdlingResource.countingIdlingResource;
        IdlingRegistry.getInstance().register(idlingResource);
    }

    @Test
    public void editProfileEmptyInputTest() throws InterruptedException {
        ViewInteraction actionMenuItemView = onView(
                allOf(withId(R.id.edit_profile_btn), withContentDescription("Edit Profile"),
                        isDisplayed()));
        actionMenuItemView.perform(click());

        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.editTextTextBio),
                        isDisplayed()));
        appCompatEditText.perform(replaceText("hey"), closeSoftKeyboard());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.editTextTextBio), withText("hey"),
                        isDisplayed()));
        appCompatEditText2.perform(pressImeActionButton());

        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.editTextHeight),
                        isDisplayed()));
        appCompatEditText3.perform(pressImeActionButton());

        ViewInteraction materialButton = onView(withId(R.id.finish_new_user_btn));
        materialButton.perform(click());
        Thread.sleep(1000);
        ViewInteraction actionMenuItemView2 = onView(
                allOf(withId(R.id.edit_profile_btn), withContentDescription("Edit Profile"),
                        isDisplayed()));
        actionMenuItemView2.perform(click());

        ViewInteraction materialButton2 = onView(
                allOf(withId(R.id.finish_new_user_btn), withText("Done"),
                        isDisplayed()));
        materialButton2.perform(click());
        Thread.sleep(1000);
        ViewInteraction textView = onView(
                allOf(withId(R.id.user_biography_text),
                        isDisplayed()));
        textView.check(matches(withText("hey")));
    }

    @Test
    public void editProfileNewBioTest() throws InterruptedException {
        ViewInteraction actionMenuItemView = onView(
                allOf(withId(R.id.edit_profile_btn), withContentDescription("Edit Profile"),
                        isDisplayed()));
        actionMenuItemView.perform(click());

        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.editTextUsername),
                        isDisplayed()));
        appCompatEditText.perform(replaceText("B"), closeSoftKeyboard());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.editTextTextBio),
                        isDisplayed()));
        appCompatEditText2.perform(replaceText("hello"), closeSoftKeyboard());

        ViewInteraction appCompatSpinner = onView(
                allOf(withId(R.id.sex),
                        isDisplayed()));
        appCompatSpinner.perform(click());

        DataInteraction appCompatCheckedTextView = onData(anything())
                .inAdapterView(childAtPosition(
                        withClassName(is("android.widget.PopupWindow$PopupBackgroundView")),
                        0))
                .atPosition(1);
        appCompatCheckedTextView.perform(click());

        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.editTextTextBio), withText("hello"),
                        isDisplayed()));
        appCompatEditText3.perform(pressImeActionButton());

        ViewInteraction appCompatEditText4 = onView(
                allOf(withId(R.id.editTextHeight),
                        isDisplayed()));
        appCompatEditText4.perform(pressImeActionButton());

        ViewInteraction materialButton = onView(
                allOf(withId(R.id.finish_new_user_btn), withText("Done"),
                        isDisplayed()));
        materialButton.perform(click());
        Thread.sleep(1000);
        ViewInteraction textView = onView(
                allOf(withId(R.id.user_biography_text),
                        isDisplayed()));
        textView.check(matches(withText("hello")));


        ViewInteraction textView2 = onView(
                allOf(withId(R.id.user_name_text),
                        withParent(withParent(IsInstanceOf.instanceOf(android.widget.ScrollView.class))),
                        isDisplayed()));
        textView2.check(matches(withText(containsString("B"))));
    }

}

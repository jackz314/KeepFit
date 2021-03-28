package com.jackz314.keepfit.views;

import androidx.test.espresso.DataInteraction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import com.jackz314.keepfit.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class EditProfileTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void editProfileEmptyInputTest() throws InterruptedException {
        Thread.sleep(6000);
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

        ViewInteraction materialButton = onView(
                allOf(withId(R.id.finish_new_user_btn), withText("Done"),
                        isDisplayed()));
        materialButton.perform(click());
        Thread.sleep(5000);
        ViewInteraction actionMenuItemView2 = onView(
                allOf(withId(R.id.edit_profile_btn), withContentDescription("Edit Profile"),
                        isDisplayed()));
        actionMenuItemView2.perform(click());

        ViewInteraction materialButton2 = onView(
                allOf(withId(R.id.finish_new_user_btn), withText("Done"),
                        isDisplayed()));
        materialButton2.perform(click());
    }

    @Test
    public void editProfileNewBioTest() throws InterruptedException {
        Thread.sleep(6000);
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
        Thread.sleep(5000);
        ViewInteraction textView = onView(
                allOf(withId(R.id.user_biography_text),
                        isDisplayed()));
        textView.check(matches(withText("hello")));


        ViewInteraction textView2 = onView(
                allOf(withId(R.id.user_name_text),
                        withParent(withParent(IsInstanceOf.<View>instanceOf(android.widget.ScrollView.class))),
                        isDisplayed()));
        textView2.check(matches(withText(containsString("B"))));
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

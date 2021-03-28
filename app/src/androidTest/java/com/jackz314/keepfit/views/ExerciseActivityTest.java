package com.jackz314.keepfit.views;


import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import com.jackz314.keepfit.GlobalConstants;
import com.jackz314.keepfit.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.jackz314.keepfit.helper.RecyclerViewItemCountAssertion.withItemCount;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalToIgnoringCase;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ExerciseActivityTest {

    @Rule
    public ActivityTestRule<ExerciseActivity> exerciseActivityTestRule = new ActivityTestRule<>(ExerciseActivity.class, false, false);

    @Test
    public void exercisePromptCategory() {
        Intent intent = new Intent();
        intent.putExtra(GlobalConstants.EXERCISE_TYPE, "test run");
        intent.putExtra(GlobalConstants.EXERCISE_INTENSITY, 1);
        exerciseActivityTestRule.launchActivity(intent);
        onView(withId(R.id.exercise_title)).check(matches(withText("Test Run")));
    }

    @Test
    public void exercisePromptIntensity() {
        String category = "LnHaZ8l5Yj";
        Intent intent = new Intent();
        intent.putExtra(GlobalConstants.EXERCISE_TYPE, category);
        intent.putExtra(GlobalConstants.EXERCISE_INTENSITY, 3);
        exerciseActivityTestRule.launchActivity(intent);
        onView(withId(R.id.exercise_intensity)).check(matches(withText("Vigorous Intensity")));

        intent.putExtra(GlobalConstants.EXERCISE_INTENSITY, 1);
        exerciseActivityTestRule.launchActivity(intent);
        onView(withId(R.id.exercise_intensity)).check(matches(withText("Light Intensity")));

        intent.putExtra(GlobalConstants.EXERCISE_INTENSITY, 2);
        exerciseActivityTestRule.launchActivity(intent);
        onView(withId(R.id.exercise_intensity)).check(matches(withText("Moderate Intensity")));
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

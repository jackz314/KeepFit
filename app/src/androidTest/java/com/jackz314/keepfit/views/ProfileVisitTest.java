package com.jackz314.keepfit.views;


import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jackz314.keepfit.R;
import com.jackz314.keepfit.TestIdlingResource;
import com.jackz314.keepfit.helper.Helper;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withChild;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.jackz314.keepfit.helper.RecyclerViewItemCountAssertion.withItemCount;
import static com.jackz314.keepfit.helper.RecyclerViewMatcher.withRecyclerView;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ProfileVisitTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    private IdlingResource idlingResource;
    private String testemail;
    private String testpassword;
    private String oldUid;

    @Before
    public void before() throws ExecutionException, InterruptedException {
        final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
//        FirebaseApp.initializeApp(context);
        idlingResource = (IdlingResource) TestIdlingResource.countingIdlingResource;
        IdlingRegistry.getInstance().register(idlingResource);
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        FirebaseApp.initializeApp(appContext);
        testemail = "searchtest@gmail.com";
        testpassword = "search";
        Tasks.await(Helper.createTempAccount(testemail, testpassword));
        oldUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @After
    public void after() throws ExecutionException, InterruptedException {
        Tasks.await(Helper.createTempAccount(testemail, testpassword));

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentSnapshot ds = Tasks.await(db.collection("users").document(oldUid).get());
//        assertFalse(ds.exists());
    }

    @Test
    public void visitFromFeed() throws InterruptedException, ExecutionException {

//        Tasks.await(AuthUI.getInstance().signOut(appContext));
//        Tasks.await(AuthUI.getInstance().silentSignIn(appContext, Collections.singletonList(new AuthUI.IdpConfig.EmailBuilder().build())));

        mActivityTestRule.launchActivity(new Intent());

        ViewInteraction bottomNavigationItemView = onView(
                allOf(withId(R.id.navigation_feed), withContentDescription("Feed"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.nav_view),
                                        0),
                                0),
                        isDisplayed()));
        bottomNavigationItemView.perform(click());

        Thread.sleep(1000);

        onView(withId(R.id.feed_recycler)).check(withItemCount(greaterThanOrEqualTo(1)));
        ViewInteraction videoResult = onView(withRecyclerView(R.id.feed_recycler).atPosition(0));

        // Check for profile button from feed
        videoResult.check(matches(hasDescendant(withId(R.id.feed_profile_pic))));


        ViewInteraction profPic = onView(
                allOf(withId(R.id.feed_profile_pic), withContentDescription("Profile Picture"),
                        childAtPosition(
                                allOf(withId(R.id.constraintLayout),
                                        childAtPosition(
                                                withId(R.id.feed_recycler),
                                                0)),
                                2),
                        isDisplayed()));
        profPic.perform(click());

        Thread.sleep(1000);

        ViewInteraction checkProfile = onView(
                allOf(
                    withChild(withId(R.id.user_profile_picture)),
                    withChild(withId(R.id.followButton))
                )
        );
        checkProfile.check(matches(isDisplayed()));


    }


    @Test
    public void visitFromSearch() throws InterruptedException {
        ViewInteraction bottomNavigationItemView = onView(
                allOf(withId(R.id.navigation_feed), withContentDescription("Feed"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.nav_view),
                                        0),
                                0),
                        isDisplayed()));
        bottomNavigationItemView.perform(click());

        ViewInteraction actionMenuItemView = onView(
                allOf(withId(R.id.app_bar_search), withContentDescription("Search"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.action_bar),
                                        1),
                                0),
                        isDisplayed()));
        actionMenuItemView.perform(click());

        ViewInteraction searchAutoComplete = onView(
                allOf(withClassName(is("android.widget.SearchView$SearchAutoComplete")),
                        childAtPosition(
                                allOf(withClassName(is("android.widget.LinearLayout")),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                0),
                        isDisplayed()));
        searchAutoComplete.perform(replaceText("hernansj"), closeSoftKeyboard());

        ViewInteraction searchAutoComplete2 = onView(
                allOf(withClassName(is("android.widget.SearchView$SearchAutoComplete")), withText("hernansj"),
                        childAtPosition(
                                allOf(withClassName(is("android.widget.LinearLayout")),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                1)),
                                0),
                        isDisplayed()));
        searchAutoComplete2.perform(pressImeActionButton());
        Thread.sleep(2000);

        onView(withId(R.id.search_recycler)).check(withItemCount(greaterThanOrEqualTo(1)));
        ViewInteraction profileResult = onView(withRecyclerView(R.id.search_recycler).atPosition(0));

        profileResult.perform(click());

        ViewInteraction checkProfile = onView(
                allOf(
                        withChild(withId(R.id.user_profile_picture)),
                        withChild(withId(R.id.followButton))
                )
        );
        checkProfile.check(matches(isDisplayed()));




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

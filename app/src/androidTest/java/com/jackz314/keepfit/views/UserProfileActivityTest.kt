package com.jackz314.keepfit.views

import android.app.Instrumentation
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.jackz314.keepfit.GlobalConstants
import com.jackz314.keepfit.R
import com.jackz314.keepfit.models.User
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.containsString
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@LargeTest
@RunWith(AndroidJUnit4::class)
class UserProfileActivityTest {
    private val dummyUser = User().apply {
        birthday = Date(2000,1,1)
        sex = true
        height = 180
        weight = 75
        name = "Test User"
        biography = "Hello"
    }

    @get:Rule
    var profileActivityTestRule = ActivityTestRule(UserProfileActivity::class.java, true, false)

    @Test
    fun profileName() {
        val instrumentation = InstrumentationRegistry.getInstrumentation();

        val intent = Intent()
        intent.putExtra(GlobalConstants.USER_PROFILE, dummyUser)
        profileActivityTestRule.launchActivity(intent)

        instrumentation.waitForIdleSync();

        onView(ViewMatchers.withId(R.id.user_name_text)).check(ViewAssertions.matches(ViewMatchers.withText(dummyUser.name)))

        profileActivityTestRule.finishActivity()


        intent.putExtra(GlobalConstants.USER_PROFILE, dummyUser.apply { name = "New User Name" })
        profileActivityTestRule.launchActivity(intent)

        instrumentation.waitForIdleSync();

        onView(ViewMatchers.withId(R.id.user_name_text)).check(ViewAssertions.matches(ViewMatchers.withText(dummyUser.name)))
        profileActivityTestRule.finishActivity()
    }

    @Test
    fun profileBio() {
        val instrumentation = InstrumentationRegistry.getInstrumentation();

        val intent = Intent()
        intent.putExtra(GlobalConstants.USER_PROFILE, dummyUser)
        profileActivityTestRule.launchActivity(intent)

        instrumentation.waitForIdleSync();

        onView(ViewMatchers.withId(R.id.biography)).check(ViewAssertions.matches(ViewMatchers.withText(containsString(dummyUser.biography))))

        profileActivityTestRule.finishActivity()

        intent.putExtra(GlobalConstants.USER_PROFILE, dummyUser.apply { biography = "" })
        profileActivityTestRule.launchActivity(intent)

        instrumentation.waitForIdleSync();

        onView(ViewMatchers.withId(R.id.biography)).check(ViewAssertions.matches(ViewMatchers.withText(containsString("Hello"))))
    }

}
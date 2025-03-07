package com.jackz314.keepfit.views.other

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.FragmentNavigator

// from https://stackoverflow.com/a/51684125/8170714 and https://github.com/STAR-ZERO/navigation-keep-fragment-sample/
// Fixes destorying fragments every time a switch occurs. Google sucks :(

private const val TAG = "KeepStateNavigator"

@Navigator.Name("keep_state_fragment")  // Use as custom tag at navigation.xml
class KeepStateNavigator(
        private val context: Context,
        private val manager: FragmentManager,
        private val containerId: Int
) : FragmentNavigator(context, manager, containerId) {

    override fun navigate(
            destination: Destination,
            args: Bundle?,
            navOptions: NavOptions?,
            navigatorExtras: Navigator.Extras?
    ): NavDestination? {
//        Log.d(TAG, "navigate: navigating to ${destination}, ${navOptions}, ${navigatorExtras}")
        val tag = destination.id.toString()
        val transaction = manager.beginTransaction()

        var initialNavigate = false
        val currentFragment = manager.primaryNavigationFragment
        if (currentFragment != null) {
            transaction.detach(currentFragment)
        } else {
            initialNavigate = true
        }

        var fragment = manager.findFragmentByTag(tag)
        if (fragment == null) {
            val className = destination.className
            fragment = manager.fragmentFactory.instantiate(context.classLoader, className)
            transaction.add(containerId, fragment, tag)
        } else {
            transaction.attach(fragment)
        }

        transaction.setPrimaryNavigationFragment(fragment)
        transaction.setReorderingAllowed(true)
        transaction.commitNow()

        return if (initialNavigate) {
            destination
        } else {
            null
        }
    }
}
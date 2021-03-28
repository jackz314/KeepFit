package com.jackz314.keepfit

object TestIdlingResource {

    private const val RESOURCE = "GLOBAL"

    @JvmField
    val countingIdlingResource
        = SimpleCountingIdlingResource(RESOURCE)

    @JvmStatic
    fun increment() {
        countingIdlingResource.increment()
    }

    @JvmStatic
    fun decrement() {
        if (!countingIdlingResource.isIdleNow) {
            countingIdlingResource.decrement()
        }
    }
}
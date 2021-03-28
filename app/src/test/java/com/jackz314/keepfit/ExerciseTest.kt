package com.jackz314.keepfit

import com.jackz314.keepfit.controllers.ExerciseController
import com.jackz314.keepfit.models.User
import junit.framework.Assert.assertEquals
import org.junit.Test
import java.util.*

class ExerciseTest {
    private val dummyUser = User().apply {
        birthday = Date(2000,1,1)
        sex = true
        height = 180
        weight = 75
    }

    @Test
    fun bmr() {
        assertEquals(11306.31, ExerciseController(dummyUser).userBMR, 0.1)
        assertEquals(11140.31, ExerciseController(dummyUser.copy().apply { sex = false }).userBMR, 0.1)
        assertEquals(11243.81, ExerciseController(dummyUser.copy().apply { height = 170 }).userBMR, 0.1)
        assertEquals(11456.31, ExerciseController(dummyUser.copy().apply { weight = 90 }).userBMR, 0.1)
        assertEquals(11055.45, ExerciseController(dummyUser.copy().apply { birthday = Date(1950,1,1) }).userBMR, 0.1)
    }

    @Test
    fun calMultiplier() {
        assertEquals(4710.96, ExerciseController(dummyUser).calMultiplier, 0.1)
        assertEquals(4641.79, ExerciseController(dummyUser.copy().apply { sex = false }).calMultiplier, 0.1)
        assertEquals(4684.92, ExerciseController(dummyUser.copy().apply { height = 170 }).calMultiplier, 0.1)
        assertEquals(4773.46, ExerciseController(dummyUser.copy().apply { weight = 90 }).calMultiplier, 0.1)
        assertEquals(4606.44, ExerciseController(dummyUser.copy().apply { birthday = Date(1950,1,1) }).calMultiplier, 0.1)
        assertEquals(2355.48, ExerciseController(dummyUser, 5F).calMultiplier, 0.1)
        assertEquals(47109.61, ExerciseController(dummyUser, 100F).calMultiplier, 0.1)
    }

    @Test
    fun calorieBurned() {
        assertEquals(1.309, ExerciseController(dummyUser).getCalBurned(1000), 0.01)
        assertEquals(2.652, ExerciseController(dummyUser.copy().apply { weight = 90 }).getCalBurned(2000), 0.01)
        assertEquals(13.086, ExerciseController(dummyUser).getCalBurned(10000), 0.01)
        assertEquals(1.963, ExerciseController(dummyUser, 5F).getCalBurned(3000), 0.01)
        assertEquals(52.344, ExerciseController(dummyUser, 100F).getCalBurned(4000), 0.01)
    }
}
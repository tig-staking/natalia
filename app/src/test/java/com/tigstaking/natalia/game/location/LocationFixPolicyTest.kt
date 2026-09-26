package com.tigstaking.natalia.game.location

import com.tigstaking.natalia.game.Coordinates
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class LocationFixPolicyTest {
    @Test
    fun acceptsFreshValidFix() {
        val fix = DeviceLocation(Coordinates(49.9, 18.7), 8f, capturedAtMillis = 100_000)
        assertEquals(fix, LocationFixPolicy.validate(fix, nowMillis = 115_000))
    }

    @Test
    fun rejectsStaleFutureInvalidAndUnmeasuredFixes() {
        val now = 100_000L
        fun fix(latitude: Double = 49.9, accuracy: Float = 8f, time: Long = now) =
            DeviceLocation(Coordinates(latitude, 18.7), accuracy, time)

        assertThrows(CurrentLocationUnavailableException::class.java) {
            LocationFixPolicy.validate(fix(time = now - 30_001), now)
        }
        assertThrows(CurrentLocationUnavailableException::class.java) {
            LocationFixPolicy.validate(fix(time = now + 15_001), now)
        }
        assertThrows(CurrentLocationUnavailableException::class.java) {
            LocationFixPolicy.validate(fix(latitude = 91.0), now)
        }
        assertThrows(CurrentLocationUnavailableException::class.java) {
            LocationFixPolicy.validate(fix(accuracy = Float.NaN), now)
        }
    }
}

package com.tigstaking.natalia.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class GameRulesTest {
    @Test
    fun rewardEventIsAppliedOnlyOnce() {
        val event = RewardEvent(id = "place:sagrada:discovery", source = "DISCOVERY", xp = 10, stars = 10)

        val progress = GameProgress().applyOnce(event).applyOnce(event)

        assertEquals(10, progress.xp)
        assertEquals(10, progress.stars)
        assertEquals(1, progress.ledger.size)
    }

    @Test
    fun redemptionSpendsStarsButNeverXp() {
        val progress = GameProgress(xp = 80, stars = 120)
            .redeemOnce("ice-cream", 100)
            .redeemOnce("ice-cream", 100)

        assertEquals(80, progress.xp)
        assertEquals(20, progress.stars)
        assertEquals(-100, progress.ledger.single().stars)
    }

    @Test
    fun redemptionFailsWhenStarsAreInsufficient() {
        assertThrows(IllegalArgumentException::class.java) {
            GameProgress(xp = 80, stars = 99).redeemOnce("ice-cream", 100)
        }
    }

    @Test
    fun levelsUseConfiguredThresholds() {
        assertEquals(PlayerLevel.EXPLORER, PlayerLevel.forXp(49))
        assertEquals(PlayerLevel.TRACKER, PlayerLevel.forXp(50))
        assertEquals(PlayerLevel.DETECTIVE, PlayerLevel.forXp(100))
        assertEquals(PlayerLevel.MASTER, PlayerLevel.forXp(200))
    }

    @Test
    fun proximityUsesRadiusInMeters() {
        val sagrada = Coordinates(41.4036, 2.1744)
        val nearby = Coordinates(41.4040, 2.1744)
        val farAway = Coordinates(41.4100, 2.1744)

        assertTrue(Proximity.isInside(nearby, sagrada, radiusMeters = 100))
        assertFalse(Proximity.isInside(farAway, sagrada, radiusMeters = 100))
    }
}

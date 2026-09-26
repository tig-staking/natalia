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
    fun pendingRewardReservesStarsButDoesNotSpendUntilApproval() {
        val requested = GameProgress(xp = 80, stars = 120).requestRedemption("ice-cream", 100)

        assertEquals(80, requested.xp)
        assertEquals(120, requested.stars)
        assertTrue("ice-cream" in requested.pendingRewardRequests)
        assertTrue(requested.ledger.isEmpty())
        assertThrows(IllegalArgumentException::class.java) {
            requested.redeemOnce("other-reward", 30)
        }

        val approved = requested.approveRedemption("ice-cream").approveRedemption("ice-cream")
        assertEquals(80, approved.xp)
        assertEquals(20, approved.stars)
        assertTrue("ice-cream" in approved.redeemedRewardIds)
        assertTrue(approved.pendingRewardRequests.isEmpty())
        assertEquals(-100, approved.ledger.single().stars)
    }

    @Test
    fun cancellingRewardRequestReleasesReservedStars() {
        val requested = GameProgress(stars = 100)
            .requestRedemption("ice-cream", 100)
            .cancelRedemption("ice-cream")
        val redeemed = requested.redeemOnce("other-reward", 100)

        assertEquals(0, redeemed.stars)
        assertTrue(redeemed.ledger.single().eventId == "reward:other-reward")
    }

    @Test
    fun parentStarAdjustmentPreservesReservationsAndIsIdempotent() {
        val requested = GameProgress(xp = 70, stars = 110).requestRedemption("ice-cream", 100)
        val adjusted = requested.adjustStarsOnce("parent:1", -10).adjustStarsOnce("parent:1", -10)

        assertEquals(70, adjusted.xp)
        assertEquals(100, adjusted.stars)
        assertEquals(1, adjusted.ledger.size)
        assertEquals("PARENT", adjusted.ledger.single().source)
        assertThrows(IllegalArgumentException::class.java) {
            adjusted.adjustStarsOnce("parent:2", -10)
        }
        assertEquals(0, adjusted.approveRedemption("ice-cream").stars)
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

    @Test
    fun accurateFixWellInsideRadiusConfirmsCheckIn() {
        val sagrada = Coordinates(41.4036, 2.1744)

        val result = Proximity.classify(sagrada, sagrada, radiusMeters = 120, accuracyMeters = 10.0)

        assertTrue(result is ProximityResult.Inside)
    }

    @Test
    fun uncertainFixNearGeofenceEdgeDoesNotConfirmCheckIn() {
        val sagrada = Coordinates(41.4036, 2.1744)
        val nearEdge = Coordinates(41.4046, 2.1744)

        val result = Proximity.classify(nearEdge, sagrada, radiusMeters = 120, accuracyMeters = 25.0)

        assertTrue(result is ProximityResult.Uncertain)
    }

    @Test
    fun inaccurateFixAtTheCenterDoesNotConfirmCheckIn() {
        val sagrada = Coordinates(41.4036, 2.1744)

        val result = Proximity.classify(sagrada, sagrada, radiusMeters = 120, accuracyMeters = 150.0)

        assertTrue(result is ProximityResult.Uncertain)
    }

    @Test
    fun clearlyDistantFixIsOutsideEvenWithNormalAccuracy() {
        val sagrada = Coordinates(41.4036, 2.1744)
        val farAway = Coordinates(41.4100, 2.1744)

        val result = Proximity.classify(farAway, sagrada, radiusMeters = 120, accuracyMeters = 10.0)

        assertTrue(result is ProximityResult.Outside)
    }

    @Test
    fun proximityRejectsInvalidAccuracy() {
        val sagrada = Coordinates(41.4036, 2.1744)

        assertThrows(IllegalArgumentException::class.java) {
            Proximity.classify(sagrada, sagrada, radiusMeters = 120, accuracyMeters = Double.NaN)
        }
        assertThrows(IllegalArgumentException::class.java) {
            Proximity.classify(sagrada, sagrada, radiusMeters = 120, accuracyMeters = -1.0)
        }
    }

    @Test
    fun quizAcceptsOnlyTheConfiguredCorrectAnswer() {
        val quiz = Quiz("q", "Question?", listOf("A", "B", "C"), correctAnswerIndex = 1)

        assertTrue(quiz.isCorrect(1))
        assertFalse(quiz.isCorrect(0))
        assertFalse(quiz.isCorrect(-1))
    }

    @Test
    fun placeStageRewardsAddUpToConfiguredTotals() {
        val rewards = PlaceRewards(
            discovery = RewardAmount(10, 10),
            quest = RewardAmount(20, 20),
            spanishWord = RewardAmount(10, 10),
            quiz = RewardAmount(10, 10),
        )

        assertEquals(50, rewards.totalXp)
        assertEquals(50, rewards.totalStars)
    }
}

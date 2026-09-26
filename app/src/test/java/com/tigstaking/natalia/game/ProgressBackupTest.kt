package com.tigstaking.natalia.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test

class ProgressBackupTest {
    @Test
    fun roundTripsCompleteProgressAndOnboardingState() {
        val progress = GameProgress()
            .applyOnce(RewardEvent("discovery:sagrada", "DISCOVERY", xp = 10, stars = 120))
            .requestRedemption("ice-cream", 100)
            .approveRedemption("ice-cream")
            .requestRedemption("sticker", 10)
            .copy(
                discoveredPlaceIds = setOf("sagrada"),
                completedQuestIds = setOf("quest:sagrada"),
                completedQuizIds = setOf("quiz:sagrada"),
                completedWordIds = setOf("sagrada"),
                earnedBadgeIds = setOf("badge:sagrada"),
            )
        val expected = ProgressBackup(onboardingCompleted = true, progress = progress)

        assertEquals(expected, ProgressBackupCodec.decode(ProgressBackupCodec.encode(expected)))
    }

    @Test
    fun rejectsWrongVersionAndLedgerMismatch() {
        val valid = ProgressBackupCodec.encode(ProgressBackup(false, GameProgress()))
        assertThrows(IllegalArgumentException::class.java) {
            ProgressBackupCodec.decode(valid.replace("\"formatVersion\": 1", "\"formatVersion\": 2"))
        }
        assertThrows(IllegalArgumentException::class.java) {
            ProgressBackupCodec.decode(valid.replace("\"stars\": 0", "\"stars\": 10"))
        }
    }

    @Test
    fun rejectsMalformedRedemptionLedgerEntry() {
        val progress = GameProgress()
            .applyOnce(RewardEvent("award", "TEST", stars = 10))
            .redeemOnce("ice", 10)
        val json = ProgressBackupCodec.encode(ProgressBackup(false, progress))
            .replace("reward:ice", "bad-redemption")
        assertThrows(IllegalArgumentException::class.java) { ProgressBackupCodec.decode(json) }
    }

    @Test
    fun exportDoesNotIncludeDeviceSpecificData() {
        val json = ProgressBackupCodec.encode(ProgressBackup(false, GameProgress()))
        assertFalse(json.contains("parentPin"))
        assertFalse(json.contains("debugTestPoi"))
        assertFalse(json.contains("latitude"))
        assertFalse(json.contains("longitude"))
    }
}

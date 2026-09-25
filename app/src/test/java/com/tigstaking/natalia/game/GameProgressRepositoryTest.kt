package com.tigstaking.natalia.game

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import java.nio.file.Files
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameProgressRepositoryTest {
    @Test
    fun rewardsAndLedgerSurviveRepositoryRecreationAndResetClearsThem() = runBlocking {
        val directory = Files.createTempDirectory("natalia-progress-test").toFile()
        val file = File(directory, "game_progress.preferences_pb")
        var firstScope: CoroutineScope? = null
        var secondScope: CoroutineScope? = null

        try {
            val firstRepository = newRepository(file).also { firstScope = it.second }.first
            firstRepository.discoverPlace("sagrada", xp = 10, stars = 10)
            firstRepository.completeQuest("sagrada-quest", xp = 20, stars = 20)
            assertFalse(firstRepository.redeemOnce("ice-cream", cost = 100))
            assertTrue(firstRepository.redeemOnce("souvenir", cost = 15))

            firstScope?.cancel()
            firstScope = null

            val secondRepository = newRepository(file).also { secondScope = it.second }.first
            val restored = secondRepository.progress.first()
            assertEquals(30, restored.xp)
            assertEquals(15, restored.stars)
            assertTrue("sagrada" in restored.discoveredPlaceIds)
            assertTrue("sagrada-quest" in restored.completedQuestIds)
            assertEquals(3, restored.ledger.size)
            assertEquals(-15, restored.ledger.last().stars)

            secondRepository.reset()
            val reset = secondRepository.progress.first()
            assertEquals(0, reset.xp)
            assertEquals(0, reset.stars)
            assertTrue(reset.ledger.isEmpty())
            assertTrue(reset.discoveredPlaceIds.isEmpty())
            assertTrue(reset.pendingRewardRequests.isEmpty())
        } finally {
            firstScope?.cancel()
            secondScope?.cancel()
            directory.deleteRecursively()
        }
    }

    @Test
    fun rewardRequestPersistsAndOnlyApprovalSpendsStars() = runBlocking {
        val directory = Files.createTempDirectory("natalia-reward-request-test").toFile()
        val file = File(directory, "game_progress.preferences_pb")
        val (repository, scope) = newRepository(file)

        try {
            repository.awardOnce(RewardEvent("test:stars", "TEST", stars = 120))
            assertTrue(repository.requestRedemptionOnce("ice-cream", cost = 100))
            assertTrue(repository.requestRedemptionOnce("ice-cream", cost = 100))
            assertFalse(repository.requestRedemptionOnce("souvenir", cost = 30))
            assertFalse(repository.redeemOnce("souvenir", cost = 30))

            val requested = repository.progress.first()
            assertEquals(120, requested.stars)
            assertEquals(100, requested.pendingRewardRequests["ice-cream"]?.cost)
            assertTrue(requested.ledger.none { it.source == "REWARD" })
            assertFalse(repository.approveRedemption("unknown"))
            assertTrue(repository.approveRedemption("ice-cream"))
            assertTrue(repository.approveRedemption("ice-cream"))

            val approved = repository.progress.first()
            assertEquals(20, approved.stars)
            assertTrue(approved.pendingRewardRequests.isEmpty())
            assertTrue("ice-cream" in approved.redeemedRewardIds)
            assertEquals(0, approved.ledger.single { it.source == "REWARD" }.xp)
            assertEquals(-100, approved.ledger.single { it.source == "REWARD" }.stars)
        } finally {
            scope.cancel()
            directory.deleteRecursively()
        }
    }

    private fun newRepository(file: File): Pair<GameProgressRepository, CoroutineScope> {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { file },
        )
        return GameProgressRepository(dataStore) to scope
    }
}

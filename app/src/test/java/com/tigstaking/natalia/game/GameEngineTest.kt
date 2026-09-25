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

class GameEngineTest {
    @Test
    fun sagradaFlowRequiresOrderedStagesAndPaysRewardsOnlyOnce() = runBlocking {
        val directory = Files.createTempDirectory("natalia-engine-test").toFile()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { File(directory, "progress.preferences_pb") },
        )
        val repository = GameProgressRepository(dataStore)
        val engine = GameEngine(repository)
        val place = samplePlace()

        try {
            assertTrue("Quest must stay locked until discovery", runCatching {
                engine.completeQuest(place)
            }.isFailure)

            engine.simulateDiscovery(place)
            assertTrue("Word must stay locked until quest completion", runCatching {
                engine.learnSpanishWord(place)
            }.isFailure)

            engine.completeQuest(place)
            assertTrue("Quiz must stay locked until the word is learned", runCatching {
                engine.answerQuiz(place, place.quiz.correctAnswerIndex)
            }.isFailure)

            engine.learnSpanishWord(place)
            val xpBeforeWrongAnswer = repository.progress.first().xp
            val wrongAnswer = engine.answerQuiz(place, 1)
            assertTrue(wrongAnswer is QuizAnswerResult.TryAgain)
            assertEquals(xpBeforeWrongAnswer, repository.progress.first().xp)

            val correctAnswer = engine.answerQuiz(place, place.quiz.correctAnswerIndex)
            assertTrue(correctAnswer is QuizAnswerResult.Correct)
            engine.earnBadge(place)
            engine.earnBadge(place)

            val completed = repository.progress.first()
            assertEquals(50, completed.xp)
            assertEquals(50, completed.stars)
            assertEquals(4, completed.ledger.size)
            assertTrue(place.id in completed.discoveredPlaceIds)
            assertTrue(place.quest.id in completed.completedQuestIds)
            assertTrue(place.id in completed.completedWordIds)
            assertTrue(place.quiz.id in completed.completedQuizIds)
            assertTrue("badge:${place.id}" in completed.earnedBadgeIds)
            assertFalse(completed.ledger.any { it.xp < 0 })
        } finally {
            scope.cancel()
            directory.deleteRecursively()
        }
    }

    private fun samplePlace() = Place(
        id = "sagrada",
        name = "Sagrada Família",
        coordinates = Coordinates(41.4036, 2.1744),
        geofenceRadiusMeters = 120,
        intro = "Welcome",
        fact = "A fact",
        spanishWord = SpanishWord("torre", "wieża"),
        quest = Quest("sagrada-quest", QuestType.OBSERVATION, "Look around"),
        quiz = Quiz("sagrada-quiz", "What does torre mean?", listOf("tower", "street", "sea"), 0),
        xp = 50,
        stars = 50,
        rewards = PlaceRewards(
            discovery = RewardAmount(10, 10),
            quest = RewardAmount(20, 20),
            spanishWord = RewardAmount(10, 10),
            quiz = RewardAmount(10, 10),
        ),
        badge = "ARCHITEKTONICZNA DETEKTYWKA",
        avatarPose = "surprised",
    )
}

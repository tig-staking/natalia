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
    fun typedQuestRulesRequireCorrectAnswerOrParentConfirmation() = runBlocking {
        val directory = Files.createTempDirectory("natalia-typed-quest-test").toFile()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { File(directory, "progress.preferences_pb") },
        )
        val repository = GameProgressRepository(dataStore)
        val engine = GameEngine(repository)
        val base = samplePlace()
        val choicePlace = base.copy(
            quest = Quest("choice-quest", QuestType.MULTIPLE_CHOICE, "Choose", listOf("A", "B"), 1),
        )
        val parentPlace = base.copy(quest = Quest("parent-quest", QuestType.PARENT_CHECK, "Ask your parent"))
        try {
            engine.simulateDiscovery(base)
            assertTrue(runCatching { engine.completeQuest(choicePlace, answerIndex = 0) }.isFailure)
            engine.completeQuest(choicePlace, answerIndex = 1)
            assertTrue(runCatching { engine.completeQuest(parentPlace) }.isFailure)
            engine.completeQuest(parentPlace, parentConfirmed = true)

            val progress = repository.progress.first()
            assertTrue("choice-quest" in progress.completedQuestIds)
            assertTrue("parent-quest" in progress.completedQuestIds)
            assertEquals(50, progress.xp)
            assertEquals(50, progress.stars)
        } finally {
            scope.cancel()
            directory.deleteRecursively()
        }
    }

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
            val multipleChoiceQuest = place.copy(
                quest = Quest("choice-quest", QuestType.MULTIPLE_CHOICE, "Choose", listOf("A", "B"), 1),
            )
            assertTrue("Wrong quest choice must not complete the quest", runCatching {
                engine.completeQuest(multipleChoiceQuest, answerIndex = 0)
            }.isFailure)
            assertTrue("Parent-check quests stay locked until confirmed", runCatching {
                engine.completeQuest(place.copy(quest = Quest("parent-quest", QuestType.PARENT_CHECK, "Ask a parent")))
            }.isFailure)
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
            assertTrue("Quiz and badge must be saved together", "badge:${place.id}" in
                (correctAnswer as QuizAnswerResult.Correct).progress.earnedBadgeIds)
            engine.answerQuiz(place, place.quiz.correctAnswerIndex)

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

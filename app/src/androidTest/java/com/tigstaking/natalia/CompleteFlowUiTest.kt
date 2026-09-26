package com.tigstaking.natalia

import android.content.Context
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tigstaking.natalia.game.GameProgressRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CompleteFlowUiTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun completesPlaceAndRestoresPassportAfterRestart() = runBlocking {
        val repository = GameProgressRepository(ApplicationProvider.getApplicationContext<Context>())
        repository.reset()
        composeRule.activityRule.scenario.recreate()
        tap("ROZPOCZNIJ PRZYGODĘ")
        tap("Developer mode")
        tap("ODKRYJ MIEJSCE BEZ SPRAWDZANIA GPS (DEBUG)")
        tap("ZROBIONE!")
        tap("ZAPAMIĘTANE")
        tap("ulica")
        tap("wieża")

        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithText("PASZPORT BARCELONY").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("PASZPORT BARCELONY").assertExists()
        val complete = withTimeout(10_000) {
            repository.progress.first { "badge:sagrada-familia" in it.earnedBadgeIds }
        }
        assertEquals(50, complete.xp)
        assertEquals(50, complete.stars)
        assertEquals(4, complete.ledger.size)

        composeRule.activityRule.scenario.recreate()
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithText("PASZPORT").fetchSemanticsNodes().isNotEmpty()
        }
        tap("PASZPORT")
        composeRule.onNodeWithText("✓ UKOŃCZONE · ODZNAKA: ARCHITEKTONICZNA DETEKTYWKA").assertExists()
        assertTrue("badge:sagrada-familia" in repository.progress.first().earnedBadgeIds)
    }

    private fun tap(label: String) {
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithText(label).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(label).performScrollTo().performClick()
        composeRule.waitForIdle()
    }
}

package com.tigstaking.natalia

import android.content.Context
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tigstaking.natalia.game.GameProgressRepository
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeSmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun opensSagradaPlaceAndShowsLocationCheckIn() {
        runBlocking {
            GameProgressRepository(ApplicationProvider.getApplicationContext<Context>()).reset()
        }
        composeRule.waitForIdle()
        if (composeRule.onAllNodesWithText("ROZPOCZNIJ PRZYGODĘ").fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithText("ROZPOCZNIJ PRZYGODĘ").performClick()
        }
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithText("POKAŻ MIEJSCE").fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithText("KONTYNUUJ PRZYGODĘ").fetchSemanticsNodes().isNotEmpty()
        }
        if (composeRule.onAllNodesWithText("POKAŻ MIEJSCE").fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithText("POKAŻ MIEJSCE").performClick()
            composeRule.onNodeWithText("JESTEM NA MIEJSCU — SPRAWDŹ GPS").assertExists()
        } else {
            composeRule.onNodeWithText("KONTYNUUJ PRZYGODĘ").performClick()
            composeRule.onNodeWithText("Barcelona · Sagrada Família").assertExists()
        }
    }
}

package com.tigstaking.natalia

import android.content.Context
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tigstaking.natalia.game.GameProgressRepository
import com.tigstaking.natalia.game.RewardEvent
import com.tigstaking.natalia.security.ParentPinStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RewardApprovalUiTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun parentApprovesIceCreamRequestAndSpendsStarsOnce() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = GameProgressRepository(context)
        repository.reset()
        repository.completeOnboarding()
        repository.awardOnce(RewardEvent("ui-test:stars", "TEST", stars = 100))
        context.getSharedPreferences("parent_auth", Context.MODE_PRIVATE).edit().clear().commit()

        composeRule.waitForIdle()
        clickIfPresent("ROZPOCZNIJ PRZYGODĘ")
        tap("TRYB RODZICA")
        tapText("Nowy PIN", "4826")
        tapText("Powtórz PIN", "4826")
        tapDialog("USTAW PIN I KONTYNUUJ")
        composeRule.onNodeWithText("Brak oczekujących próśb.").assertExists()
        tapDialog("ZAMKNIJ")

        tap("POPROŚ O LODY · 100 ★")
        tap("TRYB RODZICA")
        tapText("PIN", "4826")
        tapDialog("ODBLOKUJ")
        composeRule.onNodeWithText("Lody za 100 ★").assertExists()
        tapDialog("ZATWIERDŹ")
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithText("Brak oczekujących próśb.").fetchSemanticsNodes().isNotEmpty()
        }

        val completed = withTimeout(10_000) {
            repository.progress.first { it.stars == 0 && it.redeemedRewardIds.isNotEmpty() }
        }
        assertEquals(0, completed.stars)
        assertEquals(-100, completed.ledger.last { it.source == "REWARD" }.stars)
        assertEquals(1, completed.redeemedRewardIds.size)
        assertTrue(completed.pendingRewardRequests.isEmpty())
    }

    private fun tap(label: String) {
        composeRule.onNodeWithText(label).performScrollTo().performClick()
        composeRule.waitForIdle()
    }

    private fun tapText(label: String, value: String) {
        composeRule.onNodeWithText(label).performTextInput(value)
        composeRule.waitForIdle()
    }

    private fun tapDialog(label: String) {
        composeRule.onNodeWithText(label).performClick()
        composeRule.waitForIdle()
    }

    private fun clickIfPresent(label: String) {
        if (composeRule.onAllNodesWithText(label).fetchSemanticsNodes().isNotEmpty()) tap(label)
    }
}

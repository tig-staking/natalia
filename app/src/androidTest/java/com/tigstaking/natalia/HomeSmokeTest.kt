package com.tigstaking.natalia

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeSmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun opensSagradaPlaceAndShowsLocationCheckIn() {
        composeRule.onNodeWithText("NATALIA NA TROPIE").assertExists()
        composeRule.onNodeWithText("POKAŻ MIEJSCE").performClick()
        composeRule.onNodeWithText("Sagrada Família").assertExists()
        composeRule.onNodeWithText("JESTEM NA MIEJSCU — SPRAWDŹ GPS").assertExists()
    }
}

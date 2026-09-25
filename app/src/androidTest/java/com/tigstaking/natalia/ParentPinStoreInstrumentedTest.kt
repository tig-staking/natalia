package com.tigstaking.natalia

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tigstaking.natalia.security.ParentPinStore
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ParentPinStoreInstrumentedTest {
    @Test
    fun storesNoPlaintextPinAndLocksAfterFiveFailedAttempts() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val preferences = context.getSharedPreferences("parent_auth", Context.MODE_PRIVATE)
        preferences.edit().clear().commit()
        val pinStore = ParentPinStore(context)

        assertTrue(pinStore.setPin("4826"))
        assertFalse("PIN must not be stored as plain text", preferences.all.values.any { it == "4826" })
        repeat(5) { index -> assertFalse(pinStore.verify("0000", nowMillis = 1_000L + index)) }
        assertFalse("Correct PIN remains blocked during lockout", pinStore.verify("4826", nowMillis = 2_000L))
        assertTrue("Correct PIN works after the lockout expires", pinStore.verify("4826", nowMillis = 32_000L))
    }
}

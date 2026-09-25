package com.tigstaking.natalia.security

import android.content.Context
import android.util.Base64
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties

/**
 * Stores only a keyed HMAC of the parent PIN. The HMAC key is non-exportable and stays
 * in Android Keystore, so a copied preferences backup cannot reveal or validate the PIN.
 */
class ParentPinStore(context: Context) {
    private val preferences = context.applicationContext
        .getSharedPreferences("parent_auth", Context.MODE_PRIVATE)

    val isConfigured: Boolean
        get() = preferences.contains(PIN_MAC) && keyStore().containsAlias(KEY_ALIAS)

    fun setPin(pin: String): Boolean {
        if (!PIN_PATTERN.matches(pin)) return false
        return runCatching {
            val mac = calculateMac(pin, getOrCreateKey())
            preferences.edit().putString(PIN_MAC, Base64.encodeToString(mac, Base64.NO_WRAP))
                .putInt(FAILED_ATTEMPTS, 0)
                .putLong(LOCKED_UNTIL, 0L)
                .commit()
        }.getOrDefault(false)
    }

    fun verify(pin: String, nowMillis: Long = System.currentTimeMillis()): Boolean {
        if (!PIN_PATTERN.matches(pin) || nowMillis < preferences.getLong(LOCKED_UNTIL, 0L)) return false
        val stored = preferences.getString(PIN_MAC, null) ?: return false
        val valid = runCatching {
            val expected = Base64.decode(stored, Base64.NO_WRAP)
            val actual = calculateMac(pin, getOrCreateKey())
            java.security.MessageDigest.isEqual(expected, actual)
        }.getOrDefault(false)

        if (valid) {
            preferences.edit().putInt(FAILED_ATTEMPTS, 0).putLong(LOCKED_UNTIL, 0L).apply()
            return true
        }

        val failures = preferences.getInt(FAILED_ATTEMPTS, 0) + 1
        preferences.edit()
            .putInt(FAILED_ATTEMPTS, failures)
            .putLong(LOCKED_UNTIL, if (failures >= MAX_FAILURES) nowMillis + LOCKOUT_MILLIS else 0L)
            .apply()
        return false
    }

    private fun getOrCreateKey(): SecretKey {
        val store = keyStore()
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_HMAC_SHA256, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY,
            ).setDigests(KeyProperties.DIGEST_SHA256).build(),
        )
        return generator.generateKey()
    }

    private fun keyStore(): KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    private fun calculateMac(pin: String, key: SecretKey): ByteArray =
        Mac.getInstance(HMAC_SHA256).run { init(key); doFinal(pin.toByteArray(Charsets.UTF_8)) }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val HMAC_SHA256 = "HmacSHA256"
        const val KEY_ALIAS = "natalia.parent.pin.hmac.v1"
        const val PIN_MAC = "pin_mac"
        const val FAILED_ATTEMPTS = "failed_attempts"
        const val LOCKED_UNTIL = "locked_until"
        const val MAX_FAILURES = 5
        const val LOCKOUT_MILLIS = 30_000L
        val PIN_PATTERN = Regex("\\d{4}")
    }
}

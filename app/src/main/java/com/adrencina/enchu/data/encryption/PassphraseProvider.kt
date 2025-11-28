package com.adrencina.enchu.data.encryption

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PassphraseProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val encryptedPrefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "database_passphrase_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Retrieves the database passphrase.
     * If it doesn't exist, it generates a new one, saves it securely, and returns it.
     * If it exists, it retrieves and decodes it.
     * @return A ByteArray representing the passphrase.
     */
    fun getPassphrase(): ByteArray {
        val encryptedPassphrase = encryptedPrefs.getString(KEY_PASSPHRASE, null)

        return if (encryptedPassphrase == null) {
            val newPassphrase = generatePassphrase()
            val newEncryptedPassphrase = Base64.encodeToString(newPassphrase, Base64.NO_WRAP)
            encryptedPrefs.edit().putString(KEY_PASSPHRASE, newEncryptedPassphrase).apply()
            newPassphrase
        } else {
            Base64.decode(encryptedPassphrase, Base64.NO_WRAP)
        }
    }

    private fun generatePassphrase(): ByteArray {
        val random = SecureRandom()
        val passphrase = ByteArray(32) // 256-bit key
        random.nextBytes(passphrase)
        return passphrase
    }

    companion object {
        private const val KEY_PASSPHRASE = "db_passphrase"
    }
}

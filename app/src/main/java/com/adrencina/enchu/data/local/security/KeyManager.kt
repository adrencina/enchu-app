package com.adrencina.enchu.data.local.security

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeyManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val encryptedPrefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "database_key_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Obtiene la frase de paso segura para la base de datos.
     * Si no existe, genera una nueva de 32 bytes (256 bits), la guarda cifrada y la retorna.
     */
    fun getSafeDatabasePassphrase(): ByteArray {
        val storedKey = encryptedPrefs.getString(KEY_DB_PASSPHRASE, null)

        return if (storedKey == null) {
            val newKey = generateRandomKey()
            val encodedKey = Base64.encodeToString(newKey, Base64.NO_WRAP)
            encryptedPrefs.edit().putString(KEY_DB_PASSPHRASE, encodedKey).apply()
            newKey
        } else {
            Base64.decode(storedKey, Base64.NO_WRAP)
        }
    }

    private fun generateRandomKey(): ByteArray {
        val random = SecureRandom()
        val key = ByteArray(32)
        random.nextBytes(key)
        return key
    }

    companion object {
        private const val KEY_DB_PASSPHRASE = "secure_db_passphrase"
    }
}

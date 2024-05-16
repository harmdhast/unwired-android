import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.nio.charset.Charset
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator.getInstance
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * This object `KeystoreHelper` provides methods for encrypting and decrypting data using the Android Keystore.
 * It uses AES encryption with GCM block mode and no padding.
 */
object KeystoreHelper {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "myKeyAlias"
    private const val AES_MODE = "AES/GCM/NoPadding"

    /**
     * Encrypts the provided data using the secret key from the Android Keystore.
     *
     * The encryption is done using the AES/GCM/NoPadding cipher. The initialization vector (iv) and the encrypted data
     * are returned as a pair. The iv is needed for decryption.
     *
     * @param data The data to be encrypted.
     * @return A pair containing the initialization vector and the encrypted data.
     */
    fun encryptData(data: String): Pair<ByteArray, ByteArray> {
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())

        val iv = cipher.iv
        val encryptedData = cipher.doFinal(data.toByteArray(Charset.forName("UTF-8")))

        return Pair(iv, encryptedData)
    }

    /**
     * Decrypts the provided encrypted data using the secret key from the Android Keystore.
     *
     * The decryption is done using the AES/GCM/NoPadding cipher. The initialization vector (iv) used for the decryption
     * is provided as a parameter. The decrypted data is returned as a string.
     *
     * @param iv The initialization vector used for the decryption.
     * @param encryptedData The data to be decrypted.
     * @return The decrypted data as a string.
     */
    fun decryptData(iv: ByteArray, encryptedData: ByteArray): String {
        val cipher = Cipher.getInstance(AES_MODE)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)

        return String(cipher.doFinal(encryptedData), Charset.forName("UTF-8"))
    }

    /**
     * Retrieves the secret key from the Android Keystore. If the key does not exist, it generates a new one.
     *
     * The key is stored under a specific alias (KEY_ALIAS) and is used for AES encryption and decryption.
     * The key is generated with the following properties:
     * - Algorithm: AES
     * - Block Mode: GCM
     * - Padding: None
     * - Randomized Encryption: Disabled
     *
     * @return The secret key from the Android Keystore.
     */
    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator =
                getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(false)
                    .build()
            )
            keyGenerator.generateKey()
        }

        return keyStore.getKey(KEY_ALIAS, null) as SecretKey
    }
}
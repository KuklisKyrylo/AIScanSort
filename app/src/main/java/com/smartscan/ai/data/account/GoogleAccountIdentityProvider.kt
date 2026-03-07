package com.smartscan.ai.data.account

import android.content.Context
import android.provider.Settings
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.smartscan.ai.domain.repository.AccountIdentityProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class GoogleAccountIdentityProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : AccountIdentityProvider {

    override suspend fun getStableAccountId(): String = withContext(Dispatchers.Default) {
        val accountId = GoogleSignIn.getLastSignedInAccount(context)?.id
        if (!accountId.isNullOrBlank()) {
            return@withContext "google:$accountId"
        }

        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown"

        "device:$androidId"
    }
}


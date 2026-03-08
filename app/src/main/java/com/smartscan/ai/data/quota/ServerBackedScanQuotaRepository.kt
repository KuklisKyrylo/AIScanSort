package com.smartscan.ai.data.quota

import com.smartscan.ai.BuildConfig
import com.smartscan.ai.data.preferences.PreferencesManager
import com.smartscan.ai.domain.repository.AccountIdentityProvider
import com.smartscan.ai.domain.repository.ScanQuotaRepository
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

@Singleton
class ServerBackedScanQuotaRepository @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val accountIdentityProvider: AccountIdentityProvider
) : ScanQuotaRepository {

    private val baseUrl = BuildConfig.SCAN_QUOTA_BASE_URL.trim().trimEnd('/')

    override suspend fun refreshFromServer() {
        if (baseUrl.isBlank()) return

        val accountId = accountIdentityProvider.getStableAccountId()
        val response = runCatching { requestServerQuota(accountId, 0, increment = false) }.getOrNull() ?: return
        if (response.used > 0) {
            preferencesManager.ensureScanCountAtLeast(response.used)
        }
    }

    override suspend fun getUsedScans(): Int {
        if (baseUrl.isBlank()) {
            return preferencesManager.getScanCountNow()
        }

        val local = preferencesManager.getScanCountNow()
        val accountId = accountIdentityProvider.getStableAccountId()
        val remote = runCatching { requestServerQuota(accountId, 0, increment = false) }.getOrNull()
        val best = maxOf(local, remote?.used ?: 0)
        preferencesManager.ensureScanCountAtLeast(best)
        return best
    }

    override suspend fun incrementAndGet(): Int {
        if (baseUrl.isBlank()) {
            preferencesManager.addScanCount()
            return preferencesManager.getScanCountNow()
        }

        val accountId = accountIdentityProvider.getStableAccountId()
        val remote = runCatching { requestServerQuota(accountId, 1, increment = true) }.getOrNull()
        if (remote != null) {
            preferencesManager.ensureScanCountAtLeast(remote.used)
            return remote.used
        }

        preferencesManager.addScanCount()
        return preferencesManager.getScanCountNow()
    }

    private suspend fun requestServerQuota(
        accountId: String,
        delta: Int,
        increment: Boolean
    ): QuotaResponse = withContext(Dispatchers.IO) {
        val path = if (increment) "/v1/scan-quota/check-and-increment" else "/v1/scan-quota/check"
        val url = URL("$baseUrl$path")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 7000
            readTimeout = 7000
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
        }

        val payload = JSONObject()
            .put("accountId", accountId)
            .put("delta", delta)
            .toString()

        connection.outputStream.use { output ->
            output.write(payload.toByteArray())
        }

        val status = connection.responseCode
        if (status !in 200..299) {
            connection.disconnect()
            throw IllegalStateException("Quota API HTTP $status")
        }

        val response = connection.inputStream.bufferedReader().use { it.readText() }
        connection.disconnect()

        val json = JSONObject(response)
        QuotaResponse(
            used = json.optInt("used", 0),
            limit = json.optInt("limit", 15_000),
            allowed = json.optBoolean("allowed", true)
        )
    }
}

data class QuotaResponse(
    val used: Int,
    val limit: Int,
    val allowed: Boolean
)

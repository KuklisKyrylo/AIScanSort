package com.smartscan.ai.ui.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.smartscan.ai.BuildConfig

private const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

@Composable
fun LightBannerAd(modifier: Modifier = Modifier) {
    val adUnitId = when {
        BuildConfig.DEBUG -> TEST_BANNER_AD_UNIT_ID
        BuildConfig.ADMOB_BANNER_AD_UNIT_ID.isNotBlank() -> BuildConfig.ADMOB_BANNER_AD_UNIT_ID
        else -> TEST_BANNER_AD_UNIT_ID
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}


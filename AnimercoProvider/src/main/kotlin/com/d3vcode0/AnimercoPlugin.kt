package com.d3vcode0

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class AnimercoPlugin: Plugin() {
    override fun load(context: Context) {
        // All providers should be added in this manner. Please don't edit the providers list directly.
        registerMainAPI(AnimercoApi())
        registerExtractorAPI(AnimercoExtractor())
        registerExtractorAPI(WorkUpload())
        registerExtractorAPI(VkExtractor())
        registerExtractorAPI(Server1())
        registerExtractorAPI(Server2())
        registerExtractorAPI(Server3())
    }
}
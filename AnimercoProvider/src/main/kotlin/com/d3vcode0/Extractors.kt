package com.d3vcode0

import android.util.Log
import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.extractors.StreamWishExtractor
import okhttp3.*
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody


class QiwiExtractor : ExtractorApi() {
    override val name = "Qiwi"
    override val mainUrl = "https://qiwi.gg"
    override val requiresReferer = true

    override suspend fun getUrl(url: String, referer: String?): List<ExtractorLink>? {
        val document = app.get(url, referer = referer).document
        val getSize = document.selectFirst("span[style='opacity:1']")?.text().toString()
        val size = getSize.split(" ", limit = 2)[1]
        val source = url.split("/")[4]

        Log.d("DEV_${this.name}", "source » ${source}")

        return listOf(
            ExtractorLink(
                this.name + " - ${size}",
                this.name + " - ${size}",
                "https://spyderrock.com/${source}.mp4",
                "https://ww3.animerco.org/",
                Qualities.Unknown.value,
                isM3u8 = false

            )
        )
    } 
}

class Burstcloud : ExtractorApi() {
    override val name = "Burstcloud"
    override val mainUrl = "https://www.burstcloud.co"
    override val requiresReferer = true

    override suspend fun getUrl(url: String, referer: String?): List<ExtractorLink>? {
        val uid = url.split("/")[4]
        Log.d("DEV_${this.name}", "uid » ${uid}")

        val mediaType = "text/plain; charset=utf-8".toMediaType()
        val requestBody = "token=${uid}".toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://www.burstcloud.co/file/share-info/")
            .post(requestBody)
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:126.0) Gecko/20100101 Firefox/126.0")
            .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
            .addHeader("Accept-Language", "en-US,en;q=0.5")
            .addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            .addHeader("X-Requested-With", "XMLHttpRequest")
            .addHeader("Sec-Fetch-Dest", "empty")
            .addHeader("Sec-Fetch-Mode", "cors")
            .addHeader("Sec-Fetch-Site", "same-origin")
            .addHeader("Referer", url)
            // Add any additional headers as needed
            .build()
        Log.d("DEV_${this.name}", "Request » ${request}")
        Log.d("DEV_${this.name}", "Request » ${request.body}")

        return listOf(
            ExtractorLink(
                this.name,
                this.name,
                "https://s248.vidcache.net:8166/play/a20240604y28Kk57oG1u/[Animerco%20com]%20K8G%20-%2008.mp4?&cid=33529094",
                url,
                Qualities.Unknown.value
            )
        )
    }

    data class Main (
        val fileList: List<FileList>
    )

    data class FileList (
        val id: Long,
        val name: String,
        val description: Any? = null,
        val path: String,
        val public: Long,
        val password: Any? = null,
        val hashType: String,
        val hash: String,
        val size: Long,
        val ext: String,
        val encoding: String,
        val mimeType: String,
        val usageType: String,
        val tempFile: String,
        val tempName: String,
        val sendResult: String,
        val jobHandle: String,
        val jobLog: String,
        val startedAt: Any? = null,
        val completedAt: String,
        val status: String,
        val statusDescription: String,
        val stepTotal: Long,
        val stepComplete: Long,
        val frameTotal: Long,
        val frameComplete: Long,
        val frameDescription: String,
        val error: Any? = null,
        val erroredAt: Any? = null,
        val retryCount: Long,
        val downloadViews: Long,
        val embedViews: Long,
        val totalViews: Long,
        val folder: Long,
        val defaultFolder: Long,
        val valid: Long,
        val active: Long,
        val dateFail: Any? = null,
        val createdAt: String,
        val updatedAt: String,
        val fileID: Long,
        val userID: Long,
        val sizeShort: String
    )

    data class MainLink (
        val purchase: Purchase,
        val previewURL: String
    )

    data class Purchase (
        val cdnURL: String
    )
}

class Vk : ExtractorApi() {
    override val name = "Vk"
    override val mainUrl = "https://vk.com"
    override val requiresReferer = false

    override suspend fun getUrl(url: String, referer: String?): List<ExtractorLink>? {
        val document = app.get(url).document
        val link = document.selectFirst("a.FlatButton")?.attr("href").toString()
        Log.d("DEV_${this.name}", "Link » ${link}")

        return listOf(
            
            ExtractorLink(
                source  = this.name,
                name    = this.name,
                url     = link,
                referer = "",
                quality = Qualities.Unknown.value
            )
        )
    }
}

class Swdyu : StreamWishExtractor() {
    override var name = "Swdyu"
    override var mainUrl = "https://swdyu.com"
    override var requiresReferer = false
}

class Swhoi : StreamWishExtractor() {
    override var name = "Swhoi"
    override var mainUrl = "https://swhoi.com"
    override var requiresReferer = false
}

class Jodwish : StreamWishExtractor() {
    override var name = "Jodwish"
    override var mainUrl = "https://jodwish.com"
    override var requiresReferer = false
}
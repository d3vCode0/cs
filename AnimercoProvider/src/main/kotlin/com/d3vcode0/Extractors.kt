package com.d3vcode0

import android.util.Log
import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.extractors.StreamWishExtractor


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
        val data_text = mapOf(
            "token" to uid
        )
        val headers = mapOf(
            "Accept" to "application/json, text/javascript, */*; q=0.01",
            "Content-Type" to "application/x-www-form-urlencoded; charset=UTF-8",
            "X-Requested-With" to "XMLHttpRequest",
            "Referer" to url,
            "Cookie" to "session=8b64c4a8d3664ba62ee8e02ed9b4d6d67f998a9f"
        )
        val meta = app.post(
            "https://www.burstcloud.co/file/share-info/",
            headers = headers,
            referer = url,
            data = data_text
        ).parsedSafe<FileList>()
        val id_file = meta?.id.toString()
        Log.d("DEV_${this.name}", "id file » ${id_file}")
        val res = app.post(
            "https://www.burstcloud.co/file/play-request/",
            headers = mapOf(
                "Accept" to "application/json, text/javascript, */*; q=0.01",
                "Content-Type" to "application/x-www-form-urlencoded; charset=UTF-8",
                "X-Requested-With" to "XMLHttpRequest",
                "Referer" to url,
                "Cookie" to "session=8b64c4a8d3664ba62ee8e02ed9b4d6d67f998a9f"
            ),
            referer = url,
            data = mapOf(
                "fileId" to id_file
            )
        ).parsedSafe<Purchase>()
        val url_link = res?.cdnURL.toString()
        Log.d("DEV_${this.name}", "url link » ${url_link}")

        return listOf(
            ExtractorLink(
                this.name,
                this.name,
                url_link,
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
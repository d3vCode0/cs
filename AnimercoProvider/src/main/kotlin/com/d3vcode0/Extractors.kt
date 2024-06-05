package com.d3vcode0

import android.util.Log
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.utils.AppUtils.parseJson
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.extractors.StreamWishExtractor
import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.utils.AppUtils.tryParseJson
import okhttp3.*
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaTypeOrNull
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
        val api_info = "https://www.burstcloud.co/file/share-info/"
        val api_req = "https://www.burstcloud.co/file/play-request/"
        val uid = url.split("/")[4]
        val bodyToken = "token=${uid}"
        val requestBody = RequestBody.create("application/x-www-form-urlencoded; charset=UTF-8".toMediaTypeOrNull(), bodyToken)

        val headers = mapOf(
            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:126.0) Gecko/20100101 Firefox/126.0",
            "Content-Type" to "application/x-www-form-urlencoded; charset=UTF-8",
            "Cookie" to "session=8b64c4a8d3664ba62ee8e02ed9b4d6d67f998a9f"
        )
        data class FileList(
            @JsonProperty("id") val id: Int
        )
        data class PollingData(
            @JsonProperty("fileList") val fileList: List<FileList>
        )
        data class Main(
            @JsonProperty("hash") val hash: String
        )
        data class Purchase(
            @JsonProperty("purchase") val purchase: List<Main>
        )

        val req = app.post(api_info, requestBody = requestBody, headers = headers).text
        Log.d("DEV_${this.name}", "req » ${req}")

        val sid = tryParseJson<PollingData>(req)?.fileList?.apmap { me ->
            me.id
        }.toString().replace("[","").replace("]","")
        Log.d("DEV_${this.name}", "id » ${sid}")

        val bodyFile = "fileId=${sid}"
        val reqBody = RequestBody.create("application/x-www-form-urlencoded; charset=UTF-8".toMediaTypeOrNull(), bodyFile)
        val nextReq = app.post(api_req, requestBody = reqBody, headers = headers, referer = url).text
        Log.d("DEV_${this.name}", "nextReq » ${nextReq}")

        val hash = tryParseJson<Purchase>(nextReq)?.purchase?.apmap { me ->
            me.hash
        }.toString().replace("[","").replace("]","")
        Log.d("DEV_${this.name}", "hash » ${hash}")

        return listOf(
            ExtractorLink(
                this.name,
                this.name,
                hash,
                "https://burstcloud.co",
                Qualities.Unknown.value,
                isM3u8 = false

            )
        )
    }
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
    override var requiresReferer = true
}

class Swhoi : StreamWishExtractor() {
    override var name = "Swhoi"
    override var mainUrl = "https://swhoi.com"
    override var requiresReferer = true
}

class Jodwish : StreamWishExtractor() {
    override var name = "Jodwish"
    override var mainUrl = "https://jodwish.com"
    override var requiresReferer = true
}
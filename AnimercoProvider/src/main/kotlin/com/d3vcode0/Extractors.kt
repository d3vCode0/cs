package com.d3vcode0

import android.util.Log
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.utils.AppUtils.parseJson
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.extractors.StreamWishExtractor
import com.lagradost.cloudstream3.utils.AppUtils.tryParseJson
import okhttp3.*
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import com.fasterxml.jackson.annotation.JsonProperty


class QiwiExtractor : ExtractorApi() {
    override val name = "Qiwi"
    override val mainUrl = "https://qiwi.gg"
    override val requiresReferer = true

    override suspend fun getUrl(url: String, referer: String?): List<ExtractorLink>? {
        val document = app.get(url, referer = referer).document
        // val getSize = document.selectFirst("span[style='opacity:1']")?.text().toString()
        // val size = getSize.split(" ", limit = 2)[1]
        val source = url.split("/")[4]

        Log.d("DEV_${this.name}", "source » ${source}")

        return listOf(
            ExtractorLink(
                this.name,
                this.name,
                "https://spyderrock.com/${source}.mp4",
                "https://ww3.animerco.org/",
                Qualities.Unknown.value,
                isM3u8 = false

            )
        )
    } 
}

class Burstcloud : ExtractorApi() {
    override val name            = "Burstcloud"
    override val mainUrl         = "https://www.burstcloud.co"
    override val requiresReferer = true

    override suspend fun getUrl(url: String, referer: String?, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) {
        Log.d("DEV_${this.name}", "===== BURSTCLOUD =====")
        val token = url.split("/")[4]
        val headers = mapOf(
            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:126.0) Gecko/20100101 Firefox/126.0",
            "Content-Type" to "application/x-www-form-urlencoded; charset=UTF-8",
            "Cookie" to "session=8b64c4a8d3664ba62ee8e02ed9b4d6d67f998a9f"
        )

        Log.d("DEV_${this.name}", "Start Fetch > Info")

        // ========= fetch Info ==========
        val api_info = "https://www.burstcloud.co/file/share-info/"
        val textBody = "token=${token}"
        val bodyToken = RequestBody.create("application/x-www-form-urlencoded; charset=UTF-8".toMediaTypeOrNull(), textBody)
        val jsonString1 = app.post(api_info, requestBody = bodyToken, headers = headers).text
        Log.d("DEV_${this.name}", "req » ${jsonString1}")

        val list1 = tryParseJson<List1>(jsonString1)
        val id = list1?.fileList?.apmap { me ->
            me.id
        }.toString().replace("[","").replace("]","")
        Log.d("DEV_${this.name}", "id » ${id}")
        // ========= fetch Info ==========

        Log.d("DEV_${this.name}", "Start Fetch > Data")

        // ========= fetch data ==========
        val api_req = "https://www.burstcloud.co/file/play-request/"
        val bodyFile = "fileId=${id}"
        val reqBody = RequestBody.create("application/x-www-form-urlencoded; charset=UTF-8".toMediaTypeOrNull(), bodyFile)
        val list2Json = app.post(api_req, requestBody = reqBody, headers = headers, referer = url).text
        Log.d("DEV_${this.name}", "nextReq » ${list2Json}")

        val list2 = tryParseJson<List2>(list2Json)
        val link = list2?.purchase?.cdnUrl.toString()
        Log.d("DEV_${this.name}", "cdnUrl » ${link}")
        // ========= fetch data ==========

        return callback.invoke(
            ExtractorLink(
                this.name,
                this.name,
                link,
                "https://burstcloud.co",
                Qualities.Unknown.value,
                isM3u8 = false
            )
        )
    }

    data class Purchase(
        @JsonProperty("cdnUrl") val cdnUrl: String
    )
    data class List2(
        @JsonProperty("purchase") val purchase: Purchase
    )
    data class File(
        @JsonProperty("id") val id: Int
    )
    data class List1(
        @JsonProperty("fileList") val fileList: List<File>
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
                referer = mainUrl,
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
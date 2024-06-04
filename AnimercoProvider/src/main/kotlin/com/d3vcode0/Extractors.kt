package com.d3vcode0

import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.extractors.StreamWishExtractor


class AnimercoExtractor : ExtractorApi() {
    override val name = "Qiwi"
    override val mainUrl = "https://qiwi.gg"
    override val requiresReferer = true

    override suspend fun getUrl(url: String, referer: String?): List<ExtractorLink>? {
        val document = app.get(url, referer = referer).document
        val getSize = document.selectFirst("span[style='opacity:1']")?.text().toString()
        val size = getSize.split(" ", limit = 2)[1]
        val source = url.split("/")[4]

        return listOf(
            ExtractorLink(
                this.name + " - ${size}",
                this.name + " - ${size}",
                "https://spyderrock.com/${source}.mp4",
                "https://ww3.animerco.org/",
                Qualities.P1080.value

            )
        )
    } 
}

class WorkUpload : ExtractorApi() {
    override val mainUrl = "https://workupload.com"
    override val name = "Workupload"
    override val requiresReferer = false
    private val mainApi = "https://workupload.com/api"

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val headers = mapOf(
            "Accept" to "application/json, text/javascript, */*; q=0.01",
            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:126.0) Gecko/20100101 Firefox/126.0",
            "X-Requested-With" to "XMLHttpRequest",
            "Referer" to url,
            "Cookie" to "token=racgvfc7nilhqeogr0j0gpce6q"
        )
        val id = url.split("/")[4]
        val response = app.get("${mainApi}/file/getDownloadServer/${id}", headers=headers).text
        val jsonVideoData = AppUtils.parseJson<Source>(response)
        jsonVideoData.data.forEach {
            callback.invoke(
                ExtractorLink(
                    this.name,
                    this.name,
                    it.url,
                    mainUrl,
                    Qualities.P1080.value
                )
            )
        }
    }
    data class Source(
        @JsonProperty("success") val success: String,
        @JsonProperty("data") val data: List<GuardareData>
    )
    data class GuardareData(
        @JsonProperty("url") val url: String
    )
}

class VkExtractor : ExtractorApi() {
    override val name = "Qiwi"
    override val mainUrl = "https://vk.com"

    override suspend fun getUrl(url: String, referer: String?): List<ExtractorLink>? {
        val document = app.get(url).document
        val mpfour = document.selectFirst("a.FlatButton").attr("href")

        return listOf(
            ExtractorLink(
                this.name,
                this.name,
                mpfour,
                "https://ww3.animerco.org/",
                Qualities.P1080.value
            )
        )
    }
}

class Server1 : StreamWishExtractor() {
    override var name = "Jodwish"
    override var mainUrl = "https://jodwish.com"
}
class Server2 : StreamWishExtractor() {
    override var name = "Swhoi"
    override var mainUrl = "https://swhoi.com"
}
class Server3 : StreamWishExtractor() {
    override var name = "Swdyu"
    override var mainUrl = "https://www.swdyu.com"
}

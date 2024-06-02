package com.d3vcode0

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.extractors.StreamWishExtractor
import com.lagradost.cloudstream3.extractors.Mp4Upload
import com.lagradost.cloudstream3.extractors.YourUpload

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

class server1 : StreamWishExtractor() {
    override var name = "Jodwish"
    override var mainUrl = "https://jodwish.com"// or "https://swhoi.com" or "https://swdyu.com"
}
class server2 : Mp4Upload() {
    override var name = "Mp4Upload"
    override var mainUrl = "https://www.mp4upload.com"
}
class server3 : YourUpload() {
    override var name = "YourUpload"
    override var mainUrl = "https://www.yourupload.com"
}
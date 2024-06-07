package com.d3vcode0

import android.util.Log
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.extractors.StreamWishExtractor
import com.lagradost.cloudstream3.utils.AppUtils.tryParseJson
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
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
                Qualities.P1080.value,
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
        val token = url.split("/")[4]
        val headers = mapOf(
            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:126.0) Gecko/20100101 Firefox/126.0",
            "Content-Type" to "application/x-www-form-urlencoded; charset=UTF-8",
            "Cookie" to "session=8b64c4a8d3664ba62ee8e02ed9b4d6d67f998a9f"
        )

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
                Qualities.P1080.value,
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
                quality = Qualities.P1080.value
            )
        )
    }
}

class DriveGoogle : ExtractorApi() {
    override val name = "Google Drive"
    override val mainUrl = "https://drive.google.com"
    override val requiresReferer = false
    override suspend fun getUrl(url: String, referer: String?): List<ExtractorLink> {
        val id = url.split("/")[5]
        val newUrl = "https://drive.usercontent.google.com/download?id=${id}&export=download&authuser=0"
        val document = app.get(
            url,
            headers = mapOf(
                "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:126.0) Gecko/20100101 Firefox/126.0",
                "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
                "Cookie" to "NID=514=HDtMGRy8s63EqcvnhfRMMayE7wVdDn4qLuwgqAiw3fXjKpkEObO9K4lFxsfx37SAqqXtO4tscol7g2FEA_iRLjndsBaWoA0BeGhEoY0LOSrNdWr__heYPx9q4WRkAZ1p5qNDQihMmnQ1ZWyB1E6L-6xoYFUcnjcVpPtxclupvlfoOL-V9V-qekfQUjJ0rzoVnltKGEx2hh9sjF2zblLWxFnL3JqanuhCK2mZeDIkhYxyeGhLBanrFSYlqneTqm5FYvX4USOXcWDoVWk2qEMrGV7c9t0; SID=g.a000kggWvq7DZqQfgzFoiCfR3JgOoD9oPeeNAc_BQI6YiouWdHS_07aujsy8D4kH2B3N2e_RZAACgYKAcESARMSFQHGX2MiIh5VtPFO6bK2a645aJMmjhoVAUF8yKoa-H9w9ondODU9fkQNH2nA0076; __Secure-1PSID=g.a000kggWvq7DZqQfgzFoiCfR3JgOoD9oPeeNAc_BQI6YiouWdHS_hbcjJv6gnBYyH2ohoOYKQgACgYKAeoSARMSFQHGX2MioqxBleVo3Gd943bcHFmrYxoVAUF8yKrRZZOOYP7s58gRdJlHL2XV0076; __Secure-3PSID=g.a000kggWvq7DZqQfgzFoiCfR3JgOoD9oPeeNAc_BQI6YiouWdHS_TUgOd_IGQDTk-2lZpabZ2wACgYKAYQSARMSFQHGX2MiGV0V2Yrk7c0uPfLX99vzHxoVAUF8yKqHt6aq2zwtiFEZw-I8dh490076; HSID=AoObqdRHKxV-4En8b; SSID=A2baKAWrx1qsBrXXW; APISID=yEu1EWWcpIwdLBRb/ALydrD1VxrvzFruW6; SAPISID=AxaLSIOC5whHlwvC/ACoHAJLMA_snZPbWr; __Secure-1PAPISID=AxaLSIOC5whHlwvC/ACoHAJLMA_snZPbWr; __Secure-3PAPISID=AxaLSIOC5whHlwvC/ACoHAJLMA_snZPbWr; SIDCC=AKEyXzWUQc-YZCUnBNvxjbkrzFiEkJgYX5E0UOkB-WoknfhQYVK6p2xAJVfANDrEgGl7p9TJ; __Secure-1PSIDCC=AKEyXzWgJDv8o5tz-EvQdJmqXCyhpGE8So6o6fZRZAnhw_BQiiq-YhRPw4V1gp1YK2ZD2ssGAw; __Secure-3PSIDCC=AKEyXzUrRVYYi6mbD7cf-ACg6g8-0ZvkC0J2Sfsv89JeVPPbtNw-drXnRaJ-OXNMTKm59xU0; __Secure-1PSIDTS=sidts-CjIB3EgAEl5wJbTV_09fcGoTMjIqtx5KpDX5cKhdAMREc--ipP2s7fEguuyGworTTfgapxAA; __Secure-3PSIDTS=sidts-CjIB3EgAEl5wJbTV_09fcGoTMjIqtx5KpDX5cKhdAMREc--ipP2s7fEguuyGworTTfgapxAA",
                "Referer" to "https://drive.google.com/"
            )
        ).document
        Log.d("DEV_${this.name}", "document » ${document}")
        val download_form = document.selectFirst("form#download-form")?.attr("action")
        val export = document.selectFirst("form#download-form input[name='export']")?.attr("value")
        val authuser = document.selectFirst("form#download-form input[name='authuser']")?.attr("value")
        val confirm = document.selectFirst("form#download-form input[name='confirm']")?.attr("value")
        val uuid = document.selectFirst("form#download-form input[name='uuid']")?.attr("value")
        val link = "${download_form}?id=${id}&export=${export}&confirm=${confirm}&uuid=${uuid}"
        Log.d("DEV_${this.name}", "link google » ${link}")
        return listOf(    
            ExtractorLink(
                source  = this.name,
                name    = this.name,
                url     = link,
                referer = "https://drive.usercontent.google.com/",
                quality = Qualities.P1080.value
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
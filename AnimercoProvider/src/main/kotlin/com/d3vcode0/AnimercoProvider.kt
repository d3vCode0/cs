package com.d3vcode0

import android.annotation.TargetApi
import android.os.Build
import android.util.Log
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer
import com.lagradost.cloudstream3.network.CloudflareKiller
import com.lagradost.nicehttp.NiceResponse
import org.jsoup.nodes.Element
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class AnimercoApi : MainAPI() {
    override var mainUrl = "https://ww3.animerco.org"
    override var name = "Animerco"
    override val hasMainPage = true
    override var lang = "ar"
    override val supportedTypes = setOf(TvType.AnimeMovie, TvType.Anime)
    private val interceptor = CloudflareKiller()
    // val now = LocalDate.now()
    // val weekday = now.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH).lowercase()

    override val mainPage = mainPageOf(
        "${mainUrl}/animes/" to "Animes",
        "${mainUrl}/movies/" to "Movies",
        "${mainUrl}/episodes/" to "episodes",
        "${mainUrl}/schedule/" to "Episode Schedule"
    )
    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val regex = Regex("animes|seasons|movies")
        val isTrue = regex.containsMatchIn(request.data)
        val home = if(isTrue){

            val document = app.get(request.data + "page/$page/").document
            val list = document.select("div.page-content .row div.box-5x1").mapNotNull {
                it.toSearchResult()
            }
            HomePageList(request.name, list, false)

        } else if(request.data.contains("episodes")) {

            val document = app.get(request.data + "page/$page/").document
            val list = document.select("div.page-content .row div.col-12").mapNotNull {
                it.toSearchResult()
            }
            HomePageList(request.name, list, true)

        }else {

            val now = LocalDate.now()
            val weekday = now.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH).lowercase()
            val document = app.get(request.data).document

            val list = document.select("div.tabs-wraper div#$weekday div.box-5x1").mapNotNull {
                val title = it.selectFirst("div.info h3")!!.text()
                val href = it.selectFirst("a")!!.attr("href")
                val posterUrl = it.selectFirst("a")!!.attr("data-src")

                newAnimeSearchResponse(title, href, TvType.Anime) {
                    this.posterUrl = posterUrl
                }
            }
            HomePageList(request.name, list, false)

        }
        return newHomePageResponse(home, hasNext = !request.name.contains("Schedule"))
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst("div.info h3")?.text()?.trim() ?: return null
        val href = this.selectFirst("a")?.attr("href") ?: return null
        val posterUrl = this.selectFirst("a")?.attr("data-src") ?: return null
        val e = this.selectFirst("a.episode")?.text()?.trim()?.replace("الحلقة ", "") ?: return null
        val s = this.selectFirst("a.extra")?.text()?.trim()?.replace("الموسم ", "") ?: return null

        return if(href.contains("movies")) {
            newMovieSearchResponse(title, href, TvType.AnimeMovie) {
                this.posterUrl = posterUrl
            }
        } else {
            newAnimeSearchResponse("$title Se-$e", href, TvType.Anime) {
                this.posterUrl = posterUrl
            }
        }
    }
}
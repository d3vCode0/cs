package com.d3vcode0

import android.util.Log
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer
import com.lagradost.cloudstream3.network.CloudflareKiller
import com.lagradost.nicehttp.NiceResponse
import org.jsoup.nodes.Element
// import java.time.LocalDate
// import java.time.format.TextStyle
// import java.util.Locale

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
        "animes" to "Animes",
        "movies" to "Movies",
        "episodes" to "episodes",
        "schedule" to "Episode Schedule"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val url = if(page ==1) "${mainUrl}/${request.data}/" else "${mainUrl}/${request.data}/page/${page}/"
        var document = app.get(url).document
        if(document.select("title").text() == "Just a moment...") {
            app.get(url, interceptor = interceptor).document.also { document = it }
        }
        val regex = Regex("movies|animes|seasons|schedule")
        val home = if (regex.matches(request.name)) {
            document.select("div.container div.row div.box-5x1").mapNotNull { it.toSearchResult() }
        } else if(request.name.contains("episodes")) {
            document.select("div.container div.row div.col-12").mapNotNull { it.toSearchEpisodes() }
        } else {
            document.select("div.container div.row div.box-5x1").mapNotNull { it.toSearchOuter() }
        }

        return newHomePageResponse(request.name, home)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst("div.info a h3")?.text()?.trim() ?: return null
        val href = fixUrlNull(this.selectFirst("a")?.attr("href")) ?: return null

        return newMovieSearchResponse(title, href, TvType.Movie)
    }

    private fun Element.toSearchEpisodes(): SearchResponse? {
        val title = this.selectFirst("div.info a h3")?.text()?.trim() ?: return null
        val href = fixUrlNull(this.selectFirst("a")?.attr("href")) ?: return null
        return newAnimeSearchResponse(title, href, TvType.Anime)
    }

    private fun Element.toSearchOuter(): SearchResponse? {
        val title = this.selectFirst("div.info a h3")?.text()?.trim() ?: return null
        val href = fixUrlNull(this.selectFirst("a")?.attr("href")) ?: return null
        return newMovieSearchResponse(title, href, TvType.Movie)
    }
}
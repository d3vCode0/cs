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

    // companion object {
    //     val cookies = mapOf(
    //         "cookie" to "_ga_QE0HFP3PHP=GS1.1.1717154429.1.0.1717154429.0.0.0; _ga=GA1.1.982484213.1717154429"
    //     )
    // }

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val url = if(page ==1) "${mainUrl}/${request.data}/" else "${mainUrl}/${request.data}/page/${page}/"
        var document = app.get(url).document
        if(document.select("title").text() == "Just a moment...") {
            app.get(url, interceptor = interceptor).document.also { document = it }
        }
        val home = when (request.name) {
            "animes", "movies", "Episode Schedule" -> {
                document.select("div.page-content div.container div.row div.box-5x1").mapNotNull {
                    it.toSearchResult()
                }
            }
            else -> {
                document.select("div.page-content div.container div.row div.col-12").mapNotNull {
                    it.toSearchResult()
                }
            }
        }

        return newHomePageResponse(request.name, home)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst("div.info a h3")?.text()?.trim() ?: return null
        val href = fixUrl(this.selectFirst("a")!!.attr("href"))
        val posterUrl = fixUrlNull(this.selectFirst("a")!!.attr("data-src"))

        return newMovieSearchResponse(title, href, TvType.Movie) {
            this.posterUrl = posterUrl
        }
    }
}
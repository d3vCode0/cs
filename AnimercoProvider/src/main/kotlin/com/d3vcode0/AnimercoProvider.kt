package com.d3vcode0

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addRating
import com.lagradost.cloudstream3.LoadResponse.Companion.addDuration
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.loadExtractor
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.network.CloudflareKiller
import org.jsoup.nodes.Element
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import java.util.Base64

class AnimercoApi : MainAPI() {
    override var mainUrl = "https://ww3.animerco.org"
    override var name = "AmineRCO"
    override val hasMainPage = true
    override var lang = "ar"
    override val supportedTypes = setOf(TvType.Anime, TvType.AnimeMovie)
    private  val cfKiller = CloudflareKiller()

    val now = LocalDate.now()
    val weekday = now.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH).lowercase()

    override val mainPage = mainPageOf(
        "${mainUrl}/schedule/" to "This day: ${weekday.capitalize()}",
        "${mainUrl}/"          to "Episodes",
        "${mainUrl}/animes/"   to "Animes",
        "${mainUrl}/movies/"   to "Movies",
        "${mainUrl}/seasons/"  to "Seasons"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        return if(request.name.contains("This day:")) {
            val document = app.get(request.data).document
            val home = document.select("div.container div.tabs-wraper #${weekday} div.row div.box-5x1").mapNotNull {
                it.toSearchSchedule()
            }
            newHomePageResponse(request.name, home, false)

        } else if(request.name.contains("Animes") or request.name.contains("Movies") or request.name.contains("Seasons")){
            val document = app.get(request.data + "page/${page}").document
            val home = document.select("div.container div.row div.box-5x1").mapNotNull {
                it.toSearchResult()
            }
            newHomePageResponse(request.name, home, true)

        } else {
            val document = app.get(request.data).document
            val home = document.select("div.media-section div.row div.col-12").mapNotNull {
                it.toSearchEpisodes()
            }
            newHomePageResponse(
                list = HomePageList(
                    name = request.name,
                    list = home,
                    isHorizontalImages = true
                ),
                hasNext = false
            )
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("${mainUrl}/?s=${query}").document
        return document.select("div.page-content div.row div.col-12").mapNotNull {
            val title = it.selectFirst("div.info h3")?.text()?.trim() ?: ""
            val href = fixUrlNull(it.selectFirst("a")?.attr("href")) ?: ""
            val posterUrl = fixUrlNull(it.select("a").attr("data-src")) ?: null
            if (href.contains("animes")){
                newAnimeSearchResponse(title, href, TvType.Anime, true) {
                    this.posterUrl = posterUrl
                }
            } else {
                newMovieSearchResponse(title, href, TvType.AnimeMovie, true) {
                    this.posterUrl = posterUrl
                }
            }
        }
    }

    override suspend fun load(url: String): LoadResponse {
        var document = app.get(url).document
        try {
            if(document.select("title").text() == "Just a moment...") {
                document = app.get(url, interceptor = cfKiller, timeout = 120).document
            }
        } catch (e: Exception) {
            //empty
        }

        val title = document.select("div.head-box div.media-title h3").text().trim()
        .ifEmpty() { document.select("div.head-box div.media-title h1").text().trim() }

        val posterUrl = fixUrlNull(document.select("div.anime-card div.image").attr("data-src"))
        val posterUrlBg = fixUrlNull(document.select("div.banner").attr("data-src"))

        val tags = document.select("div.genres a").mapNotNull{ it.text().trim() }
        val plot = document.select("div.content p").text().trim()
        val year = document.select("ul.media-info li:contains(بداية العرض:) a").text().trim().toIntOrNull()
        val rating = document.select("div.votes span.score").text().trim().toRatingInt()
        val duration = document.select("ul.media-info li:contains(مدة الحلقة:) span")?.text()?.getIntFromText()
        val trailer = fixUrlNull(document.select("button#btn-trailer").attr("data-href"))


        return if(url.contains("movies")){
            newMovieLoadResponse(
                title,
                url,
                TvType.AnimeMovie,
                url
            ) {
                this.posterUrl = posterUrl
                this.tags = tags
                this.plot = plot
                this.backgroundPosterUrl = posterUrlBg
                this.duration = duration
                this.year = year
                addRating(rating)
                addTrailer(trailer)
            }
        } else if(url.contains("animes")) {
            val episodes = ArrayList<Episode>()
            val getSeasons = document.select("ul.episodes-lists li").forEach { seasonElement ->
                val numSeason = seasonElement.attr("data-number")
                val seasonUrl = seasonElement.select("a.title").attr("href")

                val seasonDoc = app.get(seasonUrl).document.select("ul.episodes-lists li")
                seasonDoc.forEach { episode ->
                    episodes.add(
                        Episode(
                            episode.select("a.title").attr("href"),
                            episode.select("a.title h3")?.first()?.ownText(),
                            numSeason.toIntOrNull(),
                            episode.attr("data-number").toIntOrNull(),
                            episode.select("a.image").attr("data-src")
                        )
                    )
                }
            }
            
            newAnimeLoadResponse(
                title,
                url,
                TvType.Anime,
                true
            ){
                this.posterUrl = posterUrl
                this.tags = tags
                this.plot = plot
                this.backgroundPosterUrl = posterUrlBg
                this.duration = duration
                this.year = year
                addEpisodes(DubStatus.Subbed, episodes)
                addRating(rating)
                addTrailer(trailer)
            }
        } else if(url.contains("seasons")){
            val episodes = ArrayList<Episode>()
            document.select("ul.episodes-lists li").map { episode ->
                episodes.add(
                    Episode(
                        episode.select("a.title").attr("href"),
                        episode.select("a.title h3")?.first()?.ownText(),
                        null,
                        episode.attr("data-number").toIntOrNull(),
                        episode.select("a.image").attr("data-src")
                    )
                )
            }
            newAnimeLoadResponse(
                title,
                url,
                TvType.Anime,
                true
            ){
                this.posterUrl = posterUrl
                this.tags = tags
                this.plot = plot
                this.backgroundPosterUrl = posterUrlBg
                addEpisodes(DubStatus.Subbed, episodes)
                addRating(rating)
            }
        } else {
            val title = document.selectFirst("div.page-head h1")?.text() ?: ""
            val posterUrl = document.selectFirst("a#click-player")?.attr("data-src")
            val plot = document.selectFirst("div.server-notice strong")?.text()
            val addTime = document.selectFirst("span.publish-date")?.text()
            newMovieLoadResponse(
                title,
                url,
                TvType.AnimeMovie,
                url
            ){
                this.posterUrl = posterUrl
                this.plot = "${plot} \n ${addTime}"
            }
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val document = app.get(data).document
        document.select("table.table tr td a").map {
            it.attr("href").let { url ->
                val iframe = app.get(url).document

                val kuri = iframe.selectFirst("a#link")?.attr("data-url").toString()
                val decoded = Base64.getDecoder().decode(kuri)
                val deurl = String(decoded)
                if(deurl.contains("yourupload")){
                    val newUrl = deurl.replace("watch", "embed")
                    loadExtractor(deurl, subtitleCallback, callback)
                } else{
                    loadExtractor(deurl, subtitleCallback, callback)
                }
            }
        }
        return true
    }

    private fun Element.toSearchSchedule(): SearchResponse? {
        val title = this.selectFirst("div.info h3")?.text()?.trim() ?: ""
        val season = this.selectFirst("div.info a.extra")?.text()?.trim()?.replace("الموسم ", "") ?: ""
        val href = fixUrl(this.selectFirst("a")?.attr("href") ?: return null)
        val posterUrl = fixUrlNull(this.select("a").attr("data-src"))

        return newAnimeSearchResponse("${title} S-${season}", href, TvType.Anime, true) {
            this.posterUrl = posterUrl
        }
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst("div.info h3")?.text()?.trim() ?: ""
        val href = fixUrl(this.selectFirst("a")?.attr("href") ?: return null)
        val posterUrl = fixUrlNull(this.select("a").attr("data-src"))

        return if (href.contains("animes")){
            newAnimeSearchResponse(title, href, TvType.Anime, true) {
                this.posterUrl = posterUrl
            }
        } else if(href.contains("seasons")) {
            val se = this.selectFirst("div.info a.extra h4")?.text()?.trim()?.replace("الموسم ", "") ?: ""
            newAnimeSearchResponse("${title} S${se}", href, TvType.Anime, true) {
                this.posterUrl = posterUrl
            }
        } else {
            newMovieSearchResponse(title, href, TvType.AnimeMovie, true) {
                this.posterUrl = posterUrl
            }
        }
    }

    private fun Element.toSearchEpisodes(): SearchResponse? {
        val title = this.selectFirst("div.info h3")?.text()?.trim() ?: ""
        val href = fixUrl(this.selectFirst("a")?.attr("href") ?: return null)
        val posterUrl = fixUrlNull(this.select("a").attr("data-src"))
        val episode = this.selectFirst("div.info a.badge")?.text()?.trim()?.replace("الحلقة ", "") ?: ""
        val season = this.selectFirst("div.info span.anime-type")?.text()?.trim()?.replace("الموسم ", "") ?: ""

        return newAnimeSearchResponse("${title} S${season}", href, TvType.Anime, true) {
            this.posterUrl = posterUrl
            addSub(episodes = episode?.toIntOrNull())
        }
    }

    private fun String.getIntFromText(): Int? {
        return Regex("""\d+""").find(this)?.groupValues?.firstOrNull()?.toIntOrNull()
    }
}
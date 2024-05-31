package com.d3vcode0

import com.lagradost.cloudstream3.*
import org.jsoup.nodes.Element

class AnimercoApi : MainAPI() {
    override var mainUrl = ""
    override var name = ""
    override val hasMainPage = true
    override var lang = "pt"

    override val supportedTypes = setOf(
        TvType.Anime,
        TvType.AnimeMovie
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get("${mainUrl}/schedule/").document
        val isToDay = "monday"
        val home = mutableListOf<HomePageList>()
        document.select("div.tabs-wraper ${isToDay}").map {
            div ->
            val header = div.selectFirst("div.page-head h2")?.text() ?: return@map
            val child = HomePageList(
                header,
                div.select("div.box-5x1").mapNotNull {
                    it.toSearchSchedule()
                },
                false
            )
            home.add(child)
        }
        return HomePageResponse(home)
    }

    private fun Element.toSearchSchedule(): AnimeSearchResponse? {
        val title = this.selectFirst("div.info h3")?.text()?.trim() ?: ""
        val href = fixUrl(this.selectFirst("a")?.attr("href") ?: return null)
        val posterUrl = fixUrlNull(this.select("a").attr("data-src"))

        return newAnimeSearchResponse(title, href, TvType.Anime) {
            this.posterUrl = posterUrl
        }
    }
}
package example

import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.argamap
import com.lagradost.cloudstream3.utils.AppUtils
import com.lagradost.cloudstream3.utils.ExtractorLink
import example.CodeExtractor.invoke2embed
import example.CodeExtractor.invokeAllMovieland
import example.CodeExtractor.invokeAnimes
import example.CodeExtractor.invokeAoneroom
import example.CodeExtractor.invokeCinemaTv
import example.CodeExtractor.invokeDoomovies
import example.CodeExtractor.invokeDotmovies
import example.CodeExtractor.invokeDramaday
import example.CodeExtractor.invokeDreamfilm
import example.CodeExtractor.invokeDumpStream
import example.CodeExtractor.invokeEmovies
import example.CodeExtractor.invokeFilmxy
import example.CodeExtractor.invokeFlixon
import example.CodeExtractor.invokeGoku
import example.CodeExtractor.invokeKimcartoon
import example.CodeExtractor.invokeKisskh
import example.CodeExtractor.invokeLing
import example.CodeExtractor.invokeM4uhd
import example.CodeExtractor.invokeMoflix
import example.CodeExtractor.invokeMoviesdrive
import example.CodeExtractor.invokeMultimovies
import example.CodeExtractor.invokeNetmovies
import example.CodeExtractor.invokeNinetv
import example.CodeExtractor.invokeNowTv
import example.CodeExtractor.invokeRStream
import example.CodeExtractor.invokeRidomovies
import example.CodeExtractor.invokeShowflix
import example.CodeExtractor.invokeVegamovies
import example.CodeExtractor.invokeVidSrc
import example.CodeExtractor.invokeVidsrcto
import example.CodeExtractor.invokeWatchCartoon
import example.CodeExtractor.invokeWatchsomuch
import example.CodeExtractor.invokeZoechip
import example.CodeExtractor.invokeZshow

class CodeStreamLite : CodeStream() {
  override var name = "CodeStream-Lite"

  override suspend fun loadLinks(
    data: String,
    isCasting: Boolean,
    subtitleCallback: (SubtitleFile) -> Unit,
    callback: (ExtractorLink) -> Unit
  ): Boolean {

    val res = AppUtils.parseJson<LinkData>(data)

    argamap(
      {
        if (!res.isAnime) invokeMoflix(res.id, res.season, res.episode, callback)
      },
      {
        if (!res.isAnime) invokeWatchsomuch(
          res.imdbId,
          res.season,
          res.episode,
          subtitleCallback
        )
      },
      {
        invokeDumpStream(
          res.title,
          res.year,
          res.season,
          res.episode,
          subtitleCallback
        )
      },
      {
        if (!res.isAnime) invokeNinetv(
          res.id,
          res.season,
          res.episode,
          subtitleCallback,
          callback
        )
      },
      {
        invokeGoku(
          res.title,
          res.year,
          res.season,
          res.lastSeason,
          res.episode,
          subtitleCallback,
          callback
        )
      },
      {
        invokeVidSrc(res.id, res.season, res.episode, callback)
      },
      {
        if (!res.isAnime && res.isCartoon) invokeWatchCartoon(
          res.title,
          res.year,
          res.season,
          res.episode,
          subtitleCallback,
          callback
        )
      },
      {
        if (res.isAnime) invokeAnimes(
          res.title,
          res.epsTitle,
          res.date,
          res.airedDate,
          res.season,
          res.episode,
          subtitleCallback,
          callback
        )
      },
      {
        if (!res.isAnime) invokeDreamfilm(
          res.title,
          res.season,
          res.episode,
          subtitleCallback,
          callback
        )
      },
      {
        if (!res.isAnime) invokeFilmxy(
          res.imdbId,
          res.season,
          res.episode,
          subtitleCallback,
          callback
        )
      },
      {
        if (!res.isAnime && res.isCartoon) invokeKimcartoon(
          res.title,
          res.season,
          res.episode,
          subtitleCallback,
          callback
        )
      },
      {
        if (!res.isAnime) invokeVidsrcto(
          res.imdbId,
          res.season,
          res.episode,
          subtitleCallback,
          callback
        )
      },
      {
        if (res.isAsian || res.isAnime) invokeKisskh(
          res.title,
          res.season,
          res.episode,
          res.isAnime,
          res.lastSeason,
          subtitleCallback,
          callback
        )
      },
      {
        if (!res.isAnime) invokeLing(
          res.title, res.airedYear
            ?: res.year, res.season, res.episode, subtitleCallback, callback
        )
      },
      {
        if (!res.isAnime) invokeM4uhd(
          res.title, res.airedYear
            ?: res.year, res.season, res.episode, subtitleCallback, callback
        )
      },
      {
        if (!res.isAnime) invokeRStream(res.id, res.season, res.episode, callback)
      },
      {
        if (!res.isAnime) invokeFlixon(
          res.id,
          res.imdbId,
          res.season,
          res.episode,
          callback
        )
      },
      {
        invokeCinemaTv(
          res.imdbId, res.title, res.airedYear
            ?: res.year, res.season, res.episode, subtitleCallback, callback
        )
      },
      {
        if (!res.isAnime) invokeNowTv(res.id, res.imdbId, res.season, res.episode, callback)
      },
      {
        if (!res.isAnime) invokeAoneroom(
          res.title, res.airedYear
            ?: res.year, res.season, res.episode, subtitleCallback, callback
        )
      },
      {
        if (!res.isAnime) invokeRidomovies(
          res.id,
          res.imdbId,
          res.season,
          res.episode,
          subtitleCallback,
          callback
        )
      },
      {
        if (!res.isAnime) invokeEmovies(
          res.title,
          res.year,
          res.season,
          res.episode,
          subtitleCallback,
          callback
        )
      },
      {
        invokeMultimovies(
          multimoviesAPI,
          res.title,
          res.season,
          res.episode,
          subtitleCallback,
          callback
        )
      },
      {
        invokeNetmovies(
          res.title,
          res.year,
          res.season,
          res.episode,
          subtitleCallback,
          callback
        )
      },
      {
        if (!res.isAnime) invokeAllMovieland(res.imdbId, res.season, res.episode, callback)
      },
      {
        if (!res.isAnime && res.season == null) invokeDoomovies(
          res.title,
          subtitleCallback,
          callback
        )
      },
      {
        if (res.isAsian) invokeDramaday(
          res.title,
          res.year,
          res.season,
          res.episode,
          subtitleCallback,
          callback
        )
      },
      {
        if (!res.isAnime) invoke2embed(
          res.imdbId,
          res.season,
          res.episode,
          subtitleCallback,
          callback
        )
      },
      {
        invokeZshow(
          res.title,
          res.year,
          res.season,
          res.episode,
          subtitleCallback,
          callback
        )
      },
      {
        if (!res.isAnime) invokeShowflix(
          res.title,
          res.year,
          res.season,
          res.episode,
          subtitleCallback,
          callback
        )
      },
      {
        if (!res.isAnime) invokeZoechip(
          res.title,
          res.year,
          res.season,
          res.episode,
          callback
        )
      },
      /*{
          if (!res.isAnime) invokeNepu(
              res.title,
              res.airedYear ?: res.year,
              res.season,
              res.episode,
              callback
          )
      }

       */
      {
        if (!res.isAnime) invokeMoviesdrive(
          res.title,
          res.season,
          res.episode,
          res.year,
          subtitleCallback,
          callback
        )
      },
      {
        if (!res.isAnime) invokeVegamovies(
          res.title,
          res.year,
          res.season,
          res.lastSeason,
          res.episode,
          subtitleCallback,
          callback
        )
      },
      {
        if (!res.isAnime) invokeDotmovies(
          res.title,
          res.year,
          res.season,
          res.lastSeason,
          res.episode,
          subtitleCallback,
          callback
        )
      },
    )
    return true
  }

}
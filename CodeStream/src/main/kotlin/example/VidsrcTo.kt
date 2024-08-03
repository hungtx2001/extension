package example

import android.util.Base64
import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.apmap
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.extractors.FileMoon
import com.lagradost.cloudstream3.extractors.Vidplay
import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import java.net.URLDecoder
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class AnyVidplay(hostUrl: String) : Vidplay() {
  override val mainUrl = hostUrl
}

class VidsrcTo : ExtractorApi() {
  override val name = "VidSrcTo"
  override val mainUrl = "https://vidsrc.to"
  override val requiresReferer = true

  override suspend fun getUrl(
    url: String,
    referer: String?,
    subtitleCallback: (SubtitleFile) -> Unit,
    callback: (ExtractorLink) -> Unit
  ) {
    val mediaId = app.get(url).document.selectFirst("ul.episodes li a")?.attr("data-id")
    val res =
      app.get("$mainUrl/ajax/embed/episode/$mediaId/sources")
        .parsedSafe<VidsrctoEpisodeSources>()
    if (res?.status == 200) {
      res.result?.apmap { source ->
        val embedRes =
          app.get("$mainUrl/ajax/embed/source/${source.id}")
            .parsedSafe<VidsrctoEmbedSource>()
        val finalUrl = decryptUrl(embedRes?.result?.encUrl ?: "")
        when (source.title) {
          "Vidplay" ->
            AnyVidplay(finalUrl.substringBefore("/e/"))
              .getUrl(finalUrl, referer, subtitleCallback, callback)

          "Filemoon" -> FileMoon().getUrl(finalUrl, referer, subtitleCallback, callback)
          else -> {}
        }
      }
    }
  }

  private fun decryptUrl(encUrl: String): String {
    var data = encUrl.toByteArray()
    data = Base64.decode(data, Base64.URL_SAFE)
    val rc4Key = SecretKeySpec("WXrUARXb1aDLaZjI".toByteArray(), "RC4")
    val cipher = Cipher.getInstance("RC4")
    cipher.init(Cipher.DECRYPT_MODE, rc4Key, cipher.parameters)
    data = cipher.doFinal(data)
    return URLDecoder.decode(data.toString(Charsets.UTF_8), "utf-8")
  }

  data class VidsrctoEpisodeSources(
    @JsonProperty("status") val status: Int,
    @JsonProperty("result") val result: List<VidsrctoResult>?
  )

  data class VidsrctoResult(
    @JsonProperty("id") val id: String,
    @JsonProperty("title") val title: String
  )

  data class VidsrctoEmbedSource(
    @JsonProperty("status") val status: Int,
    @JsonProperty("result") val result: VidsrctoUrl
  )

  data class VidsrctoUrl(@JsonProperty("url") val encUrl: String)
}

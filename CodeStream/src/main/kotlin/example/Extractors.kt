package example

import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.APIHolder.getCaptchaToken
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.apmap
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.extractors.*
import com.lagradost.cloudstream3.utils.*
import java.math.BigInteger
import java.security.MessageDigest

open class Playm4u : ExtractorApi() {
  override val name = "Playm4u"
  override val mainUrl = "https://play9str.playm4u.xyz"
  override val requiresReferer = true
  private val password = "plhq@@@22"

  override suspend fun getUrl(
    url: String,
    referer: String?,
    subtitleCallback: (SubtitleFile) -> Unit,
    callback: (ExtractorLink) -> Unit
  ) {
    val document = app.get(url, referer = referer).document
    val script = document.selectFirst("script:containsData(idfile =)")?.data() ?: return
    val passScript = document.selectFirst("script:containsData(domain_ref =)")?.data() ?: return

    val pass = passScript.substringAfter("CryptoJS.MD5('").substringBefore("')")
    val amount = passScript.substringAfter(".toString()), ").substringBefore("));").toInt()

    val idFile = "idfile".findIn(script)
    val idUser = "idUser".findIn(script)
    val domainApi = "DOMAIN_API".findIn(script)
    val nameKeyV3 = "NameKeyV3".findIn(script)
    val dataEnc = caesarShift(
      mahoa(
        "Win32|$idUser|$idFile|$referer",
        md5(pass)
      ), amount
    ).toHex()

    val captchaKey =
      document.select("script[src*=https://www.google.com/recaptcha/api.js?render=]")
        .attr("src").substringAfter("render=")
    val token = getCaptchaToken(
      url,
      captchaKey,
      referer = referer
    )

    val source = app.post(
      domainApi, data = mapOf(
        "namekey" to nameKeyV3,
        "token" to "$token",
        "referrer" to "$referer",
        "data" to "$dataEnc|${md5(dataEnc + password)}",
      ), referer = "$mainUrl/"
    ).parsedSafe<Source>()

    callback.invoke(
      ExtractorLink(
        this.name,
        this.name,
        source?.data ?: return,
        "$mainUrl/",
        Qualities.P1080.value,
        INFER_TYPE
      )
    )

    subtitleCallback.invoke(
      SubtitleFile(
        source.sub?.substringBefore("|")?.toLanguage() ?: return,
        source.sub.substringAfter("|"),
      )
    )

  }

  private fun caesarShift(str: String, amount: Int): String {
    var output = ""
    val adjustedAmount = if (amount < 0) amount + 26 else amount
    for (element in str) {
      var c = element
      if (c.isLetter()) {
        val code = c.code
        c = when (code) {
          in 65..90 -> ((code - 65 + adjustedAmount) % 26 + 65).toChar()
          in 97..122 -> ((code - 97 + adjustedAmount) % 26 + 97).toChar()
          else -> c
        }
      }
      output += c
    }
    return output
  }

  private fun mahoa(input: String, key: String): String {
    val a = CryptoJS.encrypt(key, input)
    return a.replace("U2FsdGVkX1", "")
      .replace("/", "|a")
      .replace("+", "|b")
      .replace("=", "|c")
      .replace("|", "-z")
  }

  private fun md5(input: String): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
  }

  private fun String.toHex(): String {
    return this.toByteArray().joinToString("") { "%02x".format(it) }
  }

  private fun String.findIn(data: String): String {
    return "$this\\s*=\\s*[\"'](\\S+)[\"'];".toRegex().find(data)?.groupValues?.get(1) ?: ""
  }

  private fun String.toLanguage(): String {
    return if (this == "EN") "English" else this
  }

  data class Source(
    @JsonProperty("data") val data: String? = null,
    @JsonProperty("sub") val sub: String? = null,
  )

}

open class M4ufree : ExtractorApi() {
  override val name = "M4ufree"
  override val mainUrl = "https://play.playm4u.xyz"
  override val requiresReferer = true

  override suspend fun getUrl(
    url: String,
    referer: String?,
    subtitleCallback: (SubtitleFile) -> Unit,
    callback: (ExtractorLink) -> Unit
  ) {
    val document = session.get(url, referer = referer).document
    val script = document.selectFirst("script:containsData(idfile =)")?.data() ?: return

    val idFile = "idfile".findIn(script)
    val idUser = "idUser".findIn(script)

    val video = session.post(
      "https://api-plhq.playm4u.xyz/apidatard/$idUser/$idFile",
      data = mapOf("referrer" to "$referer"),
      headers = mapOf(
        "Accept" to "*/*",
        "X-Requested-With" to "XMLHttpRequest",
      )
    ).text.let { AppUtils.tryParseJson<Source>(it) }?.data

    callback.invoke(
      ExtractorLink(
        this.name,
        this.name,
        video ?: return,
        referer ?: "",
        Qualities.P720.value,
        INFER_TYPE
      )
    )

  }

  private fun String.findIn(data: String): String? {
    return "$this\\s*=\\s*[\"'](\\S+)[\"'];".toRegex().find(data)?.groupValues?.get(1)
  }

  data class Source(
    @JsonProperty("data") val data: String? = null,
  )

}

class VCloud : ExtractorApi() {
  override val name: String = "V-Cloud"
  override val mainUrl: String = "https://vcloud.lol"
  override val requiresReferer = false

  override suspend fun getUrl(
    url: String,
    referer: String?,
    subtitleCallback: (SubtitleFile) -> Unit,
    callback: (ExtractorLink) -> Unit
  ) {
    val doc = app.get(url).document
    val scriptTag = doc.selectFirst("script:containsData(url)")?.toString()
    val urlValue = scriptTag?.let { Regex("var url = '([^']*)'").find(it)?.groupValues?.get(1) } ?: ""
    val document = app.get(urlValue).document

    val size = document.selectFirst("i#size")?.text()
    val div = document.selectFirst("div.card-body")
    val header = document.selectFirst("div.card-header")?.text()
    div?.select("a")?.apmap {
      val link = it.attr("href")
      if (link.contains("pixeldra")) {
        callback.invoke(
          ExtractorLink(
            "Pixeldrain",
            "Pixeldrain $size",
            link,
            "",
            getIndexQuality(header),
          )
        )
      } else if (link.contains("dl.php")) {
        val response = app.get(link, allowRedirects = false)
        val downloadLink = response.headers["location"].toString().split("link=").getOrNull(1) ?: link
        callback.invoke(
          ExtractorLink(
            "V-Cloud[Download]",
            "V-Cloud[Download] $size",
            downloadLink,
            "",
            getIndexQuality(header),
          )
        )
      } else if (link.contains(".dev")) {
        callback.invoke(
          ExtractorLink(
            "V-Cloud",
            "V-Cloud $size",
            link,
            "",
            getIndexQuality(header),
          )
        )
      } else {
        loadExtractor(link, subtitleCallback, callback)
      }
    }
  }

  private fun getIndexQuality(str: String?): Int {
    return Regex("(\\d{3,4})[pP]").find(str ?: "")?.groupValues?.getOrNull(1)?.toIntOrNull()
      ?: Qualities.Unknown.value
  }

}

open class Streamruby : ExtractorApi() {
  override val name = "Streamruby"
  override val mainUrl = "https://streamruby.com"
  override val requiresReferer = true

  override suspend fun getUrl(
    url: String,
    referer: String?,
    subtitleCallback: (SubtitleFile) -> Unit,
    callback: (ExtractorLink) -> Unit
  ) {
    val id = "/e/(\\w+)".toRegex().find(url)?.groupValues?.get(1) ?: return
    val response = app.post(
      "$mainUrl/dl", data = mapOf(
        "op" to "embed",
        "file_code" to id,
        "auto" to "1",
        "referer" to "",
      ), referer = referer
    )
    val script = if (!getPacked(response.text).isNullOrEmpty()) {
      getAndUnpack(response.text)
    } else {
      response.document.selectFirst("script:containsData(sources:)")?.data()
    }
    val m3u8 = Regex("file:\\s*\"(.*?m3u8.*?)\"").find(script ?: return)?.groupValues?.getOrNull(1)
    M3u8Helper.generateM3u8(
      name,
      m3u8 ?: return,
      mainUrl
    ).forEach(callback)
  }

}

open class Uploadever : ExtractorApi() {
  override val name = "Uploadever"
  override val mainUrl = "https://uploadever.in"
  override val requiresReferer = true

  override suspend fun getUrl(
    url: String,
    referer: String?,
    subtitleCallback: (SubtitleFile) -> Unit,
    callback: (ExtractorLink) -> Unit
  ) {
    var res = app.get(url, referer = referer).document
    val formUrl = res.select("form").attr("action")
    var formData = res.select("form input").associate { it.attr("name") to it.attr("value") }
      .filterKeys { it != "go" }
      .toMutableMap()
    val formReq = app.post(formUrl, data = formData)

    res = formReq.document
    val captchaKey =
      res.select("script[src*=https://www.google.com/recaptcha/api.js?render=]").attr("src")
        .substringAfter("render=")
    val token = getCaptchaToken(url, captchaKey, referer = "$mainUrl/")
    formData = res.select("form#down input").associate { it.attr("name") to it.attr("value") }
      .toMutableMap()
    formData["adblock_detected"] = "0"
    formData["referer"] = url
    res = app.post(
      formReq.url,
      data = formData + mapOf("g-recaptcha-response" to "$token"),
      cookies = formReq.cookies
    ).document
    val video = res.select("div.download-button a.btn.btn-dow.recaptchav2").attr("href")

    callback.invoke(
      ExtractorLink(
        this.name,
        this.name,
        video,
        "",
        Qualities.Unknown.value,
        INFER_TYPE
      )
    )

  }

}

open class Netembed : ExtractorApi() {
  override var name: String = "Netembed"
  override var mainUrl: String = "https://play.netembed.xyz"
  override val requiresReferer = true

  override suspend fun getUrl(
    url: String,
    referer: String?,
    subtitleCallback: (SubtitleFile) -> Unit,
    callback: (ExtractorLink) -> Unit
  ) {
    val response = app.get(url, referer = referer)
    val script = getAndUnpack(response.text)
    val m3u8 = Regex("((https:|http:)//.*\\.m3u8)").find(script)?.groupValues?.getOrNull(1) ?: return

    M3u8Helper.generateM3u8(this.name, m3u8, "$mainUrl/").forEach(callback)
  }
}

open class Ridoo : ExtractorApi() {
  override val name = "Ridoo"
  override var mainUrl = "https://ridoo.net"
  override val requiresReferer = true
  open val defaulQuality = Qualities.P1080.value

  override suspend fun getUrl(
    url: String,
    referer: String?,
    subtitleCallback: (SubtitleFile) -> Unit,
    callback: (ExtractorLink) -> Unit
  ) {
    val response = app.get(url, referer = referer)
    val script = if (!getPacked(response.text).isNullOrEmpty()) {
      getAndUnpack(response.text)
    } else {
      response.document.selectFirst("script:containsData(sources:)")?.data()
    }
    val m3u8 = Regex("file:\\s*\"(.*?m3u8.*?)\"").find(script ?: return)?.groupValues?.getOrNull(1)
    val quality = "qualityLabels.*\"(\\d{3,4})[pP]\"".toRegex().find(script)?.groupValues?.get(1)
    callback.invoke(
      ExtractorLink(
        this.name,
        this.name,
        m3u8 ?: return,
        mainUrl,
        quality?.toIntOrNull() ?: defaulQuality,
        INFER_TYPE
      )
    )
  }

}

open class Gdmirrorbot : ExtractorApi() {
  override val name = "Gdmirrorbot"
  override val mainUrl = "https://gdmirrorbot.nl"
  override val requiresReferer = true

  override suspend fun getUrl(
    url: String,
    referer: String?,
    subtitleCallback: (SubtitleFile) -> Unit,
    callback: (ExtractorLink) -> Unit
  ) {
    app.get(url, referer = referer).document.select("ul#videoLinks li").apmap {
      loadExtractor(it.attr("data-link"), "$mainUrl/", subtitleCallback, callback)
    }
  }

}

open class Streamvid : ExtractorApi() {
  override val name = "Streamvid"
  override val mainUrl = "https://streamvid.net"
  override val requiresReferer = true

  override suspend fun getUrl(
    url: String,
    referer: String?,
    subtitleCallback: (SubtitleFile) -> Unit,
    callback: (ExtractorLink) -> Unit
  ) {
    val response = app.get(url, referer = referer)
    val script = if (!getPacked(response.text).isNullOrEmpty()) {
      getAndUnpack(response.text)
    } else {
      response.document.selectFirst("script:containsData(sources:)")?.data()
    }
    val m3u8 =
      Regex("src:\\s*\"(.*?m3u8.*?)\"").find(script ?: return)?.groupValues?.getOrNull(1)
    M3u8Helper.generateM3u8(
      name,
      m3u8 ?: return,
      mainUrl
    ).forEach(callback)
  }

}

open class Embedrise : ExtractorApi() {
  override val name = "Embedrise"
  override val mainUrl = "https://embedrise.com"
  override val requiresReferer = true

  override suspend fun getUrl(
    url: String,
    referer: String?,
    subtitleCallback: (SubtitleFile) -> Unit,
    callback: (ExtractorLink) -> Unit
  ) {
    val res = app.get(url, referer = referer).document
    val title = res.select("title").text()
    val video = res.select("video#player source").attr("src")

    callback.invoke(
      ExtractorLink(
        this.name,
        this.name,
        video,
        "$mainUrl/",
        getIndexQuality(title),
        INFER_TYPE
      )
    )

  }

}

class FilemoonNl : Ridoo() {
  override val name = "FilemoonNl"
  override var mainUrl = "https://filemoon.nl"
  override val defaulQuality = Qualities.Unknown.value
}

class Alions : Ridoo() {
  override val name = "Alions"
  override var mainUrl = "https://alions.pro"
  override val defaulQuality = Qualities.Unknown.value
}

class Streamwish : Filesim() {
  override val name = "Streamwish"
  override var mainUrl = "https://streamwish.to"
}

class UqloadsXyz : Filesim() {
  override val name = "Uqloads"
  override var mainUrl = "https://uqloads.xyz"
}

class Pixeldra : PixelDrain() {
  override val mainUrl = "https://pixeldra.in"
}

class Snolaxstream : Filesim() {
  override val mainUrl = "https://snolaxstream.online"
  override val name = "Snolaxstream"
}

class Do0od : DoodLaExtractor() {
  override var mainUrl = "https://do0od.com"
}

class TravelR : GMPlayer() {
  override val name = "TravelR"
  override val mainUrl = "https://travel-russia.xyz"
}

class Mwish : Filesim() {
  override val name = "Mwish"
  override var mainUrl = "https://mwish.pro"
}

class Animefever : Filesim() {
  override val name = "Animefever"
  override var mainUrl = "https://animefever.fun"
}

class Multimovies : Ridoo() {
  override val name = "Multimovies"
  override var mainUrl = "https://multimovies.cloud"
}

class MultimoviesSB : StreamSB() {
  override var name = "Multimovies"
  override var mainUrl = "https://multimovies.website"
}

class Yipsu : Voe() {
  override val name = "Yipsu"
  override var mainUrl = "https://yip.su"
}

class Embedwish : Filesim() {
  override val name = "Embedwish"
  override var mainUrl = "https://embedwish.com"
}

class Dwish : Filesim() {
  override val name = "Dwish"
  override var mainUrl = "https://dwish.pro"
}

class Dlions : VidhideExtractor() {
  override var name = "Dlions"
  override var mainUrl = "https://dlions.pro"
}

class Filelion : Filesim() {
  override val name = "Filelion"
  override var mainUrl = "https://filelions.to"
}

class Flaswish : Ridoo() {
  override val name = "Flaswish"
  override var mainUrl = "https://flaswish.com"
  override val defaulQuality = Qualities.Unknown.value
}

class Comedyshow : Jeniusplay() {
  override val mainUrl = "https://comedyshow.to"
  override val name = "Comedyshow"
}

class Bestx : Chillx() {
  override val name = "Bestx"
  override val mainUrl = "https://bestx.stream"
}


class Graceaddresscommunity : Voe() {
  override var mainUrl = "https://graceaddresscommunity.com"
}

suspend fun unBlockedlinks(url: String): String? {
  val driveLink = bypassHrefli(url) ?: ""
  val driveReq = app.get(driveLink)
  val driveRes = driveReq.document
  val finallink = driveRes.selectFirst("a.btn.btn-danger")?.attr("href")
  return finallink
}
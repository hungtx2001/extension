package example

import android.content.Context
import com.lagradost.cloudstream3.extractors.*
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin

@CloudstreamPlugin
class CodeStreamPlugin : Plugin() {
  override fun load(context: Context) {
    registerMainAPI(CodeStream())
    registerMainAPI(CodeStreamLite())
    registerExtractorAPI(Animefever())
    registerExtractorAPI(Multimovies())
    registerExtractorAPI(MultimoviesSB())
    registerExtractorAPI(VidsrcTo())
    registerExtractorAPI(Yipsu())
    registerExtractorAPI(Mwish())
    registerExtractorAPI(TravelR())
    registerExtractorAPI(Playm4u())
    registerExtractorAPI(Vidplay())
    registerExtractorAPI(FileMoon())
    registerExtractorAPI(VCloud())
    registerExtractorAPI(Bestx())
    registerExtractorAPI(Snolaxstream())
    registerExtractorAPI(Pixeldra())
    registerExtractorAPI(Graceaddresscommunity())
    registerExtractorAPI(M4ufree())
    registerExtractorAPI(Streamruby())
    registerExtractorAPI(Streamwish())
    registerExtractorAPI(Filelion())
    registerExtractorAPI(DoodYtExtractor())
    registerExtractorAPI(Dlions())
    registerExtractorAPI(MixDrop())
    registerExtractorAPI(Dwish())
    registerExtractorAPI(Embedwish())
    registerExtractorAPI(UqloadsXyz())
    registerExtractorAPI(Uploadever())
    registerExtractorAPI(Netembed())
    registerExtractorAPI(Flaswish())
    registerExtractorAPI(Comedyshow())
    registerExtractorAPI(Ridoo())
    registerExtractorAPI(Streamvid())
    registerExtractorAPI(StreamTape())
    registerExtractorAPI(Do0od())
    registerExtractorAPI(Embedrise())
    registerExtractorAPI(Gdmirrorbot())
    registerExtractorAPI(FilemoonNl())
    registerExtractorAPI(Alions())
    registerExtractorAPI(Vidmolyme())
  }
}
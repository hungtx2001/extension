// use an integer for version numbers
version = 1


cloudstream {
  language = "vi"

  authors = listOf("Hung")

  /**
   * Status int as the following:
   * 0: Down
   * 1: Ok
   * 2: Slow
   * 3: Beta only
   * */
  status = 1 // will be 3 if unspecified
  tvTypes = listOf(
    "AsianDrama",
    "Anime",
    "TvSeries",
    "Movie",
  )

  iconUrl = "https://www.google.com/s2/favicons?domain=phimmoichill.net&sz=%size%"
}
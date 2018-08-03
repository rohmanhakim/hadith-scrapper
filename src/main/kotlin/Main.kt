import org.jsoup.Jsoup
import org.jsoup.nodes.Document

fun main(args: Array<String>) {
  val document: Document = Jsoup.connect("https://sunnah.com/bukhari/1/1").get()

  val englishChapterNumber = document.getElementsByClass("echapno").text()
  val englishChapterText = document.getElementsByClass("englishchapter").text()
  val arabicChapterNumber = document.getElementsByClass("achapno").text()
  val arabicChapterText = document.getElementsByClass("arabicchapter arabic").text()
  val arabicChapterIntro = document.getElementsByClass("arabic achapintro").text()
  val narrator = document.getElementsByClass("hadith_narrated").tagName("a")[0].text().substringAfter("Narrated ").substringBefore(":")
  var arabicContent = ""
  document.getElementsByClass("arabic_hadith_full arabic").tagName("span").forEach {
    arabicContent += it.text()
  }
  val englishContent = document.getElementsByClass("text_details").tagName("p")[0].text()
  val hadithReferences = document.getElementsByClass("hadith_reference").tagName("tbody")[0].getElementsByTag("tr")
  val reference = hadithReferences[0].text()
  val inBookReference = hadithReferences[1].text()
  val uscMsaWebReference = hadithReferences[2].text()


  println("($englishChapterNumber) $englishChapterText")
  println("($arabicChapterNumber) $arabicChapterText")
  println(arabicChapterIntro)
  println(narrator)
  println(arabicContent)
  println(englishContent)
  println("Reference : $reference")
  println("In-book reference : $inBookReference")
  println("USC_MSA web reference : $uscMsaWebReference")


}
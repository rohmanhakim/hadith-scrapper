import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.FileInputStream
import java.util.*

const val projectId = "hikma-api"

fun main(args: Array<String>) {

  val compilers = listOf(
          "bukhari" to "Sahih Al-Bukhari",
          "muslim" to "Sahih Muslim",
          "nasai" to "Sunan An-Nasai",
          "abudawud" to "Sunan Abi Dawud",
          "tirmidhi" to "Jami` at-Tirmidhi ",
          "ibnmajah" to "Sunan ibn Majah",
          "malik" to "Muwatta Malik",
          "nawawi40" to "Nawawi",
          "riyadussaliheen" to "Riyad As-Salihin",
          "adab" to "al-Adab Al-Mufrad",
          "qudsi40" to "Qudsi",
          "shamail" to "Shama'il Muhammadiyah",
          "bulugh" to "Bulugh Al-Maram")


  val serviceAccount = FileInputStream("src/main/resources/private/serviceAccount.json")

  val credentials = GoogleCredentials.fromStream(serviceAccount)
  val options = FirebaseOptions.Builder()
          .setCredentials(credentials)
          .setProjectId(projectId)
          .build()

  FirebaseApp.initializeApp(options)

  val db = FirestoreClient.getFirestore()

  val hadith = scrapHadith(compilers[0].second, "https://sunnah.com/${compilers[0].first}/1/1")

  saveToFirestore(hadith, db)
}

private fun saveToFirestore(hadith: Hadith, db: Firestore) {
  val docData = HashMap<String, Any>()

  docData["compiler"] = hadith.compiler
  docData["englishChapterNumber"] = hadith.englishChapterNumber
  docData["englishChapterText"] = hadith.englishChapterText
  docData["arabicChapterNumber"] = hadith.arabicChapterNumber
  docData["arabicChapterText"] = hadith.arabicChapterText
  docData["arabicChapterIntro"] = hadith.arabicChapterIntro
  docData["narrator"] = hadith.narrator
  docData["arabicContent"] = hadith.arabicContent
  docData["englishContent"] = hadith.englishContent
  docData["reference"] = hadith.reference
  docData["inBookReference"] = hadith.inBookReference
  docData["uscMsaWebReference"] = hadith.uscMsaWebReference
  docData["englishContentTextCount"] = hadith.englishContent.length
  docData["favorite"] = 0
  docData["included"] = true

  val future = db.collection("hadith").document().set(docData)

  println("Update time : " + future.get().updateTime)
}

private fun scrapHadith(compiler: String, url: String): Hadith {
  val document: Document = Jsoup.connect(url).get()

  val bookNumber = document.getElementsByClass("book_page_number").text().toInt()
  val bookEnglishName = document.getElementsByClass("book_page_english_name").text()
  val bookArabicName = document.getElementsByClass("book_page_arabic_name arabic").text()
  val englishChapterNumber = document.getElementsByClass("echapno").text().substringAfter("(").substringBefore(")").toInt()
  val englishChapterText = document.getElementsByClass("englishchapter").text()
  val arabicChapterNumber = document.getElementsByClass("achapno").text().substringAfter("(").substringBefore(")").toInt()
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

  return Hadith(
          compiler,
          bookNumber,
          bookEnglishName,
          bookArabicName,
          englishChapterNumber,
          englishChapterText,
          arabicChapterNumber,
          arabicChapterText,
          arabicChapterIntro,
          narrator,
          arabicContent,
          englishContent,
          reference,
          inBookReference,
          uscMsaWebReference
  )
}

data class Hadith(
        val compiler: String,
        val bookNumber: Int,
        val bookEnglishName: String,
        val bookArabicName: String,
        val englishChapterNumber: Int,
        val englishChapterText: String,
        val arabicChapterNumber: Int,
        val arabicChapterText: String,
        val arabicChapterIntro: String,
        val narrator: String,
        val arabicContent: String,
        val englishContent: String,
        val reference: String,
        val inBookReference: String,
        val uscMsaWebReference: String
)
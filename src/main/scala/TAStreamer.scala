import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.sql.SQLContext
import org.apache.spark.streaming.dstream.DStream

object TAStreamer extends App{
  override def main(args: Array[String]): Unit = {
    System.setProperty("twitter4j.oauth.consumerKey", "PbHuTRuSqSHTUzAJGJuqZAZoz")
    System.setProperty("twitter4j.oauth.consumerSecret", "BjpUlQLT7lVuXR7kI0PJCYS77b6hFDuQKYLLex7ETfKRYDiLpX")
    System.setProperty("twitter4j.oauth.accessToken", "319641898-eSXscJnl6aTyExpEZUCVi6RyQ8JzRPIQ4R9qFDA5")
    System.setProperty("twitter4j.oauth.accessTokenSecret", "1AYcrO4wodtFxVZKWv5UB5v7eKgZq21wJ8xaTRYkvhCKU")

    val ssc = new StreamingContext("local[2]", "PrintTweets", Seconds(2))

    val sqlContext = new SQLContext(ssc.sparkContext)
    import sqlContext.implicits._

    val filters = Array("#Tesla", "#Microsoft", "#Apple", "#Total", "#EHang", "#Renault", "#Sanofi")
    val tweets = TwitterUtils.createStream(ssc, None, filters)
    val englishTweets = tweets.filter(x => x.getLang() == "en")

//    val statuses = englishTweets.map(status => (status.getText() ,status.getUser.getScreenName(), status.getLang(), status.getUser.getName(), status.getGeoLocation(),status.getCreatedAt.toString))
    val data: DStream[(Long, String, String, String, Double, Double, String, Int, Int, Int, Int)] = englishTweets.map { status =>
      val id = status.getUser.getId
      val date = status.getUser.getCreatedAt.toString()
      val placeLal = Option(status.getGeoLocation).map{ _.getLatitude}.getOrElse(0.0)
      val placeLon = Option(status.getGeoLocation).map{ _.getLongitude}.getOrElse(0.0)
      val country =  Option(status.getPlace).map{ _.getCountry}.getOrElse("")
      val user = status.getUser.getName()
      val text = status.getText

      val TWITTER = "EEE MMM dd HH:mm:ss ZZZZ yyyy"
      import java.text.SimpleDateFormat
      import java.util.Calendar
      import java.util.Locale

      val sf = new SimpleDateFormat(TWITTER, Locale.ENGLISH)
      val d = sf.parse(date)
      val cal = Calendar.getInstance
      cal.setTime(d)
      val year = cal.get(Calendar.YEAR)
      val month = cal.get(Calendar.MONTH) + 1
      val day = cal.get(Calendar.DAY_OF_MONTH)
      val hour = cal.get(Calendar.HOUR_OF_DAY + 1 )
      (id,date,user,country,placeLal,placeLon,text, year, month, day, hour)
    }
    data.foreachRDD{rdd =>
      if(!rdd.isEmpty){
        val df = rdd.toDF("id","date","user","country","placeLal", "placeLon","text","year", "month", "day", "hour")
        df.show()
        df.write.mode("append").partitionBy("year", "month", "day", "hour").format("parquet").save("input/tweets/parquet")
      }
    }

    ssc.start()
    ssc.awaitTermination()

  }
}

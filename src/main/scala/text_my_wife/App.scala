package text_my_wife

import scopt.OParser
import scalaj.http.{Http, HttpResponse}
import play.api.libs.json._

import com.twilio.Twilio
import com.twilio.rest.api.v2010.account.Message
import com.twilio.`type`.PhoneNumber

import scala.util.{Failure, Success, Try}
import grizzled.slf4j.Logging
import com.jcabi.aspects.RetryOnFailure
import java.util.concurrent.TimeUnit

import java.net.URI
import java.util.Arrays

case class BadRequestException(msg:String) extends Exception(msg)
case class Quote(quote: String, author: Option[String], likes: Int, tags: Array[String], language: String)

object App extends Logging {

  @RetryOnFailure(attempts = 3, delay = 5, unit = TimeUnit.SECONDS)
  def get(url: String, apiKey: String): Either[String,Quote] = {
    val response: HttpResponse[String] = Http(url)
      .header("Content-Type", "application/json")
      .header("Accept", "application/json")
      .header("Authorization", s"Token $apiKey")
      .method("GET")
      .param("language", "en")
      .timeout(connTimeoutMs = 2000, readTimeoutMs = 5000)
      .asString

    implicit val reads = Json.reads[Quote]
    val parse = (x: String) => Json.fromJson[Quote](Json.parse(x).as[JsValue])

    if(response.code == 429){
      throw BadRequestException(response.throwError.body)
    }

    if(response.isSuccess){
       parse(response.body) match {
         case JsSuccess(obj: Quote, _: JsPath) => Right(obj)
         case e @ JsError(_) => Left(JsError.toJson(e).toString())
       }
    } else {
      Left(response.throwError.body)
    }
  }

  def main(args: Array[String]): Unit = {

    val builder = OParser.builder[Config]
    val parser1 = {
      import builder._
      OParser.sequence(
        programName("Text My Wife"),
        head("scopt", "4.x"),
        opt[String]("twilioAccountSID")
          .required()
          .action((x, c) => c.copy(twilioAccountSID = x))
          .text("Twilio Account SID"),
        opt[String]("twilioAuthToken")
          .required()
          .action((x, c) => c.copy(twilioAuthToken = x))
          .text("Twilio Auth Token"),
        opt[String]("paperQuotesAPIKey")
          .required()
          .action((x, c) => c.copy(paperQuotesAPIKey = x))
          .text("Paper Quotes Key"),
        opt[String]("fromNum")
          .required()
          .action((x, c) => c.copy(fromNum = x))
          .text("Call From Number"),
        opt[String]("toNum")
          .required()
          .action((x, c) => c.copy(toNum = x))
          .text("Call To Number")
      )
    }

    val config = OParser.parse(parser1, args, Config()) match {
      case Some(config) => config
      case _ => throw new IllegalArgumentException("Arguments Are Bad, See Errors Above..")
    }

    val request: Quote = get("https://api.paperquotes.com/apiv1/qod/", config.paperQuotesAPIKey) match {
      case Right(r) => r
      case Left(e) => throw BadRequestException(e)
    }

    val messageHeader = "Hi BooBoo, this is Husband and I Love You <3 \ud83d\udc36"
    val attribution = "Powered by quotes from paperquotes.com"

    val content = messageHeader + "\n\n" + s""""${request.quote}"""" + s" -${request.author.getOrElse("Unknown")}" + "\n\n" + attribution

    Twilio.init(config.twilioAccountSID, config.twilioAuthToken)

    val from = new PhoneNumber(config.fromNum)
    val to = new PhoneNumber(config.toNum)

    Try(Message.creator(to, from, content).setMediaUrl(
      Arrays.asList(URI.create("https://cdn.pixabay.com/photo/2016/11/19/12/25/art-1839006_1280.jpg"))
    ).create()) match {
      case Success(message) =>
        info(s"Message sent to $to with ID ${message.getSid}")
      case Failure(e) =>
        throw new RuntimeException(s"Encountered an exception: n${e.getMessage}")
    }

  }
}

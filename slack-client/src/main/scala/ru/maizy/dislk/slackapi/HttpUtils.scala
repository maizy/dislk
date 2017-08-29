package ru.maizy.dislk.slackapi

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import scala.collection.JavaConverters._
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Success, Try }
import dispatch.{ Http, Req, as, url }
import ru.maizy.dislk.slackapi.data.ErrorDto

case class RawHttpResponse(status: Int, body: Array[Byte], headers: Map[String, IndexedSeq[String]]) {
  def bodyAsString: String = new String(body)
}

object HttpUtils {
  final val HTTP_OK = 200
}

trait HttpUtils extends ParseUtils {
  import HttpUtils._

  protected val httpClient: Http = Http.default
  protected def config: Config
  protected def context: ExecutionContext

  protected def requestWithToken(
      endpoint: String,
      additionalParams: Seq[(String, String)] = Seq.empty,
      httpMethod: String = "GET"): Future[RawHttpResponse] =
  {
    implicit val ec = context
    val inBody = !Seq("GET", "HEAD").contains(httpMethod)

    val reqWithMethod = url(config.BASE_URL + s"/$endpoint").setMethod(httpMethod)

    val params = ("token", config.personalToken) +: additionalParams
    val requestWithParams: Req = if(inBody) {
      params.foldLeft(reqWithMethod) { case (req, (key, value)) =>
        req.addParameter(key, value)
      }
    } else {
      params.foldLeft(reqWithMethod) { case (req, (key, value)) =>
        req.addQueryParameter(key, value)
      }
    }

    httpClient(requestWithParams > as.Response { response =>
      val headers = response
        .getHeaders
        .entries.asScala
        .groupBy(_.getValue)
        .mapValues{entries => entries.map(_.getValue).toIndexedSeq}
      RawHttpResponse(response.getStatusCode, response.getResponseBodyAsBytes, headers)
    })
  }

  protected implicit class Check(f: Future[RawHttpResponse])(implicit ex: ExecutionContext) {
    def checkResponse(expectedCodes: Seq[Int] = Seq(HTTP_OK)): Future[Either[ClientError, RawHttpResponse]] = {
      f.map { rawResponse =>
        if (!expectedCodes.contains(rawResponse.status)) {
          Left(ClientError(s"Bad response status ${rawResponse.status}, expected ${expectedCodes mkString ","}"))
        } else {
          Try(ErrorDto.parseJson(rawResponse.body)) match {
            case Success(error) if !error.ok => Left(ClientError(s"Slack responses with error '${error.errorCode}'"))
            case _ => Right(rawResponse)
          }
        }
      }
    }

    def checkResponseIsSuccessful(expectedCodes: Seq[Int] = Seq(HTTP_OK)): Future[Unit] = {
      f.checkResponse(expectedCodes) map {
        case Left(error) => throw error
        case Right(_) => ()
      }
    }
  }

  protected implicit class CheckAndParse(f: Future[RawHttpResponse])(implicit ex: ExecutionContext) {
    def checkAndParse[T](parse: RawHttpResponse => T, expectedCodes: Seq[Int] = Seq(HTTP_OK)): Future[T] = {
      f.checkResponse(expectedCodes).map {
        case Left(error) => throw error
        case Right(response) =>
          wrapParseException{
            parse(response)
          }
      }
    }
  }

}

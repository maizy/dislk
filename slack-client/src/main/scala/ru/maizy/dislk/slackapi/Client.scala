package ru.maizy.dislk.slackapi

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import scala.concurrent.{ ExecutionContext, Future }
import ru.maizy.dislk.slackapi.data.{ DndInfo, Profile }

case class ClientError(message: String, cause: Option[Throwable] = None) extends Exception(message, cause.orNull)

class Client private (val config: Config)(implicit ec: ExecutionContext)
  extends HttpUtils
{

  override protected def context: ExecutionContext = ec

  def dndInfo(): Future[DndInfo] = {
    requestWithToken("dnd.info").checkAndParse { response =>
      DndInfo.parseJson(response.body)
    }
  }

  def setStatus(text: String, emoji: String): Future[Unit] = {
    val profileParam = Profile(text, emoji).toJson
    requestWithToken("users.profile.set", Seq("profile" -> profileParam), httpMethod = "POST")
      .checkResponseIsSuccessful()
  }
}

object Client {
  def withConfig(config: Config)(implicit ec: ExecutionContext): Client = new Client(config)
}

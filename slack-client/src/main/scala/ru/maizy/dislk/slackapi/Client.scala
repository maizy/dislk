package ru.maizy.dislk.slackapi

/**
 * Copyright (c) Nikita Kovaliov, maizy.ru, 2017
 * See LICENSE.txt for details.
 */

import scala.concurrent.{ ExecutionContext, Future }
import ru.maizy.dislk.slackapi.data.DndInfo

case class ClientError(message: String, cause: Option[Throwable] = None) extends Exception(message, cause.orNull)

class Client private (val config: Config)(implicit ec: ExecutionContext)
  extends HttpUtils
  with ParseUtils
{

  override protected def context: ExecutionContext = ec

  def dndInfo(): Future[DndInfo] = {
    requestWithToken("dnd.info").map { checkResponse(_) match {
      case Left(error) => throw error
      case Right(response) =>
        wrapParseException{
          DndInfo.parse(response.body)
        }
    }}
  }
}

object Client {
  def withConfig(config: Config)(implicit ec: ExecutionContext): Client = new Client(config)
}

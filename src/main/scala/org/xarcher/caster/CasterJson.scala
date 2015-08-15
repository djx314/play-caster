package org.xarcher.caster

import cats.data.Xor
import play.api._
import play.api.http._
import play.api.mvc._
import play.api.libs.iteratee._
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import io.circe._, io.circe.generic.auto._, io.circe.jawn._, io.circe.syntax._

trait CasterJson {

  /**
   * This method is copied from Play 2.2
   */
  protected def DEFAULT_MAX_TEXT_LENGTH: Int = Play.maybeApplication.flatMap { app =>
    app.configuration.getBytes("parsers.text.maxLength").map(_.toInt)
  }.getOrElse(1024 * 100)

  final type ParseErrorHandler = (RequestHeader, Array[Byte], Throwable) => Future[Result]

  protected def defaultParseErrorMessage = "Invalid Json"
  protected def defaultParseErrorHandler: ParseErrorHandler = {
    (header, _, _) => createBadResult(defaultParseErrorMessage)(header)
  }

  protected def defaultTolerantBodyParser[A](name: String, maxLength: Int)(parser: (RequestHeader, Array[Byte]) => Xor[ParsingFailure, Json]): BodyParser[Json] =
    tolerantBodyParser(name, maxLength)(parser)(defaultParseErrorHandler)

  protected def tolerantBodyParser(name: String, maxLength: Int)(defaultParser: (RequestHeader, Array[Byte]) => Xor[ParsingFailure, Json])(parseErrorHandler: ParseErrorHandler): BodyParser[Json] =
    BodyParser(name + ", maxLength=" + maxLength) {
      request =>

        val bodyParser: Iteratee[Array[Byte], Either[Result, Xor[Future[Result], Json]]] =
          Traversable.takeUpTo[Array[Byte]](maxLength).transform(
            Iteratee.consume[Array[Byte]]().map {
              bytes =>
                defaultParser(request, bytes).leftMap {
                  e => parseErrorHandler(request, bytes, e)
                }
            }
          ).flatMap(Iteratee.eofOrElse(Results.EntityTooLarge))

        bodyParser.mapM {
          case Left(tooLarge) => Future.successful(Left(tooLarge))
          case Right(Xor.Left(badResult)) => badResult.map(Left.apply)
          case Right(Xor.Right(body)) => Future.successful(Right(body))
        }
    }

  protected val defaultParser: (RequestHeader, Array[Byte]) => Xor[ParsingFailure, Json] = {
    (request, bytes) =>
      parse(new String(bytes, request.charset.getOrElse("utf-8")))
  }

  protected def createBadResult(msg: String): RequestHeader => Future[Result] = {
    request =>
      Play.maybeApplication.map(_.global.onBadRequest(request, "Expecting json body"))
        .getOrElse(Future.successful(Results.BadRequest))
  }

  /**
   * Parse the body as Json without checking the Content-Type.
   */
  def tolerantJson: BodyParser[Json] = tolerantJson(DEFAULT_MAX_TEXT_LENGTH)

  def tolerantJson(maxLength: Int): BodyParser[Json] =
    defaultTolerantBodyParser[Json]("json", maxLength)(defaultParser)

  def tolerantJsonWithErrorHandler(maxLength: Int, errorHandler: ParseErrorHandler): BodyParser[Json] =
    tolerantBodyParser("json", maxLength)(defaultParser)(errorHandler)

  def json: BodyParser[Json] = json(BodyParsers.parse.DefaultMaxTextLength)

  def json(maxLength: Int): BodyParser[Json] =
    jsonWithErrorHandler(maxLength)(defaultParseErrorHandler)

  def jsonWithErrorHandler(maxLength: Int)(errorHandler: ParseErrorHandler): BodyParser[Json] = BodyParsers.parse.when(
    _.contentType.exists(m => m.equalsIgnoreCase("text/json") || m.equalsIgnoreCase("application/json")),
    tolerantJsonWithErrorHandler(maxLength, errorHandler),
    createBadResult("Expecting text/json or application/json body")
  )

}
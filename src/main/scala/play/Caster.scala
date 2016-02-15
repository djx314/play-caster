package play.api.libs.caster

import akka.stream.scaladsl.{Flow, Sink}
import akka.util.ByteString
import io.circe._
import io.circe.parser
import cats.data.Xor
import play.api.http._
import play.api.http.Status._
import play.api.libs.iteratee.Execution.Implicits.trampoline
import play.api.Logger
import play.api.libs.streams.Accumulator
import play.api.mvc._
import scala.concurrent.Future
import scala.util.control.NonFatal

trait Caster {

  implicit def contentTypeOf_Json(implicit codec: Codec): ContentTypeOf[Json] = {
    ContentTypeOf(Some(ContentTypes.JSON))
  }

  implicit def writableOf_Json(implicit codec: Codec): Writeable[Json] = {
    Writeable(a => codec.encode(a.noSpaces))
  }

  object caster {

    import BodyParsers._

    @inline def DefaultMaxTextLength: Int = parse.DefaultMaxTextLength

    val logger = Logger(BodyParsers.getClass)

    def json[T: Decoder]: BodyParser[T] =/*json.mapM { json =>
      implicitly[Decoder[T]].decodeJson(json) match {
        case Xor.Left(e) => Future.failed(e)
        case Xor.Right(t) => Future.successful(t)
      }
    }*/
      BodyParser("json reader") { request =>
        import play.api.libs.iteratee.Execution.Implicits.trampoline
        json(request) mapFuture {
          case Left(simpleResult) =>
            Future.successful(Left(simpleResult))
          case Right(jsValue) =>
            implicitly[Decoder[T]].decodeJson(jsValue) match {
              case Xor.Left(e) => createBadResult(e.getMessage)(request) map Left.apply
              case Xor.Right(t) => Future.successful(Right(t))
            }
          /*map { a =>
              Future.successful(Right(a))
            } recoverTotal { jsError =>
              val msg = s"Json validation error ${JsError.toFlatForm(jsError)}"
              createBadResult(msg)(request) map Left.apply
            }*/
        }
      }

    def json: BodyParser[Json] = json(DefaultMaxTextLength)

    def json(maxLength: Int): BodyParser[Json] = parse.when(
      _.contentType.exists(m => m.equalsIgnoreCase("text/json") || m.equalsIgnoreCase("application/json")),
      tolerantJson(maxLength),
      createBadResult("Expecting text/json or application/json body", UNSUPPORTED_MEDIA_TYPE)
    )

    def tolerantJson[T: Decoder]: BodyParser[T] =
      BodyParser("json reader") { request =>
        import play.api.libs.iteratee.Execution.Implicits.trampoline
        tolerantJson(request) mapFuture {
          case Left(simpleResult) =>
            Future.successful(Left(simpleResult))
          case Right(jsValue) =>
            implicitly[Decoder[T]].decodeJson(jsValue) match {
              case Xor.Left(e) => createBadResult(e.getMessage)(request) map Left.apply
              case Xor.Right(t) => Future.successful(Right(t))
            }
        }
      }

    def tolerantJson: BodyParser[Json] = tolerantJson(DefaultMaxTextLength)

    def tolerantJson(maxLength: Int): BodyParser[Json] = {
      tolerantBodyParser[Json]("json", maxLength, "Invalid Json") { (request, bytes) =>
        parser.parse(bytes.decodeString(request.charset.getOrElse("utf-8"))).toEither
      }
    }

    private def createBadResult(msg: String, statusCode: Int = BAD_REQUEST): RequestHeader => Future[Result] = { request =>
      LazyHttpErrorHandler.onClientError(request, statusCode, msg)
    }

    /**
      * Enforce the max length on the stream consumed by the given accumulator.
      */
    private def enforceMaxLength[A](request: RequestHeader, maxLength: Long, accumulator: Accumulator[ByteString, Either[Result, A]]): Accumulator[ByteString, Either[Result, A]] = {
      val takeUpToFlow = Flow[ByteString].transform { () => new BodyParsers.TakeUpTo(maxLength) }
      import play.api.libs.concurrent.Execution.Implicits.defaultContext
      accumulator.through(takeUpToFlow).recoverWith {
        case _: BodyParsers.MaxLengthLimitAttained =>
          val badResult = createBadResult("Request Entity Too Large", REQUEST_ENTITY_TOO_LARGE)(request)
          badResult.map(Left(_))
      }
    }

    /**
      * Create a body parser that uses the given parser and enforces the given max length.
      *
      * @param name The name of the body parser.
      * @param maxLength The maximum length of the body to buffer.
      * @param errorMessage The error message to prepend to the exception message if an error was encountered.
      * @param parser The parser.
      */
    private def tolerantBodyParser[A](name: String, maxLength: Long, errorMessage: String)(parser: (RequestHeader, ByteString) => Either[Error, A]): BodyParser[A] =
      BodyParser(name + ", maxLength=" + maxLength) { request =>
        import play.api.libs.iteratee.Execution.Implicits.trampoline
        enforceMaxLength(request, maxLength, Accumulator(
          Sink.fold[ByteString, ByteString](ByteString.empty)((state, bs) => state ++ bs)
        ) mapFuture { bytes =>
          parser(request, bytes) match {
            case Left(e) =>
              logger.info("22" * 100)
              logger.debug(errorMessage, e)
              logger.info("11" * 100)
              createBadResult(errorMessage + ": " + e.getMessage)(request).map(Left(_))
            case Right(body) => Future successful Right(body)
          }
        })
      }

  }

}

object Caster extends Caster
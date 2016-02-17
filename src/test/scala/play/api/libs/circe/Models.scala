package play.api.libs.caster

case class Bar(bar: Int)
case class Foo(foo: String, bar: Bar)

import play.api.test._
import play.api.test.Helpers._
import play.core.server.akkahttp.AkkaHttpServer

object Conf {

  val bar = Bar(1)
  val foo = Foo("foo", bar)
  val port = 12345

  implicit val app = FakeApplication(
    withRoutes = {
      case ("GET", "/get") => CirceController.get
      case ("POST", "/post") => CirceController.post
      case ("POST", "/post-json") => CirceController.postJson
      case ("POST", "/post-tolerant") => CirceController.postTolerate
      case ("POST", "/post-tolerant-json") => CirceController.postTolerateJson
    }
  )
  val server = new TestServer(port = port, application = app, serverProvider = Some(AkkaHttpServer.provider))

}
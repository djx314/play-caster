package play.api.libs.caster

import io.circe.generic.auto._
import io.circe.syntax._
import play.api._
import play.api.mvc._

object CirceController extends Controller with Caster {

  def get = Action {
    Ok(Conf.foo.asJson)
  }

  def post = Action(caster.json[Foo]) { implicit request =>
    val isEqual = request.body == Conf.foo
    Ok(isEqual.toString)
  }

  def postJson = Action(caster.json) { implicit request =>
    val isEqual = request.body == Conf.foo.asJson
    Ok(isEqual.toString)
  }

  def postTolerate = Action(caster.tolerantJson[Foo]) { implicit request =>
    val isEqual = request.body == Conf.foo
    Ok(isEqual.toString)
  }

  def postTolerateJson = Action(caster.tolerantJson) { implicit request =>
    val isEqual = request.body == Conf.foo.asJson
    Ok(isEqual.toString)
  }
}

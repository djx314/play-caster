# play-caster -- play-jfc extension

To use jfc with play, just import jfc dependencies and copy the code in this repository. Then
```scala
import org.xarcher.caster._
//import play import here

case class Price(id: Option[Long], value: Double)
Action.async { implicit request =>
  Future successful Ok(Price(Option(2L, 15.22)).asJson)
}

Action.async(JfcParser.json) { implicit request =>
  val priceOpt = request.body.as[Price].toOption
  ...
}
```

The code is base on play-json4s <https://github.com/tototoshi/play-json4s>. Thanks to @tototoshi .
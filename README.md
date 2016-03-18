# A [playframework](https://playframework.com/) 2.5.0 extension for [circe](http://circe.io)
[![Build Status](https://travis-ci.org/scalax/play-caster.svg?branch=master)](https://travis-ci.org/scalax/play-caster)
[![codecov.io](https://codecov.io/github/scalax/play-caster/coverage.svg?branch=master)](https://codecov.io/github/scalax/play-caster?branch=master)

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

package org.xarcher

import io.circe.Json
import play.api.http.{ContentTypes, ContentTypeOf, Writeable}
import play.api.mvc.Codec
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
 * Created by djx314 on 15-8-15.
 */
package object caster {

  implicit def writeableOf_NativeJValue(implicit codec: Codec): Writeable[Json] =
    Writeable((jval: Json) => codec.encode(jval.noSpaces))

  implicit def contentTypeOf_JsValue(implicit codec: Codec): ContentTypeOf[Json] =
    ContentTypeOf(Some(ContentTypes.JSON))

  object JfcParser extends CasterJson

}
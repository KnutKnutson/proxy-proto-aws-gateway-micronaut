package micronaut.proxy.proto

import com.google.protobuf.StringValue
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.protobuf.codec.ProtobufferCodec
import javax.annotation.security.PermitAll

@Controller("/api")
@PermitAll
class ApiController {

  @Post(value = "/json")
  fun json(@Body json: SomeString): HttpResponse<*> {
    println("Received string value $json")
    return HttpResponse.ok<String>()
  }

  @Post(value = "/proto", consumes = [ProtobufferCodec.PROTOBUFFER_ENCODED])
  fun proto(@Body aProto: StringValue): HttpResponse<*> {
    println("Received string value $aProto")
    return HttpResponse.ok<String>()
  }

  data class SomeString(val value: String)
}
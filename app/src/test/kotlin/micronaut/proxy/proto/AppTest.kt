package micronaut.proxy.proto

import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.protobuf.StringValue
import io.micronaut.context.ApplicationContext
import io.micronaut.function.aws.proxy.MicronautLambdaContainerHandler
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.protobuf.codec.ProtobufferCodec
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.util.*
import javax.inject.Inject

@MicronautTest
class AppTest {

    private val mapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    @Inject
    lateinit var context: ApplicationContext

    @Inject
    @field:Client(value = "/")
    lateinit var client: HttpClient

    @Test
    fun passes() {
        val body: Any = StringValue.of("a value")

        val response = client.toBlocking().exchange<Any, Any>(
            HttpRequest.POST("/api/proto", body).contentType(ProtobufferCodec.PROTOBUFFER_ENCODED)
        )

        assertEquals(200, response.status.code)
    }

    @Test
    fun alsoPasses() {
        val body = """{"value": "some string"}"""
        val request = proxyRequest(
            path = "api/json", body = body, isBase64Encoded = false,
            contentType = MediaType.APPLICATION_JSON
        )

        val response = handleRequest(request)

        assertEquals(200, response.statusCode)
    }

    @Test
    fun fails() {
        val body = Base64.getEncoder().encodeToString(StringValue.of("a value").toByteArray())
        val request = proxyRequest(
            path = "api/proto", body = body, isBase64Encoded = true,
            contentType = ProtobufferCodec.PROTOBUFFER_ENCODED
        )

        val response = handleRequest(request)

        assertEquals(200, response.statusCode)
    }

    @Test
    fun workaround() {
        val body = Base64.getEncoder().encodeToString(StringValue.of("a value").toByteArray())
        val request = proxyRequest(
            path = "api/workaround", body = body, isBase64Encoded = true,
            contentType = ProtobufferCodec.PROTOBUFFER_ENCODED
        )

        val response = handleRequest(request)

        assertEquals(200, response.statusCode)
    }

    private fun handleRequest(request: APIGatewayProxyRequestEvent): LambdaResponse {
        val requestString = mapper.writeValueAsString(request)
        val inputStream = requestString.byteInputStream(Charsets.UTF_8)
        val outputStream = ByteArrayOutputStream()
        ApiLambdaHandler(MicronautLambdaContainerHandler(context)).handleRequest(inputStream, outputStream, MockLambdaContext())
        return mapper.readValue(outputStream.toString(Charsets.UTF_8))
    }

    private fun proxyRequest(path: String,
                             body: String,
                             contentType: String,
                             isBase64Encoded: Boolean
    ): APIGatewayProxyRequestEvent {
        return APIGatewayProxyRequestEvent()
            .withHeaders(mapOf(
                "content-type" to contentType
            ))
            .withMultiValueHeaders(mapOf(
                "content-type" to listOf(contentType)
            ))
            .withResource("/{proxy+}")
            .withHttpMethod("POST")
            .withPath("/my/$path")
            .withPathParameters(mapOf(
                "proxy" to path
            ))
            .withBody(body)
            .withIsBase64Encoded(isBase64Encoded)
            .withRequestContext(
                APIGatewayProxyRequestEvent.ProxyRequestContext()
                    .withResourceId("resourceId")
                    .withResourcePath("/{proxy+}")
                    .withHttpMethod("POST")
                    .withPath("/my/$path")
                    .withIdentity(
                        APIGatewayProxyRequestEvent.RequestIdentity()
                            .withSourceIp("127.0. 0.1")
                            .withUserAgent("Amazon CloudFront")
                    )
            )
    }

    data class LambdaResponse(
        val statusCode: Int,
        val body: String?
    )
}

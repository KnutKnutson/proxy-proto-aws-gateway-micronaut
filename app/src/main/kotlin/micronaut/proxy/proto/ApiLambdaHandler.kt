package micronaut.proxy.proto

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import io.micronaut.function.aws.proxy.MicronautLambdaContainerHandler
import java.io.InputStream
import java.io.OutputStream

class ApiLambdaHandler(private val handler: MicronautLambdaContainerHandler = cachedHandler): RequestStreamHandler {

  companion object {
    private val cachedHandler: MicronautLambdaContainerHandler by lazy { MicronautLambdaContainerHandler() }
  }

  override fun handleRequest(input: InputStream, output: OutputStream, context: Context) {
    handler.proxyStream(input, output, context)
  }
}
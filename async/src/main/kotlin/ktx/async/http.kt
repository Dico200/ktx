package ktx.async

import com.badlogic.gdx.Net.HttpRequest
import com.badlogic.gdx.Net.HttpResponse
import java.io.ByteArrayInputStream
import java.nio.charset.Charset

/** Thrown when unable to finish HTTP request. */
class HttpResponseException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * Stores result of a [HttpRequest]. A safer alternative to [HttpResponse].
 * @param url URL of the queried resource.
 * @param method HTTP method of the request.
 * @param statusCode HTTP status code of the response. Might be set to -1 by internal LibGDX implementation or if the
 *    status could not be determined.
 * @param content response body stored as raw bytes.
 * @param headers HTTP header values of the response. Might be empty.
 */
class HttpRequestResult(
    val url: String,
    val method: String,
    val statusCode: Int,
    val content: ByteArray,
    val headers: Map<String, List<String>>
) {
  /** Returns cached representation of the response stored as a string with default encoding.*/
  val contentAsString by lazy { getContentAsString() }
  /** Returns a new instance of [ByteArrayInputStream] with raw response bytes each time the getter is invoked. */
  val contentAsStream get() = ByteArrayInputStream(content)

  /**
   * @param header name of the HTTP header.
   * @return values assigned to the header or empty list if header is not present.
   */
  fun getHeader(header: String) = headers[header] ?: emptyList()

  /**
   * @param charset character encoding. Defaults to UTF-8.
   * @return [content] converted to a string using the selected encoding.
   */
  fun getContentAsString(charset: Charset = Charsets.UTF_8) = String(content, charset)

  override fun toString() = "HttpRequestResult(url=$url, method=$method, status=$statusCode)"

  // Implementation note: LibGDX HttpRequestResult implementation can _quietly_ ignore closed input streams, which might
  // result in empty responses in multithreaded environments. We read the whole response into a byte array and return
  // this data object to avoid response content loss.
}

/**
 * Converts this non thread-safe [HttpResponse] to [HttpRequestResult] that reads and caches the HTTP result content
 * as byte array. Note that this method blocks the current thread until the HTTP result content is read.
 * @param requestData necessary to extract relevant data about the original request.
 * @return a new [HttpRequestResult] storing [HttpResponse] content.
 */
fun HttpResponse.toHttpRequestResult(requestData: HttpRequest) = HttpRequestResult(
    url = requestData.url,
    method = requestData.method,
    statusCode = this.status?.statusCode ?: -1, // -1 matches LibGDX default behaviour on unknown status.
    content = this.result ?: ByteArray(0),
    headers = this.headers ?: emptyMap()
)

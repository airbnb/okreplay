package okreplay

import okhttp3.MediaType
import okreplay.Util.CONTENT_TYPE
import okreplay.Util.isNullOrEmpty
import java.nio.charset.Charset

internal abstract class AbstractMessage : Message {

  override fun getContentType(): String {
    val header = header(CONTENT_TYPE)
    return if (isNullOrEmpty(header)) {
      DEFAULT_CONTENT_TYPE
    } else {
      MediaType.parse(header!!)!!.toString()
    }
  }

  override fun getCharset(): Charset {
    val header = header(CONTENT_TYPE)
    return if (isNullOrEmpty(header)) {
      // TODO: this isn't valid for non-text data – this method should return String?
      UTF_8
    } else {
      MediaType.parse(header!!)!!.charset() ?: UTF_8
    }
  }

  override fun getEncoding(): String {
    val header = header(CONTENT_ENCODING)
    return if (header == null || header.isEmpty()) DEFAULT_ENCODING else header
  }

  override fun header(name: String): String? {
    return headers().get(name)
  }

  override fun bodyAsText(): String {
    return String(body(), charset)
  }

  companion object {
    private val CONTENT_ENCODING = "Content-Encoding"
    private val UTF_8 = Charset.forName("UTF-8")
    @JvmField val DEFAULT_CONTENT_TYPE = "application/octet-stream"
    private val DEFAULT_ENCODING = "none"
  }
}

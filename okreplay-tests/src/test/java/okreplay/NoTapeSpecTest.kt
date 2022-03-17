package okreplay

import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test

// https://github.com/robfletcher/betamax/issues/18
class NoTapeSpecTest {

    companion object {
        private val configuration: OkReplayConfig = OkReplayConfig.Builder().build()
        private val interceptor = OkReplayInterceptor()
        private val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        private val proxy = ProxyServer(configuration, interceptor)

        @BeforeClass
        @JvmStatic
        fun setupSpec() {
            proxy.start(null)
        }

        @AfterClass
        @JvmStatic
        fun cleanup() {
            proxy.stop()
        }
    }

    @Test
    fun `an error is returned if the proxy intercepts a request when no tape is inserted`() {
        val request = Request.Builder()
            .url("http://localhost")
            .build()
        val response = client.newCall(request).execute()

        assertEquals(403, response.code)
        assertEquals("No tape", response.body!!.string())
    }
}


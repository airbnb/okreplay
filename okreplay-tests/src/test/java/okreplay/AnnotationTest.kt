package okreplay

import com.google.common.io.Files
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okreplay.Headers.VIA_HEADER
import okreplay.Headers.XHeader.HEADER_PLAY
import okreplay.Headers.XHeader.HEADER_REC
import org.codehaus.groovy.runtime.ResourceGroovyMethods
import org.junit.AfterClass
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection

@FixMethodOrder // The test methods are dependent on one another
class AnnotationTest {
    var configuration: OkReplayConfig = OkReplayConfig.Builder()
        .tapeRoot(tapeRoot)
        .defaultMode(TapeMode.READ_WRITE)
        .interceptor(OkReplayInterceptor())
        .build()

    @Rule
    @JvmField
    var recorder = RecorderRule(configuration)

    var client: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(configuration.interceptor())
        .build()

    @Test
    fun noTapeIsInsertedIfThereIsNoAnnotationOnTheTest() {
        Assert.assertNull(recorder.tape)
    }

    @Test
    @OkReplay(tape = "annotation_test", mode = TapeMode.READ_WRITE)
    fun annotationOnTestCausesTapeToBeInserted() {
        Assert.assertEquals("annotation_test", recorder.tape!!.name)
    }

    @Test
    fun tapeIsEjectedAfterAnnotatedTestCompletes() {
        Assert.assertNull(recorder.tape)
    }

    @Test
    @OkReplay(tape = "annotation_test", mode = TapeMode.READ_WRITE)
    @Throws(IOException::class)
    fun annotatedTestCanRecord() {
        endpoint.enqueue(MockResponse().setBody("Echo"))
        val request: Request = Request.Builder().url(endpoint.url("/")).build()
        val response = client.newCall(request).execute()
        Assert.assertEquals(HttpURLConnection.HTTP_OK.toLong(), response.code.toLong())
        Assert.assertEquals(VIA_HEADER, response.header(Util.VIA))
        Assert.assertEquals(HEADER_REC.headerName, response.header(Headers.X_OKREPLAY))
    }

    @Test
    @OkReplay(tape = "annotation_test", mode = TapeMode.READ_WRITE)
    @Throws(IOException::class)
    fun annotatedTestCanPlayBack() {
        endpoint.enqueue(MockResponse().setBody("Echo"))
        val request: Request = Request.Builder().url(endpoint.url("/")).build()
        val response = client.newCall(request).execute()
        Assert.assertEquals(HttpURLConnection.HTTP_OK.toLong(), response.code.toLong())
        Assert.assertEquals(VIA_HEADER, response.header(Util.VIA))
        Assert.assertEquals(HEADER_PLAY.headerName, response.header(Headers.X_OKREPLAY))
    }

    @Test
    @Throws(IOException::class)
    fun canMakeUnproxiedRequestAfterUsingAnnotation() {
        endpoint.enqueue(MockResponse().setBody("Echo"))
        val request: Request = Request.Builder().url(endpoint.url("/")).build()
        val response = client.newCall(request).execute()
        Assert.assertEquals(HttpURLConnection.HTTP_OK.toLong(), response.code.toLong())
        Assert.assertNull(response.header(Util.VIA))
        println("test")
    }

    companion object {
        private var tapeRoot: File = Files.createTempDir()
        private var endpoint = MockWebServer()

        @BeforeClass
        @Throws(IOException::class)
        @JvmStatic
        fun startServer() {
            // The endpoint needs to be shared between all tests otherwise it will be assigned a random
            // port for each test and the rule matching will fail on different URLs
            endpoint.start()
        }

        @AfterClass
        @JvmStatic
        fun cleanUpTapeFiles() {
            ResourceGroovyMethods.deleteDir(tapeRoot)
        }
    }
}

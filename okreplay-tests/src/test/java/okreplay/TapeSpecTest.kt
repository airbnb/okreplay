package okreplay

import com.google.common.io.Files
import com.google.common.net.HttpHeaders.CONTENT_ENCODING
import com.google.common.net.HttpHeaders.CONTENT_LANGUAGE
import com.google.common.net.HttpHeaders.CONTENT_TYPE
import com.google.common.net.MediaType.FORM_DATA
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okreplay.TapeMode.READ_ONLY
import okreplay.TapeMode.READ_WRITE
import okreplay.TapeMode.WRITE_ONLY
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class TapeSpecTest {

    private val tapeRoot = Files.createTempDir()
    private val loader: YamlTapeLoader = YamlTapeLoader(tapeRoot)
    private val tape: YamlTape = loader.loadTape("tape_spec").apply {
        mode = READ_WRITE
    }
    private val getRequest = RecordedRequest.Builder()
        .url("http://icanhascheezburger.com/")
        .build()
    private val plainTextResponse = RecordedResponse.Builder()
        .code(200)
        .addHeader(CONTENT_LANGUAGE, "en-GB")
        .addHeader(CONTENT_ENCODING, "gzip")
        .body(RESPONSE_BODY.toResponseBody("text/plain;charset=UTF-8".toMediaTypeOrNull()))
        .build()

    @After
    fun cleanup() {
        tape.mode = READ_WRITE
    }

    @Test
    fun `reading from an empty tape throws an exception`() {
        assertThrows(IllegalStateException::class.java) {
            tape.play(getRequest)
        }
    }

    @Test
    fun `can write an HTTP interaction to a tape`() {
        val originalTapeSize = tape.size()

        tape.record(getRequest, plainTextResponse)

        // the size of the tape increases
        assertEquals(originalTapeSize + 1, tape.size())
        val interaction = tape.interactions.last()

        // the request data is correctly stored
        assertEquals(interaction.request.method(), getRequest.method())
        assertEquals(interaction.request.uri(), getRequest.url().toUri())

        // the response data is correctly stored
        assertEquals(interaction.response.code(), plainTextResponse.code())
        assertEquals(interaction.response.body(), RESPONSE_BODY)
        assertEquals(interaction.response.header(CONTENT_TYPE), plainTextResponse.header(CONTENT_TYPE))
        assertEquals(interaction.response.header(CONTENT_LANGUAGE), plainTextResponse.header(CONTENT_LANGUAGE))
        assertEquals(interaction.response.header(CONTENT_ENCODING), plainTextResponse.header(CONTENT_ENCODING))
    }

    @Test
    fun `can overwrite a recorded interaction`() {
        // Seed the empty tape
        tape.record(getRequest, plainTextResponse)
        val originalTapeSize = tape.size()
        val originalDate = tape.interactions.last().recorded
        runBlocking {
            delay(1000) // The granularity of tape timestamps is one second
        }

        tape.record(getRequest, plainTextResponse)

        // the tape size does not increase
        assertEquals(originalTapeSize, tape.size())

        // the previous recording was overwritten
        assertTrue(tape.interactions.last().recorded > originalDate)
    }

    @Test
    fun `seek does not match a request for a different URI`() {
        val request = RecordedRequest.Builder()
            .url("http://qwantz.com/")
            .build()

        assertFalse(tape.seek(request))
    }

    @Test
    fun `can seek for a previously recorded interaction`() {
        // Seed the empty tape
        tape.record(getRequest, plainTextResponse)

        assertTrue(tape.seek(getRequest))
    }

    @Test
    fun `can read a stored interaction`() {
        // Seed the empty tape
        tape.record(getRequest, plainTextResponse)
        val response = tape.play(getRequest)

        // the recorded response data is copied onto the response
        assertEquals(response.code(), plainTextResponse.code())
        assertEquals(response.bodyAsText(), RESPONSE_BODY)
        assertEquals(response.headers().toMultimap(), plainTextResponse.headers().toMultimap())
    }

    @Test
    fun `can record post requests with a body`() {
        val request = RecordedRequest.Builder()
            .url("http://github.com/")
            .method("POST", "q=1".toRequestBody(FORM_DATA.toString().toMediaTypeOrNull()))
            .build()

        // the request and its response are recorded
        tape.record(request, plainTextResponse)

        // the request body is stored on the tape
        val interaction = tape.interactions.last()
        assertEquals(interaction.request.body(), request.bodyAsText())
    }

    @Test
    fun `a write-only tape cannot be read from`() {
        tape.mode = WRITE_ONLY

        val exception = assertThrows(IllegalStateException::class.java) {
            tape.play(getRequest)
        }

        assertEquals(exception.message, "the tape is not readable")
    }

    @Test
    fun `a read-only tape cannot be written to`() {
        tape.mode = READ_ONLY

        val exception = assertThrows(IllegalStateException::class.java) {
            tape.record(getRequest, plainTextResponse)
        }

        assertEquals(exception.message, "the tape is not writable")
    }

    companion object {
        private const val RESPONSE_BODY = "O HAI!"
    }
}

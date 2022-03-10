package okreplay

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.junit.Assert
import org.junit.Test
import java.io.File
import java.net.HttpURLConnection
import java.nio.file.Files

class ContentTypeSpecTest {

    @Test
    fun `can record post requests with an image content-type`() {
        val tapeRoot = Files.createTempDirectory("test").toFile()
        val loader = YamlTapeLoader(tapeRoot)
        val tape: Tape = loader.loadTape("tape_spec").apply {
            mode = TapeMode.READ_WRITE
        }
        val successResponse = RecordedResponse.Builder()
            .code(HttpURLConnection.HTTP_OK)
            .body(ResponseBody.create("text/plain".toMediaTypeOrNull(), "OK"))
            .build()
        val imagePostRequest = RecordedRequest.Builder()
            .method(
                "POST",
                RequestBody.create(
                    "image/png".toMediaTypeOrNull(),
                    File(javaClass.classLoader.getResource("image.png").toURI()).readBytes()
                )
            )
            .url("http://github.com/").build()
        tape.record(imagePostRequest, successResponse)
        val interactions = (tape as YamlTape).interactions
        val interaction = interactions[interactions.size - 1]
        Assert.assertEquals(interaction.request.body(), imagePostRequest.body())
    }
}

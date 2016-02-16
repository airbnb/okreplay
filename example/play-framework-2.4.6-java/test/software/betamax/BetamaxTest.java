package software.betamax;

import software.betamax.junit.Betamax;
import software.betamax.junit.RecorderRule;
import org.junit.Rule;
import org.junit.Test;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static play.test.Helpers.*;

/**
 * Created by sean on 2/7/16.
 */
public class BetamaxTest {

    File f = new File("test/resources/betamax/tapes");
    Configuration configuration = ProxyConfiguration.builder().tapeRoot(f).ignoreLocalhost(false).build();
    @Rule public RecorderRule recorder = new RecorderRule(configuration);

    @Test
    @Betamax(tape="my tape")
    public void testInServer() {
        int port = 3333;
        running(testServer(port), () -> {
            WSResponse wsResponse = WS.url("http://localhost:" + port).get().get(5000);
            assertEquals("PLAY", wsResponse.getHeader("X-Betamax"));
            assertEquals(903, wsResponse.getStatus());
            assertEquals("Hello from Betamax", wsResponse.getBody()); // altered body to confirm playback
        });
    }
}

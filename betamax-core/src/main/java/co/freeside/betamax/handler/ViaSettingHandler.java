package co.freeside.betamax.handler;

import co.freeside.betamax.message.Request;
import co.freeside.betamax.message.Response;

import static co.freeside.betamax.Headers.VIA_HEADER;
import static org.apache.http.HttpHeaders.VIA;

public class ViaSettingHandler extends ChainedHttpHandler {
    @Override
    public Response handle(Request request) {
        Response response = chain(request);
        response.addHeader(VIA, VIA_HEADER);
        return response;
    }
}

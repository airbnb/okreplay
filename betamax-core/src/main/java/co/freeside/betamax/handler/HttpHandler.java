package co.freeside.betamax.handler;

import co.freeside.betamax.message.*;

public interface HttpHandler {

	Response handle(Request request);

}
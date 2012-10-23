package co.freeside.betamax.message.tape

import co.freeside.betamax.message.Request

class RecordedRequest extends RecordedMessage implements Request {

	String method
	URI uri

}

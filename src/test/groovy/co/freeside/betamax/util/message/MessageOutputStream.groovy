package co.freeside.betamax.util.message

class MessageOutputStream extends ByteArrayOutputStream {

	private final BasicMessage delegate

	MessageOutputStream(BasicMessage delegate) {
		this.delegate = delegate
	}

	@Override
	void close() {
		super.close()
		delegate.body = toByteArray()
	}

}

package co.freeside.betamax.encoding

class NoOpEncoder extends AbstractEncoder {

	@Override
	protected InputStream getDecodingInputStream(InputStream input) {
		input
	}

	@Override
	protected OutputStream getEncodingOutputStream(OutputStream output) {
		output
	}

}

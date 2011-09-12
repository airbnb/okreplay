package betamax.util.servlet

import javax.servlet.ServletOutputStream

class MockServletOutputStream extends ServletOutputStream {

	private final AbstractMockServletMessage delegate
	private final ByteArrayOutputStream stream = new ByteArrayOutputStream()

	MockServletOutputStream(AbstractMockServletMessage delegate) {
		this.delegate = delegate
	}

	@Override
	void write(int i) {
		stream.write(i)
	}

	@Override
	void flush() {
		super.flush()
		stream.flush()
		delegate.body = stream.toByteArray()
	}

	@Override
	void close() {
		super.close()
		stream.close()
		delegate.body = stream.toByteArray()
	}

}

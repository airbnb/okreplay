package co.freeside.betamax.util.servlet

import javax.servlet.ServletOutputStream
import javax.servlet.http.*
import org.apache.commons.collections.iterators.*

class MockHttpServletResponse implements HttpServletResponse {

	int status
	String message
	String characterEncoding
	String contentType
	Locale locale
	byte[] body

	private final Map<String, List<String>> headers = [:]
	private boolean written = false
	private ServletOutputStream outputStream
	private PrintWriter writer

	@Override
	void sendError(int sc, String msg) {
		status = sc
		message = msg
	}

	@Override
	void sendError(int sc) {
		status = sc
	}

	@Override
	void setStatus(int sc, String sm) {
		status = sc
		message = sm
	}

	final String getHeader(String name) {
		headers[name]?.join(', ')
	}

	final Enumeration getHeaders(String name) {
		def itr = headers.containsKey(name) ? headers[name].iterator() : EmptyIterator.INSTANCE
		new IteratorEnumeration(itr)
	}

	@Override
	final boolean containsHeader(String name) {
		headers.containsKey(name)
	}

	@Override
	final void setDateHeader(String name, long date) {
	}

	@Override
	final void addDateHeader(String name, long date) {
	}

	@Override
	final void setHeader(String name, String value) {
		headers[name] = [value]
	}

	@Override
	final void addHeader(String name, String value) {
		if (headers.containsKey(name)) {
			headers[name] << value
		} else {
			setHeader(name, value)
		}
	}

	@Override
	final void setIntHeader(String name, int value) {
		setHeader(name, value.toString())
	}

	@Override
	final void addIntHeader(String name, int value) {
		addHeader(name, value.toString())
	}

	@Override
	ServletOutputStream getOutputStream() {
		if (!outputStream) {
			if (written) {
				throw new IllegalStateException()
			} else {
				written = true
				outputStream = new MockServletOutputStream(this)
			}
		}
		outputStream
	}

	@Override
	PrintWriter getWriter() {
		if (!writer) {
			if (written) {
				throw new IllegalStateException()
			} else {
				written = true
				writer = new PrintWriter(new MockServletOutputStream(this), characterEncoding)
			}
		}
		writer
	}

	@Override
	void addCookie(Cookie cookie) {
		throw new UnsupportedOperationException()
	}

	@Override
	String encodeURL(String url) {
		throw new UnsupportedOperationException()
	}

	@Override
	String encodeRedirectURL(String url) {
		throw new UnsupportedOperationException()
	}

	@Override
	String encodeUrl(String url) {
		throw new UnsupportedOperationException()
	}

	@Override
	String encodeRedirectUrl(String url) {
		throw new UnsupportedOperationException()
	}

	@Override
	void sendRedirect(String location) {
		throw new UnsupportedOperationException()
	}

	@Override
	void setContentLength(int len) {
		throw new UnsupportedOperationException()
	}

	@Override
	void setBufferSize(int size) {
		throw new UnsupportedOperationException()
	}

	@Override
	int getBufferSize() {
		throw new UnsupportedOperationException()
	}

	@Override
	void flushBuffer() {
	}

	@Override
	void resetBuffer() {
		throw new UnsupportedOperationException()
	}

	@Override
	boolean isCommitted() {
		throw new UnsupportedOperationException()
	}

	@Override
	void reset() {
		throw new UnsupportedOperationException()
	}

}

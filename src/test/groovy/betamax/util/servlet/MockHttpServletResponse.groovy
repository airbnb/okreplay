package betamax.util.servlet

import javax.servlet.ServletOutputStream
import javax.servlet.http.*

class MockHttpServletResponse extends AbstractMockServletMessage implements HttpServletResponse {

	int status
	String message

	private boolean written = false

	void sendError(int sc, String msg) {
		status = sc
		message = msg
	}

	void sendError(int sc) {
		status = sc
	}

	void setStatus(int sc, String sm) {
		status = sc
		message = sm
	}

	ServletOutputStream getOutputStream() {
		if (written) {
			throw new IllegalStateException()
		} else {
			written = true
			new MockServletOutputStream(this)
		}
	}

	PrintWriter getWriter() {
		if (written) {
			throw new IllegalStateException()
		} else {
			written = true
			new PrintWriter(new MockServletOutputStream(this), characterEncoding)
		}
	}

	void addCookie(Cookie cookie) {
		throw new UnsupportedOperationException()
	}

	String encodeURL(String url) {
		throw new UnsupportedOperationException()
	}

	String encodeRedirectURL(String url) {
		throw new UnsupportedOperationException()
	}

	String encodeUrl(String url) {
		throw new UnsupportedOperationException()
	}

	String encodeRedirectUrl(String url) {
		throw new UnsupportedOperationException()
	}

	void sendRedirect(String location) {
		throw new UnsupportedOperationException()
	}

	void setContentLength(int len) {
		throw new UnsupportedOperationException()
	}

	void setBufferSize(int size) {
		throw new UnsupportedOperationException()
	}

	int getBufferSize() {
		throw new UnsupportedOperationException()
	}

	void flushBuffer() {
		throw new UnsupportedOperationException()
	}

	void resetBuffer() {
		throw new UnsupportedOperationException()
	}

	boolean isCommitted() {
		throw new UnsupportedOperationException()
	}

	void reset() {
		throw new UnsupportedOperationException()
	}

}

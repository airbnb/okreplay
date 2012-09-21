package co.freeside.betamax.util.servlet

import org.apache.commons.collections.iterators.IteratorEnumeration

import javax.servlet.RequestDispatcher
import javax.servlet.ServletInputStream
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession
import java.security.Principal

class MockHttpServletRequest extends AbstractMockServletMessage implements HttpServletRequest {

	String method
	String pathInfo
	String pathTranslated
	String contextPath
	String queryString
	String requestURI
	String servletPath
	String protocol
	String scheme
	String serverName
	int serverPort
	String remoteAddr
	String remoteHost
	int remotePort
	String localName
	String localAddr
	int localPort

	private final Map<String, Object> attributes = [:]
	private final Map<String, String> parameters = [:]
	private boolean read = false

	StringBuffer getRequestURL() {
		null
	}

	Object getAttribute(String name) {
		attributes[name]
	}

	Enumeration getAttributeNames() {
		def itr = attributes.keySet().iterator()
		new IteratorEnumeration(itr)
	}

	int getContentLength() {
		body?.length ?: 0
	}

	ServletInputStream getInputStream() {
		if (read) {
			throw new IllegalStateException()
		} else {
			read = true
			def stream = body ? new ByteArrayInputStream(body) : new ByteArrayInputStream(new byte[0])
			new MockServletInputStream(stream)
		}
	}

	String getParameter(String name) {
		parameters[name]
	}

	Enumeration getParameterNames() {
		def itr = parameters.keySet().iterator()
		new IteratorEnumeration(itr)
	}

	String[] getParameterValues(String name) {
		parameters.values().toArray()
	}

	Map getParameterMap() {
		parameters.asImmutable()
	}

	BufferedReader getReader() {
		if (read) {
			throw new IllegalStateException()
		} else {
			read = true
			def stream = body ? new ByteArrayInputStream(body) : new ByteArrayInputStream(new byte[0])
			new BufferedReader(new InputStreamReader(stream, characterEncoding))
		}
	}

	void setAttribute(String name, Object o) {
		attributes[name] = o
	}

	void removeAttribute(String name) {
		attributes.remove(name)
	}

	String getAuthType() {
		throw new UnsupportedOperationException()
	}

	Cookie[] getCookies() {
		throw new UnsupportedOperationException()
	}

	String getRemoteUser() {
		throw new UnsupportedOperationException()
	}

	boolean isUserInRole(String role) {
		throw new UnsupportedOperationException()
	}

	Principal getUserPrincipal() {
		throw new UnsupportedOperationException()
	}

	String getRequestedSessionId() {
		throw new UnsupportedOperationException()
	}

	HttpSession getSession(boolean create) {
		throw new UnsupportedOperationException()
	}

	HttpSession getSession() {
		throw new UnsupportedOperationException()
	}

	boolean isRequestedSessionIdValid() {
		throw new UnsupportedOperationException()
	}

	boolean isRequestedSessionIdFromCookie() {
		throw new UnsupportedOperationException()
	}

	boolean isRequestedSessionIdFromURL() {
		throw new UnsupportedOperationException()
	}

	boolean isRequestedSessionIdFromUrl() {
		throw new UnsupportedOperationException()
	}

	Enumeration getLocales() {
		throw new UnsupportedOperationException()
	}

	boolean isSecure() {
		throw new UnsupportedOperationException()
	}

	RequestDispatcher getRequestDispatcher(String path) {
		throw new UnsupportedOperationException()
	}

	String getRealPath(String path) {
		throw new UnsupportedOperationException()
	}

}

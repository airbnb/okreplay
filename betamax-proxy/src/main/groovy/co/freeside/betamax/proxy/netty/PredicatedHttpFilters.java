package co.freeside.betamax.proxy.netty;

import com.google.common.base.*;
import io.netty.handler.codec.http.*;
import org.littleshoot.proxy.*;

public class PredicatedHttpFilters extends HttpFiltersAdapter {

	private final HttpFilters delegate;
	private final Predicate<HttpRequest> predicate;

	public PredicatedHttpFilters(HttpFilters delegate, Predicate<HttpRequest> predicate, HttpRequest originalRequest) {
		super(originalRequest);
		this.delegate = delegate;
		this.predicate = predicate;
	}

	@Override
	public HttpResponse requestPre(HttpObject httpObject) {
		if (predicate.apply(originalRequest)) {
			return delegate.requestPre(httpObject);
		} else {
			return null;
		}
	}

	@Override
	public HttpResponse requestPost(HttpObject httpObject) {
		if (predicate.apply(originalRequest)) {
			return delegate.requestPost(httpObject);
		} else {
			return null;
		}
	}

	@Override
	public void responsePre(HttpObject httpObject) {
		if (predicate.apply(originalRequest)) {
			delegate.responsePre(httpObject);
		}
	}

	@Override
	public void responsePost(HttpObject httpObject) {
		if (predicate.apply(originalRequest)) {
			delegate.responsePost(httpObject);
		}
	}
}

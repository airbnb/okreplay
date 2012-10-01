package co.freeside.betamax.util.httpbuilder

import groovy.transform.InheritConstructors
import groovyx.net.http.RESTClient
import org.apache.http.impl.client.*
import org.apache.http.params.HttpParams

@InheritConstructors
class BetamaxRESTClient extends RESTClient {

	@Override
	protected AbstractHttpClient createClient(HttpParams params) {
		new SystemDefaultHttpClient(params)
	}
}

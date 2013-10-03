/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.freeside.betamax.util.server

import co.freeside.betamax.proxy.jetty.SimpleServer
import groovy.transform.InheritConstructors
import org.eclipse.jetty.server.*
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector

@InheritConstructors
class SimpleSecureServer extends SimpleServer {

	@Override
	String getUrl() {
		"https://$host:$port/"
	}

	@Override
	protected Server createServer(int port) {
		def server = super.createServer(port)

		def connector = new SslSelectChannelConnector()

		def keystore = SimpleSecureServer.getResource('/betamax.keystore')

		connector.port = port
		connector.keystore = keystore
		connector.password = 'password'
		connector.keyPassword = 'password'

		server.connectors = [connector]as Connector[]

		server
	}
}

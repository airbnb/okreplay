/*
 * Copyright 2011 Rob Fletcher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package betamax.util.server

import java.util.concurrent.CountDownLatch
import org.apache.log4j.Logger
import org.eclipse.jetty.util.component.AbstractLifeCycle.AbstractLifeCycleListener
import org.eclipse.jetty.util.component.LifeCycle
import org.eclipse.jetty.server.*

class SimpleServer extends AbstractLifeCycleListener {

	private final String host
	private final int port
	private Server server
	private CountDownLatch startedLatch
	private CountDownLatch stoppedLatch
	private final log = Logger.getLogger(SimpleServer)

	SimpleServer() {
		host = InetAddress.localHost.hostAddress
		port = 5000
	}

	String getUrl() {
		"http://$host:$port/"
	}

	void start(Class <? extends Handler> handlerClass) {
		startedLatch = new CountDownLatch(1)
		stoppedLatch = new CountDownLatch(1)

		server = new Server(port)
		server.handler = handlerClass.newInstance()
		server.addLifeCycleListener(this)
		server.start()

		startedLatch.await()
	}

	void stop() {
		if (server) {
			server.stop()
			stoppedLatch.await()
		}
	}

	@Override
	void lifeCycleStarted(LifeCycle event) {
		log.debug "started..."
		startedLatch.countDown()
	}

	@Override
	void lifeCycleStopped(LifeCycle event) {
		log.debug "stopped..."
		stoppedLatch.countDown()
	}

}


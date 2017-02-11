/*
 * Copyright 2013 the original author or authors.
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

package software.betamax.proxy;

import software.betamax.internal.RecorderListener;
import software.betamax.tape.Tape;

public class ProxyServer implements RecorderListener {
  private final BetamaxInterceptor interceptor;
  private boolean running;

  public ProxyServer(BetamaxInterceptor interceptor) {
    this.interceptor = interceptor;
  }

  @Override public void onRecorderStart(Tape tape) {
    if (!isRunning()) {
      start(tape);
    }
  }

  @Override public void onRecorderStop() {
    if (isRunning()) {
      stop();
    }
  }

  private boolean isRunning() {
    return running;
  }

  public void start(final Tape tape) {
    if (isRunning()) {
      throw new IllegalStateException("Betamax proxy server is already running");
    }
    interceptor.start(tape);
    running = true;
  }

  public void stop() {
    if (!isRunning()) {
      throw new IllegalStateException("Betamax proxy server is already stopped");
    }
    interceptor.stop();
    running = false;
  }
}


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

package software.betamax;

import java.io.IOException;
import java.util.Arrays;

import okhttp3.Request;
import okio.Buffer;

/** Standard {@link MatchRule} implementations. */
public enum MatchRules implements MatchRule {
  method {
    @Override public boolean isMatch(Request a, Request b) {
      return a.method().equalsIgnoreCase(b.method());
    }
  }, uri {
    @Override public boolean isMatch(Request a, Request b) {
      return a.url().equals(b.url());
    }
  }, host {
    @Override public boolean isMatch(Request a, Request b) {
      return a.url().url().getHost().equals(b.url().url().getHost());
    }
  }, path {
    @Override public boolean isMatch(Request a, Request b) {
      return a.url().url().getPath().equals(b.url().url().getPath());
    }
  }, port {
    @Override public boolean isMatch(Request a, Request b) {
      return a.url().url().getPort() == b.url().url().getPort();
    }
  }, query {
    @Override public boolean isMatch(Request a, Request b) {
      return a.url().url().getQuery().equals(b.url().url().getQuery());
    }
  }, queryParams {
    /**
     * Compare query parameters instead of query string representation.
     */
    @Override public boolean isMatch(Request a, Request b) {
      if ((a.url().url().getQuery() != null) && (b.url().url().getQuery() != null)) {
        // both request have a query, split query params and compare
        String[] aParameters = a.url().url().getQuery().split("&");
        String[] bParameters = b.url().url().getQuery().split("&");
        Arrays.sort(aParameters);
        Arrays.sort(bParameters);
        return Arrays.equals(aParameters, bParameters);
      } else {
        return (a.url().url().getQuery() == null) && (b.url().url().getQuery() == null);
      }
    }
  }, authorization {
    @Override public boolean isMatch(Request a, Request b) {
      return a.header("Authorization").equals(b.header("Authorization"));
    }
  }, accept {
    @Override public boolean isMatch(Request a, Request b) {
      return a.header("Accept").equals(b.header("Accept"));
    }
  }, body {
    @Override public boolean isMatch(Request a, Request b) {
      try {
        Buffer bufferA = new Buffer();
        a.body().writeTo(bufferA);
        Buffer bufferB = new Buffer();
        b.body().writeTo(bufferB);
        return Arrays.equals(bufferA.readByteArray(), bufferB.readByteArray());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}

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

import software.betamax.message.Request;

import java.util.Arrays;

/**
 * Standard {@link MatchRule} implementations.
 */
public enum MatchRules implements MatchRule {
    method {
        @Override
        public boolean isMatch(Request a, Request b) {
            return a.getMethod().equalsIgnoreCase(b.getMethod());
        }
    }, uri {
        @Override
        public boolean isMatch(Request a, Request b) {
            return a.getUri().equals(b.getUri());
        }
    }, host {
        @Override
        public boolean isMatch(Request a, Request b) {
            return a.getUri().getHost().equals(b.getUri().getHost());
        }
    }, path {
        @Override
        public boolean isMatch(Request a, Request b) {
            return a.getUri().getPath().equals(b.getUri().getPath());
        }
    }, port {
        @Override
        public boolean isMatch(Request a, Request b) {
            return a.getUri().getPort() == b.getUri().getPort();
        }
    }, query {
        @Override
        public boolean isMatch(Request a, Request b) {
            return a.getUri().getQuery().equals(b.getUri().getQuery());
        }
    },
    /**
     * Compare query parameters instead of query string representation.
     */
    queryParams {
        @Override
        public boolean isMatch(Request a, Request b) {
            String[] aParameters = a.getUri().getQuery().split("&");
            String[] bParameters = b.getUri().getQuery().split("&");
            Arrays.sort(aParameters);
            Arrays.sort(bParameters);
            return Arrays.equals(aParameters, bParameters);
        }
    }, authorization {
        @Override
        public boolean isMatch(Request a, Request b) {
            return a.getHeader("Authorization").equals(b.getHeader("Authorization"));
        }
    }, accept {
        @Override
        public boolean isMatch(Request a, Request b) {
            return a.getHeader("Accept").equals(b.getHeader("Accept"));
        }
    }, body {
        @Override
        public boolean isMatch(Request a, Request b) {
            return Arrays.equals(a.getBodyAsBinary(), b.getBodyAsBinary());
        }
    }
}

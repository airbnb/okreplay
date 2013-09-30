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

package co.freeside.betamax;

import co.freeside.betamax.message.Request;
import com.google.common.collect.Maps;
import com.google.common.collect.SortedMapDifference;
import com.google.common.io.CharStreams;
import com.google.common.primitives.Ints;

import java.io.IOException;
import java.util.Comparator;
import java.util.TreeMap;

/**
 * Implements a request matching rule for finding recordings on a tape.
 */
public enum MatchRule implements Comparator<Request> {
    method {
        @Override
        public int compare(Request a, Request b) {
            return a.getMethod().compareTo(b.getMethod());
        }
    }, uri {
        @Override
        public int compare(Request a, Request b) {
            return a.getUri().compareTo(b.getUri());
        }
    }, host {
        @Override
        public int compare(Request a, Request b) {
            return a.getUri().getHost().compareTo(b.getUri().getHost());
        }
    }, path {
        @Override
        public int compare(Request a, Request b) {
            return a.getUri().getPath().compareTo(b.getUri().getPath());
        }
    }, port {
        @Override
        public int compare(Request a, Request b) {
            return Ints.compare(a.getUri().getPort(), b.getUri().getPort());
        }
    }, query {
        @Override
        public int compare(Request a, Request b) {
            return a.getUri().getQuery().compareTo(b.getUri().getQuery());
        }
    }, fragment {
        @Override
        public int compare(Request a, Request b) {
            return a.getUri().getFragment().compareTo(b.getUri().getFragment());
        }
    }, headers {
        @Override
        public int compare(Request a, Request b) {
            Integer result = Ints.compare(a.getHeaders().size(), b.getHeaders().size());
            if (result != 0) {
                return result;
            }

            TreeMap<String, Object> stringObjectTreeMap = Maps.newTreeMap(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
            });
            stringObjectTreeMap.putAll(a.getHeaders());

            SortedMapDifference<String, Object> difference = Maps.difference(stringObjectTreeMap, b.getHeaders());

            return difference.areEqual() ? 0 : -1;
        }


    }, body {
        @Override
        public int compare(Request a, Request b) {
            try {
                return CharStreams.toString(a.getBodyAsText()).compareTo(CharStreams.toString(b.getBodyAsText()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    };

    public int compare(Request a, Request b) {
        throw new UnsupportedOperationException();
    }

}

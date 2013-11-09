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

package co.freeside.betamax.util;

import java.util.*;
import com.google.common.base.*;
import com.google.common.collect.*;

public class MultimapUtils {

    private static class JoinTransformer implements Maps.EntryTransformer<String, Collection<String>, String> {

        private final Joiner joiner;

        private JoinTransformer(Joiner joiner) {
            this.joiner = joiner;
        }

        private JoinTransformer(String separator) {
            this(Joiner.on(separator));
        }

        @Override
        public String transformEntry(String key, Collection<String> value) {
            return joiner.join(value);
        }
    }

    private static class SplitTransformer implements Maps.EntryTransformer<String, String, Collection<String>> {

        private final Splitter splitter;

        public SplitTransformer(Splitter splitter) {
            this.splitter = splitter;
        }

        public SplitTransformer(String separator) {
            this(Splitter.on(separator));
        }

        @Override
        public Collection<String> transformEntry(String key, String value) {
            return splitter.splitToList(value);
        }
    }

    /**
     * Flattens a `Multimap` whose values are strings into a regular `Map`
     * whose
     * values are comma-separated strings.
     *
     * For example `{"a": ["foo", "bar"], "b": "baz"}` transforms to `{"a":
     * "foo,bar", "b": "baz"}`.
     */
    public static Map<String, String> flatten(Multimap<String, String> multimap, String separator) {
        return Maps.transformEntries(multimap.asMap(), new JoinTransformer(separator));
    }

    public static Map<String, Collection<String>> unflatten(Map<String, String> map, String separator) {
        return Maps.transformEntries(map, new SplitTransformer(separator));
    }

    private MultimapUtils() {
    }
}

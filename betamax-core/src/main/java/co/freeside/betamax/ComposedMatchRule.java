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

package co.freeside.betamax;

import co.freeside.betamax.message.*;
import com.google.common.base.*;
import com.google.common.collect.*;

public class ComposedMatchRule implements MatchRule {

    public static MatchRule of(MatchRule... rules) {
        return new ComposedMatchRule(ImmutableSet.copyOf(rules));
    }

    public static MatchRule of(Iterable<MatchRule> rules) {
        return new ComposedMatchRule(ImmutableSet.copyOf(rules));
    }

    private final ImmutableSet<MatchRule> rules;

    private ComposedMatchRule(ImmutableSet<MatchRule> rules) {
        this.rules = rules;
    }

    @Override
    public boolean isMatch(final Request a, final Request b) {
        return Iterables.all(rules, new Predicate<MatchRule>() {
            @Override
            public boolean apply(MatchRule rule) {
                return rule.isMatch(a, b);
            }
        });
    }

    @Override
    public int hashCode() {
        return rules.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ComposedMatchRule that = (ComposedMatchRule) o;

        return rules.equals(that.rules);
    }
}

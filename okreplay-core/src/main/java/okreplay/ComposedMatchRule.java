package okreplay;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

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

  @Override public boolean isMatch(final Request a, final Request b) {
    return Iterables.all(rules, new Predicate<MatchRule>() {
      @Override public boolean apply(MatchRule rule) {
        return rule.isMatch(a, b);
      }
    });
  }

  @Override public int hashCode() {
    return rules.hashCode();
  }

  @Override public boolean equals(Object o) {
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

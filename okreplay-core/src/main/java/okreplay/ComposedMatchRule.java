package okreplay;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import static okreplay.Util.all;

public class ComposedMatchRule implements MatchRule {
  public static MatchRule of(MatchRule... rules) {
    return new ComposedMatchRule(new LinkedHashSet<>(Arrays.asList(rules)));
  }

  public static MatchRule of(Collection<MatchRule> rules) {
    return new ComposedMatchRule(new LinkedHashSet<>(rules));
  }

  private final Set<MatchRule> rules;

  private ComposedMatchRule(Set<MatchRule> rules) {
    this.rules = rules;
  }

  @Override public boolean isMatch(final Request a, final Request b) {
    return all(rules.iterator(), new Predicate<MatchRule>() {
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

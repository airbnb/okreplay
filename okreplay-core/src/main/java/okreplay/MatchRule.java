package okreplay;

/**
 * A rule used to determine whether a recorded HTTP interaction on tape matches a new request being
 * made.
 */
public interface MatchRule {
  boolean isMatch(Request a, Request b);
}

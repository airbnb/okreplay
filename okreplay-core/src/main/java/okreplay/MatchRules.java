package okreplay;

import java.util.Arrays;

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
      return Arrays.equals(a.body(), b.body());
    }
  }
}

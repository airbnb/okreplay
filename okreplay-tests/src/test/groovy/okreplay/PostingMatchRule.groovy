package okreplay

class PostingMatchRule implements MatchRule {
  @Override
  boolean isMatch(Request a, Request b) {
    if (a.url() == b.url() && a.method() == b.method()) {
      //Same method and URI, lets do a body comparison
      //Can only consume the body once, once it's gone it's gone.
      def aBody = a.bodyAsText
      def bBody = b.bodyAsText

      //Right now, lets just compare the bodies also
      return aBody == bBody
    } else {
      //URI and method don't match, so we're going to bail
      return false
    }
  }
}

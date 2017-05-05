package okreplay

import okreplay.MatchRule
import okreplay.Request

import java.util.concurrent.atomic.AtomicInteger

class InstrumentedMatchRule implements MatchRule {
  def counter = new AtomicInteger(0)
  def requestValidations = []

  @Override
  boolean isMatch(Request a, Request b) {

    requestValidations.each { rv ->
      rv.call(a)
      rv.call(b)
    }

    def current = counter.incrementAndGet()
    println("Matching attempt: ${current}")
    println("A request class: ${a.getClass()}")
    println("B request class: ${b.getClass()}")

    if (a.url() == b.url() && a.method() == b.method()) {
      //Same method and URI, lets do a body comparison
      //Can only consume the body once, once it's gone it's gone.
      def aBody = a.bodyAsText
      def bBody = b.bodyAsText

      //Ideally in the real world, we'd parse the XML or the JSON and compare the ASTs instead
      // of just comparing the body strings, so that meaningless whitespace doesn't mean anything
      println("aBody:  |" + aBody + "|")
      println("bBody:  |" + bBody + "|")

      //Right now, lets just compare the bodies also
      return aBody == bBody
    } else {
      //URI and method don't match, so we're going to bail
      return false
    }
  }
}

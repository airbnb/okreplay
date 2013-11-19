package co.freeside.betamax.proxy.matchRules

import co.freeside.betamax.MatchRule
import co.freeside.betamax.message.Request

import java.util.concurrent.atomic.AtomicInteger


class InstrumentedMatchRule implements MatchRule {

    def counter = new AtomicInteger(0)
    @Override
    boolean isMatch(Request a, Request b) {
        def current = counter.incrementAndGet()
        System.err.println("Matching attempt: ${current}")
        System.err.println("A request class: ${a.getClass()}")
        System.err.println("B request class: ${b.getClass()}")

        if(a.getUri() == b.getUri() && a.getMethod() == b.getMethod()) {
            //Same method and URI, lets do a body comparison
            def aBody = a.getBodyAsText().getInput().getText()
            def bBody = b.getBodyAsText().getInput().getText()

            //Ideally in the real world, we'd parse the XML or the JSON and compare the ASTs instead
            // of just comparing the body strings, so that meaningless whitespace doesn't mean anything
            System.err.println("aBody: " + aBody)
            System.err.println("bBody: " + bBody)

            //Right now, lets just compare the bodies also
            return aBody.equals(bBody)
        } else {
            //URI and method don't match, so we're going to bail
            return false
        }
    }
}

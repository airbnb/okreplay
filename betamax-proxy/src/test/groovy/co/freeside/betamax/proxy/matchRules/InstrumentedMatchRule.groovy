package co.freeside.betamax.proxy.matchRules

import co.freeside.betamax.MatchRule
import co.freeside.betamax.message.Request

import java.util.concurrent.atomic.AtomicInteger


class InstrumentedMatchRule implements MatchRule {

    def counter = new AtomicInteger(0)

    def requestValidations = []

    def readAll(Reader r) {
        def output = ""
        def line = null
        while((line = r.readLine()) != null) {
            output << line << "\n"
        }
        return output.trim()
    }

    def dumpRequest(Request r) {
        println("Request:")
        def lines = [
                "URI: ${r.uri}",
                "METHOD: ${r.method}",
                "HEADERS: ${r.headers.collect { k,v -> "$k: $v"} join ", "}",
                "BODY: ${readAll(r.bodyAsText.input)}"
        ]
        lines.each { l ->
            println("\t" + l)
        }
    }
    @Override
    boolean isMatch(Request a, Request b) {

        requestValidations.each { rv ->
            rv.call(a)
            rv.call(b)
        }

        //Does doing this eat all the text and ruin it?
        println("REQUEST A:")
        dumpRequest(a)
        println("REQUEST B:")
        dumpRequest(b)
        def current = counter.incrementAndGet()
        println("Matching attempt: ${current}")
        println("A request class: ${a.getClass()}")
        println("B request class: ${b.getClass()}")

        if(a.getUri() == b.getUri() && a.getMethod() == b.getMethod()) {
            //Same method and URI, lets do a body comparison
            def aBody = a.getBodyAsText().getInput().getText()
            def bBody = b.getBodyAsText().getInput().getText()

            //Ideally in the real world, we'd parse the XML or the JSON and compare the ASTs instead
            // of just comparing the body strings, so that meaningless whitespace doesn't mean anything
            println("aBody: " + aBody)
            println("bBody: " + bBody)

            //Right now, lets just compare the bodies also
            return aBody.equals(bBody)
        } else {
            //URI and method don't match, so we're going to bail
            return false
        }
    }
}

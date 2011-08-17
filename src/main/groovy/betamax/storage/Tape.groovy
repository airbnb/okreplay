package betamax.storage

import org.apache.http.HttpResponse
import org.apache.http.HttpRequest
import org.apache.http.Header

class Tape {

    String name
    Collection<Programme> programmes = new HashSet<Programme>()

    void play(HttpRequest request, HttpResponse response) {
        null
    }

    void record(HttpRequest request, HttpResponse response) {
        def programme = new Programme()
        programme.request.method = request.requestLine.method
        programme.request.uri = request.requestLine.uri
        programme.response.status = response.statusLine.statusCode
        programme.response.body = response.entity.content.text
        programme.response.headers = response.allHeaders.collectEntries {
            [it.name, it.value]
        }
        programme
    }

    void eject() {

    }

}

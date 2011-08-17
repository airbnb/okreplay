package betamax.storage

import org.apache.http.*

class Tape {

    String name
    Collection<Programme> programmes = new HashSet<Programme>()

    boolean read(HttpRequest request, HttpResponse response) {
		def programme = programmes.find { it.request.uri == request.requestLine.uri }
		if (programme) {
			programme.readTo(response)
			true
		} else {
			false
		}
    }

	void write(HttpRequest request, HttpResponse response) {
        programmes << Programme.write(request, response)
    }

    void eject() {

    }

}

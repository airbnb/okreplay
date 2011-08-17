package betamax.storage

class Programme {

    Programme() {
        request = new Request()
        response = new Response()
    }

    final Request request
    final Response response

}

class Request {
    String method
    String uri
}

class Response {
	String protocol
	int status
	String body
	Map<String, String> headers = [:]
}
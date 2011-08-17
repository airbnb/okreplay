# Betamax

A Groovy testing tool inspired by Ruby's [VCR][1].

This project is currently in the prototype stage.

## Roadmap

* `Betamax.withTape(name, Closure)` as a way of scoping proxy usage
* `@Betamax(tape=name)` for tests/specs
* Non-text responses
* Multipart requests
* Calculate content length header rather than specifying in tape
* Rotate multiple responses for same URL on same tape
* Throw exceptions if tape not inserted & proxy gets hit
* Allow groovy in tape files
* Match requests based on URI, host, path, method, body, headers
* Optionally re-record tapes after TTL expires
* Record modes

[1]:https://github.com/myronmarston/vcr
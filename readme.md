# Betamax

A Groovy testing tool inspired by Ruby's [VCR][1].

This project is currently in the prototype stage.

## Roadmap

* HTTP proxy using Apache HTTPClient
* HTTPCacheStorage that can read from & write to Groovy config objects
* `Betamax.withTape(name, Closure)` as a way of scoping proxy usage
* `@Betamax(tape=name)` for tests/specs

[1]:https://github.com/myronmarston/vcr
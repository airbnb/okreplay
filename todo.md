## Inconsistencies

* Response.getBody is doing inconsistent things between various impls if there is no body

## Code smells

* Is there a way we can write to some kind of response delegator that does appropriate filtering & sends data to multiple delegates? Too much copying shit around going on.

## Implementation selection

* The HttpHandlerChain should only be constructed in one place as it is (I think) always the same. If Recorder class is tidied up it should probably be done in there.

## Public API

* Recorder and ProxyServer should be merged
* JUnit Rule implementation should be moved out of Recorder into its own class
* Rule should be able to look up *active* Betamax impl based on classpath or a provider concept like Guice or Dagger have

## Dependencies

* No Groovy
* No Apache commons
* Maybe Guice or Dagger if really justified

## Docs

* Port to Sass
* Use Prism instead of Prettify
* Cover use of `@ClassRule`
* Links to all compatibility tests for examples of how to use with various clients.

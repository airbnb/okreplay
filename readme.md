# Betamax

A Groovy testing tool inspired by Ruby's [VCR][vcr]. Betamax can record and play back HTTP interactions made by your app
so that your tests can run without any real HTTP traffic going to external URLs. The first time a test is run any HTTP
traffic is recorded to a _tape_ and subsequent runs will play back the HTTP response without really connecting to the
external endpoint.

Tapes are stored to disk as [YAML][yaml] files and can be modified (or even created) by hand and committed to your project's
source control repository so that other members of the team can use them when running tests. Different tests can use
different tapes to simulate varying responses from external endpoints. Each tape can hold multiple request/response
interactions but each must (currently) have a unique request method and URI.

An example tape file can be found [here][tape].

## Full documentation

Full documentation can be found on [Betamax's home page][home].

## Project status

The current stable version of Betamax is 1.0 which is available from [Maven Central][maven].

Development versions are available from [Sonatype][sonatype].

Please get in touch if you have any  feedback. You can raise defects and feature requests via [GitHub issues][issues].

## Usage

To use Betamax you just need to annotate your JUnit test or [Spock][spock] specifications with `@Betamax(tape="tape name")`
and include a `betamax.Recorder` Rule.

[home]:http://robfletcher.github.com/betamax
[issues]:http://github.com/robfletcher/betamax/issues
[maven]:http://repo1.maven.org/maven2/com/github/robfletcher/betamax/
[sonatype]:https://oss.sonatype.org/content/groups/public/com/github/robfletcher/betamax/
[spock]:http://spockframework.org/
[tape]:https://github.com/robfletcher/betamax/blob/master/src/test/resources/betamax/tapes/smoke_spec.yaml
[vcr]:http://relishapp.com/myronmarston/vcr
[yaml]:http://yaml.org/

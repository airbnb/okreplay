# &beta;etamax [![Build Status](https://drone.io/github.com/robfletcher/betamax/status.png)](https://drone.io/github.com/robfletcher/betamax/latest)

Betamax is a tool for mocking external HTTP resources such as web services and REST APIs in your tests. The project was inspired by the [VCR][vcr] library for Ruby.

Betamax is written in Java and is compatible with tests written using [JUnit][junit] or [Spock][spock] for applications written in any JVM language.

## Usage

### Record

Add a `@Rule RecorderRule` property to your test and annotate test methods with `@Betamax`. The first time the test runs any HTTP traffic is recorded to _tape_.

### Playback

Future test runs replay responses from _tape_ without traffic going to the real target. No more 3rd party downtime or rate limits breaking your tests. You can even run your tests offline! Insert different _tapes_ to stub different responses.

### Customize
_Tapes_ are just [YAML][yaml] files so you can edit them with a text editor, commit to source control, share with your team & use on continuous integration.  An example tape file can be found [here](https://github.com/robfletcher/betamax/blob/master/betamax-proxy/src/test/resources/betamax/tapes/smoke_spec.yaml).

## Full documentation

Full documentation can be found on [Betamax's home page][home].

## Project status

The current stable version of Betamax is 1.1.2 which is available from [Maven Central][mavenrepo].

Add `'co.freeside:betamax:1.1.2'` as a test dependency to your [Gradle][gradle], [Ivy][ivy], [Grails][grails] or [Maven][maven] project (or anything that can use Maven repositories).

Development versions are available from [Sonatype][sonatype].

Betamax's tests run on [Travis CI][travis].

Please get in touch if you have any  feedback. You can raise defects and feature requests via [GitHub issues][issues].

[gradle]:http://gradle.org/
[grails]:http://grails.org/
[home]:http://freeside.co/betamax
[issues]:http://github.com/robfletcher/betamax/issues
[ivy]:http://ant.apache.org/ivy/
[junit]:http://junit.org/
[maven]:http://maven.apache.org/
[mavenrepo]:http://repo1.maven.org/maven2/co/freeside/betamax/
[sonatype]:https://oss.sonatype.org/content/groups/public/co/freeside/betamax/
[spock]:http://spockframework.org/
[travis]:http://travis-ci.org/robfletcher/betamax
[vcr]:http://relishapp.com/myronmarston/vcr
[yaml]:http://yaml.org/

## Notes on running tests from inside IntelliJ IDEA

Go to _Settings -> Compiler_ and ensure that `*.keystore` appears in the _Resource patterns_ otherwise IDEA will not
make the SSL keystore available on the classpath when tests run.

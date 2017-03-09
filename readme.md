# Walkman [![Build Status](https://travis-ci.org/felipecsl/walkman.svg?branch=master)](https://travis-ci.org/felipecsl/walkman)

Automatically record and replay OkHttp network interaction through your Android application.
This project was based on the great [Betamax](https://github.com/betamaxteam/betamax) library - which was inspired by Ruby's awesome [VCR](https://relishapp.com/vcr/vcr/docs) gem.

## Introduction

You don’t want 3rd party downtime, network issues or resource constraints (such as the Twitter API’s
rate limit) to break your tests. Writing custom stub web server code and configuring the application
to connect to a different URI when under test is tedious and might not accurately simulate the real
service.

Walkman aims to solve these problems by intercepting HTTP connections initiated by your application
and replaying previously __recorded__ responses.

The first time a test annotated with `@Betamax` is run, any HTTP traffic is recorded to a tape and
subsequent test runs will play back the recorded HTTP response from the tape without actually
connecting to the external server.

Betamax works with JUnit and Espresso. Betamax can be used to test any Java or Android applications,
provided they are using an `OkHttpClient` to make requests.

Tapes are stored to disk as YAML files and can be modified (or even created) by hand and committed
to your project’s source control repository so they can be shared by other members of your team and
used by your CI server. Different tests can use different tapes to simulate various response conditions.
Each tape can hold multiple request/response interactions. An example tape file can be found
[here](https://github.com/felipecsl/walkman/blob/master/walkman-tests/src/test/resources/walkman/tapes/smoke_spec.yaml).

## Usage

Walkman comes as an OkHttp `Interceptor`. When "started", responses are served from the `Tape` file
when a match is found for the `MatchRule` and the `TapeMode` is readable. If the `Tape` is writable,
responses will be served from the network as usual and the interaction will be stored on a `Tape`.

Add the `WalkmanInterceptor` to your `OkHttpClient`:

```java
WalkmanInterceptor walkmanInterceptor = new WalkmanInterceptor();
OkHttpClient client = new OkHttpClient.Builder()
  .addInterceptor(walkmanInterceptor)
  .build()
```

By default the interceptor won't do anything unless it's explicitly started.

### JUnit integration

TODO

### Espresso integration

In your instrumentation test class, add:

```java
private final ActivityTestRule<MainActivity> activityTestRule =
      new ActivityTestRule<>(MainActivity.class);
  private final WalkmanConfig configuration = new WalkmanConfig.Builder()
      .tapeRoot(new AndroidTapeRoot(getContext(), "testName"))
      .defaultMode(TapeMode.READ_WRITE) // or TapeMode.READ_ONLY
      .sslEnabled(true)
      .interceptor(walkmanInterceptor))
      .build();
  @Rule public final TestRule testRule =
      new WalkmanRuleChain(configuration, activityTestRule).get();

  @Test
  @Walkman
  public void testFooBar() {
    // write your test as usual...
  }
```

**IMPORTANT**: If you already have one, remove the `@Rule` annotation from your `ActivityTestRule`.

## Download

Download [the latest JAR][2] or grab via Maven:
```xml
<dependency>
  <groupId>com.airbnb.walkman</groupId>
  <artifactId>walkman</artifactId>
  <version>1.1.0-SNAPSHOT</version>
</dependency>
```
or Gradle:
```groovy
compile 'com.airbnb.walkman:walkman:1.0.0-SNAPSHOT'
```

Snapshots of the development version are available in [Sonatype's `snapshots` repository][snap].

License
-------

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 [1]: http://airbnb.io/projects/walkman/
 [2]: https://search.maven.org/remote_content?g=com.airbnb.walkman&a=core&v=LATEST
 [snap]: https://oss.sonatype.org/content/repositories/snapshots/
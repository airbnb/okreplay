# OkReplay [![Build Status](https://travis-ci.org/airbnb/okreplay.svg?branch=master)](https://travis-ci.org/airbnb/okreplay)

Automatically record and replay OkHttp network interaction through your Android application.
This project was based on the great [Betamax](https://github.com/betamaxteam/betamax) library - which was inspired by Ruby's awesome [VCR](https://relishapp.com/vcr/vcr/docs) gem.

## Introduction

You don’t want 3rd party downtime, network issues or resource constraints (such as the Twitter API’s
rate limit) to break your tests. Writing custom stub web server code and configuring the application
to connect to a different URI when under test is tedious and might not accurately simulate the real
service.

OkReplay aims to solve these problems by intercepting HTTP connections initiated by your application
and replaying previously __recorded__ responses.

The first time a test annotated with `@OkReplay` is run, any HTTP traffic is recorded to a tape and
subsequent test runs will play back the recorded HTTP response from the tape without actually
connecting to the external server.

OkReplay works with JUnit and Espresso. OkReplay can be used to test any Java or Android applications,
provided they are using an `OkHttpClient` to make requests.

Tapes are stored to disk as YAML files and can be modified (or even created) by hand and committed
to your project’s source control repository so they can be shared by other members of your team and
used by your CI server. Different tests can use different tapes to simulate various response conditions.
Each tape can hold multiple request/response interactions. An example tape file can be found
[here](https://github.com/airbnb/okreplay/blob/master/okreplay-tests/src/test/resources/okreplay/tapes/smoke_spec.yaml).

## Usage

OkReplay comes as an OkHttp `Interceptor`. When "started", responses are served from the `Tape` file
when a match is found for the `MatchRule` and the `TapeMode` is readable. If the `Tape` is writable,
responses will be served from the network as usual and the interaction will be stored on a `Tape`.

Add the `OkReplayInterceptor` to your `OkHttpClient`:

```java
OkReplayInterceptor okReplayInterceptor = new OkReplayInterceptor();
OkHttpClient client = new OkHttpClient.Builder()
  .addInterceptor(okReplayInterceptor)
  .build()
```

By default the interceptor won't do anything unless it's explicitly started.

### Espresso integration

In your instrumentation test class, add:

```java
private final ActivityTestRule<MainActivity> activityTestRule =
      new ActivityTestRule<>(MainActivity.class);
  private final OkReplayConfig configuration = new OkReplayConfig.Builder()
      .tapeRoot(new AndroidTapeRoot(getContext(), getClass()))
      .defaultMode(TapeMode.READ_WRITE) // or TapeMode.READ_ONLY
      .sslEnabled(true)
      .interceptor(okReplayInterceptor))
      .build();
  @Rule public final TestRule testRule =
      new OkReplayRuleChain(configuration, activityTestRule).get();

  @Test
  @OkReplay
  public void testFooBar() {
    // write your test as usual...
  }
```

**IMPORTANT**: If you already have one, remove the `@Rule` annotation from your `ActivityTestRule`.

### Gradle plugin integration

Add the classpath and apply the plugin in your build.config:

```groovy
buildscript {
  repositories {
    maven { url 'https://oss.sonatype.org/content/repositories/snapshots/' }
  }
  dependencies {
    classpath 'com.airbnb.okreplay:gradle-plugin:1.4.0'
  }
}

apply plugin: 'okreplay'

```

You should now see these two tasks when you run `./gradlew tasks`:

```
clearOkReplayTapes - Clear OkReplay tapes from the Device SD Card
pushOkReplayTapes - Push OkReplay tapes to the device
```

## Download

Download [the latest JAR][2] or grab via Maven:
```xml
<dependency>
  <groupId>com.airbnb.okreplay</groupId>
  <artifactId>okreplay</artifactId>
  <version>1.4.0</version>
</dependency>
```
or Gradle:
```groovy
debugCompile 'com.airbnb.okreplay:okreplay:1.4.0'
releaseCompile 'com.airbnb.okreplay:noop:1.4.0'
androidTestCompile 'com.airbnb.okreplay:espresso:1.4.0'
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

 [1]: http://airbnb.io/projects/okreplay/
 [2]: https://search.maven.org/remote_content?g=com.airbnb.okreplay&a=okreplay&v=LATEST
 [snap]: https://oss.sonatype.org/content/repositories/snapshots/

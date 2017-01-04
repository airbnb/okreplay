# &beta;etamax [![Build Status](https://travis-ci.org/betamaxteam/betamax.svg?branch=master)](https://travis-ci.org/betamaxteam/betamax) [![Join the chat at https://gitter.im/betamaxteam/betamax](https://badges.gitter.im/betamaxteam/betamax.svg)](https://gitter.im/betamaxteam/betamax?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Betamax is a tool for mocking external HTTP resources in your tests. The project was inspired by the [VCR](https://relishapp.com/vcr/vcr/docs) library for Ruby.

Documentation hosted at [http://betamax.software/](http://betamax.software/) is currently only for the 1.X branch and is deprecated. New documentation is being produced, but it will take some time. Please see examples in Betamax's tests and follow this readme.

In an effort to refocus the project and do some house-keeping, all relevant work has been moved to our [Trello board](https://trello.com/b/h38RAFcm/betamax). If you'd like to see what we're doing and where we're going with the project, please check it out!

## Installation

Since 2.0.1, requires JDK7 or later.

Betamax is hosted via [Sonatype](https://oss.sonatype.org/) and is intended to be compatible with any Maven-based build tool.

**JUnit**

```
<dependency>
  <groupId>software.betamax</groupId>
  <artifactId>betamax-junit</artifactId>
  <version>2.0.1</version>
  <scope>test</scope>
</dependency>
```

**Specs2 Maven**

```
<dependency>
  <groupId>software.betamax</groupId>
  <artifactId>betamax-specs2_2.11</artifactId>
  <version>2.0.1</version>
  <scope>test</scope>
</dependency>
```

**Specs2 SBT**

```
libraryDependencies += "software.betamax" %% "betamax-specs2" % "2.0.1" % "test"
```

#### Snapshots:

[Snapshots](https://oss.sonatype.org/content/repositories/snapshots/software/betamax/betamax-core) are made after every successful build in master, so if you want the bleeding edge, you know where to get it.

## SSL Configuration

JDK 7 dramatically increased the security of the JVM, making it much more difficult to exploit man-in-the-middle attacks. Because Betamax is a legitimate use of MITM, it is necessary to configure the environment to allow Betamax to do so. This will be accomplished by installing a Betamax certificate-authority into Java's `cacerts` which will allow Betamax to generate a mock-SSL certificate for any site.

**For all environments where tests are being run, a one-time installation of the Betamax certificate into Java's `cacerts` is necessary.**

    keytool -importcert -keystore $JAVA_HOME/jre/lib/security/cacerts -file betamax.pem -alias betamax -storepass changeit -noprompt

*Notes:*

1. `sudo` will likely be required for unix-based operating systems
2. `betamax.pem` is included in the `betamax-core.jar`, but it's probably best to pull it from GitHub.
3. `betamax.pem` shouldn't have a need to change for the foreseeable future, so this installation should last for the life of the tests.

`SecureRandom` requires a significant amount of entropy in order to generate random numbers, and when using SSL, Betamax stresses this aggressively. When `SecureRandom` fails to generate a random in a given time frame (usually around 3 seconds), a test will fail with almost no indiciation as to why, other than an SSL error occurred. It is likely best to get ahead of that issue before it becomes one, especially if your CI environment is Docker/Virtual Machine based.

**To ensure `SecureRandom` will have adequate entropy on Unix-based systems:**

    sed -i -e 's/securerandom.source=file:\/dev\/random/securerandom.source=file:\/dev\/urandom/' $JAVA_HOME/jre/lib/security/java.security

**Files to Ignore:**

Betamax generates files with the following extensions that should not be committed to source control:

- *.csr
- *.cert
- *.jks

**Continuous Integration Considerations:**

For [Docker](https://www.docker.com) users, please use the JDK images hosted on [Docker Hub](https://hub.docker.com/r/betamax/betamax/); they have the Betamax CA installed and ready to go.

For [Travis CI](https://travis-ci.org/
) users, please see Betamax's `.travis.yml`. As of writing, `sudo:required` is necessary in order to install the CA. Hopefully this won't be the case in the future.

For all other CI environments, be sure to use the `keytool` command listed above to ensure the Betamax CA is installed.

## Contributors

**Betamax Team**

- [Sean Freitag (lead)](https://github.com/cowboygneox)
- [Rob Fletcher (creator)](https://github.com/robfletcher)

**Additional Contributions** *(sorted alphabetically by last name)*

- Marcin Erdmann
- Ian Grayson
- Russel Hart
- Lari Hotari
- Michal Kováč
- David Kowis
- Jason LeCount
- Peter Ledbrook
- George McIntosh
- James Newbery
- Ryan Schmitt
- Michael Vorburger
- Alex Ianus

*Note: There were 3 others who didn't appropriately identify themselves via Git, and thus are not on this list.*

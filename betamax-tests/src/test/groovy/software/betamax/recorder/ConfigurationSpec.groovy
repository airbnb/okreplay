/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package software.betamax.recorder

import com.google.common.io.Files
import software.betamax.ComposedMatchRule
import software.betamax.Configuration
import software.betamax.MatchRules
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import static software.betamax.TapeMode.*

class ConfigurationSpec extends Specification {

  @Shared
  @AutoCleanup("deleteDir")
  def tempDir = Files.createTempDir()

  void "uses default configuration if not overridden and no properties file exists"() {
    given:
    def configuration = Configuration.builder().build()

    expect:
    with(configuration) {
      tapeRoot == new File(DEFAULT_TAPE_ROOT)
      defaultMode == DEFAULT_MODE
      defaultMatchRule == DEFAULT_MATCH_RULE
      ignoreHosts == []
      !ignoreLocalhost
      !sslEnabled
    }
  }

  void "configuration is overridden by builder methods"() {
    given:
    def configuration = Configuration.builder()
        .tapeRoot(tempDir)
        .defaultMode(READ_ONLY)
        .defaultMatchRules(MatchRules.host, MatchRules.uri)
        .ignoreHosts(["github.com"])
        .ignoreLocalhost(true)
        .sslEnabled(true)
        .build()


    expect:
    with(configuration) {
      tapeRoot == tempDir
      defaultMode == READ_ONLY
      defaultMatchRule == ComposedMatchRule.of(MatchRules.host, MatchRules.uri)
      ignoreHosts.contains("github.com")
      ignoreLocalhost
      sslEnabled
    }
  }

  void "configuration can be loaded from properties"() {
    given:
    def properties = new Properties()
    properties.with {
      setProperty("betamax.tapeRoot", this.tempDir.absolutePath)
      setProperty("betamax.defaultMode", "READ_WRITE")
      setProperty("betamax.defaultMatchRules", "host,uri")
      setProperty("betamax.ignoreHosts", "github.com,energizedwork.com")
      setProperty("betamax.ignoreLocalhost", "true")
      setProperty("betamax.sslEnabled", "true")
    }

    and:
    def configuration = Configuration.builder().withProperties(properties).build()

    expect:
    with(configuration) {
      tapeRoot == tempDir
      defaultMode == READ_WRITE
      defaultMatchRule == ComposedMatchRule.of(MatchRules.host, MatchRules.uri)
      ignoreHosts.contains("github.com")
      ignoreHosts.contains("energizedwork.com")
      ignoreLocalhost
      sslEnabled
    }
  }

  void "default properties file is used if it exists"() {
    given:
    def propertiesFile = new File(tempDir, "betamax.properties")
    def properties = new Properties()
    properties.with {
      setProperty("betamax.tapeRoot", this.tempDir.absolutePath)
      setProperty("betamax.defaultMode", "READ_WRITE")
      setProperty("betamax.defaultMatchRules", "host,uri")
      setProperty("betamax.ignoreHosts", "github.com,energizedwork.com")
      setProperty("betamax.ignoreLocalhost", "true")
      setProperty("betamax.sslEnabled", "true")
    }
    propertiesFile.withWriter { writer ->
      properties.store(writer, null)
    }

    and:
    Configuration.classLoader.addURL(tempDir.toURL())

    and:
    def configuration = Configuration.builder().build()

    expect:
    with(configuration) {
      tapeRoot == tempDir
      defaultMode == READ_WRITE
      defaultMatchRule == ComposedMatchRule.of(MatchRules.host, MatchRules.uri)
      ignoreHosts.contains("github.com")
      ignoreHosts.contains("energizedwork.com")
      ignoreLocalhost
      sslEnabled
    }

    cleanup:
    while (!propertiesFile.delete()) {
      System.gc()
    }
  }

  void "builder methods override properties file"() {
    given:
    def propertiesFile = new File(tempDir, "betamax.properties")
    def properties = new Properties()
    properties.with {
      setProperty("betamax.tapeRoot", this.tempDir.absolutePath)
      setProperty("betamax.defaultMode", "READ_WRITE")
      setProperty("betamax.defaultMatchRules", "host,query")
      setProperty("betamax.ignoreHosts", "localhost,127.0.0.1")
      setProperty("betamax.ignoreLocalhost", "true")
      setProperty("betamax.sslEnabled", "true")
    }
    propertiesFile.withWriter { writer ->
      properties.store(writer, null)
    }

    and:
    Configuration.classLoader.addURL(tempDir.toURL())

    and:
    def configuration = Configuration.builder()
        .tapeRoot(new File("test/fixtures/tapes"))
        .defaultMode(WRITE_ONLY)
        .defaultMatchRules(MatchRules.port, MatchRules.query)
        .ignoreHosts(["github.com"])
        .ignoreLocalhost(false)
        .sslEnabled(false)
        .build()

    expect:
    with(configuration) {
      tapeRoot == new File("test/fixtures/tapes")
      defaultMode == WRITE_ONLY
      defaultMatchRule == ComposedMatchRule.of(MatchRules.port, MatchRules.query)
      ignoreHosts == ["github.com"]
      !ignoreLocalhost
      !sslEnabled
    }

    cleanup:
    while (!propertiesFile.delete()) {
      System.gc()
    }
  }
}

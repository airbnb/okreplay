package okreplay

import com.google.common.io.Files
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import static okreplay.TapeMode.*

class ConfigurationSpec extends Specification {

  @Shared
  @AutoCleanup("deleteDir")
  def tempDir = Files.createTempDir()

  void "uses default configuration if not overridden and no properties file exists"() {
    given:
    def configuration = new OkReplayConfig.Builder().build()

    expect:
    with(configuration) {
      tapeRoot.get() == new File(DEFAULT_TAPE_ROOT)
      defaultMode == DEFAULT_MODE
      defaultMatchRule == DEFAULT_MATCH_RULE
      ignoreHosts == []
      !ignoreLocalhost
      !sslEnabled
    }
  }

  void "configuration is overridden by builder methods"() {
    given:
    def configuration = new OkReplayConfig.Builder()
        .tapeRoot(tempDir)
        .defaultMode(READ_ONLY)
        .defaultMatchRules(MatchRules.host, MatchRules.uri)
        .ignoreHosts(["github.com"])
        .ignoreLocalhost(true)
        .sslEnabled(true)
        .build()


    expect:
    with(configuration) {
      tapeRoot.get() == tempDir
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
      setProperty("okreplay.tapeRoot", this.tempDir.absolutePath)
      setProperty("okreplay.defaultMode", "READ_WRITE")
      setProperty("okreplay.defaultMatchRules", "host,uri")
      setProperty("okreplay.ignoreHosts", "github.com,energizedwork.com")
      setProperty("okreplay.ignoreLocalhost", "true")
      setProperty("okreplay.sslEnabled", "true")
    }

    and:
    def configuration = new OkReplayConfig.Builder().withProperties(properties).build()

    expect:
    with(configuration) {
      tapeRoot.get() == tempDir
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
    def propertiesFile = new File(tempDir, "okreplay.properties")
    def properties = new Properties()
    properties.with {
      setProperty("okreplay.tapeRoot", this.tempDir.absolutePath)
      setProperty("okreplay.defaultMode", "READ_WRITE")
      setProperty("okreplay.defaultMatchRules", "host,uri")
      setProperty("okreplay.ignoreHosts", "github.com,energizedwork.com")
      setProperty("okreplay.ignoreLocalhost", "true")
      setProperty("okreplay.sslEnabled", "true")
    }
    propertiesFile.withWriter { writer ->
      properties.store(writer, null)
    }

    and:
    OkReplayConfig.classLoader.addURL(tempDir.toURL())

    and:
    def configuration = new OkReplayConfig.Builder().build()

    expect:
    with(configuration) {
      tapeRoot.get() == tempDir
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
    def propertiesFile = new File(tempDir, "okreplay.properties")
    def properties = new Properties()
    properties.with {
      setProperty("okreplay.tapeRoot", this.tempDir.absolutePath)
      setProperty("okreplay.defaultMode", "READ_WRITE")
      setProperty("okreplay.defaultMatchRules", "host,query")
      setProperty("okreplay.ignoreHosts", "localhost,127.0.0.1")
      setProperty("okreplay.ignoreLocalhost", "true")
      setProperty("okreplay.sslEnabled", "true")
    }
    propertiesFile.withWriter { writer ->
      properties.store(writer, null)
    }

    and:
    OkReplayConfig.classLoader.addURL(tempDir.toURL())

    and:
    def configuration = new OkReplayConfig.Builder()
        .tapeRoot(new File("test/fixtures/tapes"))
        .defaultMode(WRITE_ONLY)
        .defaultMatchRules(MatchRules.port, MatchRules.query)
        .ignoreHosts(["github.com"])
        .ignoreLocalhost(false)
        .sslEnabled(false)
        .build()

    expect:
    with(configuration) {
      tapeRoot.get() == new File("test/fixtures/tapes")
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

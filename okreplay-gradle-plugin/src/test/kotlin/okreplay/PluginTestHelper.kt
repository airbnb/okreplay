package okreplay

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.testkit.runner.internal.PluginUnderTestMetadataReading
import java.io.File
import java.util.Properties

fun Project.setupDefaultAndroidProject() {
  prepareLocalProperties(projectDir)

  val manifest = File(projectDir, "src/main/AndroidManifest.xml")
  manifest.parentFile.mkdirs()
  manifest.writeText("<manifest package=\"com.example.okreplay\"/>")
  pluginManager.apply(AppPlugin::class.java)

  val androidExtension = extensions.getByType(BaseExtension::class.java)
  androidExtension.compileSdkVersion(28)
}

fun Project.applyOkReplay() {
  pluginManager.apply(OkReplayPlugin::class.java)
}

fun Project.evaluate() {
  (this as ProjectInternal).evaluate()
}

fun createTempTestDirectory(testProjectName: String): File {
  val dir = File(workingDir, "build/integrationTests/$testProjectName")
  FileUtils.deleteDirectory(dir)
  FileUtils.forceMkdir(dir)
  return dir
}

fun prepareProjectTestDir(destDir: File, testProjectName: String, testBuildScriptName: String) {
  val testProjectsRoot = "src/test/testProject"
  val projectTypeRoot = File("$testProjectsRoot/android")
  val projectUnderTest = File(workingDir, "$projectTypeRoot/$testProjectName")
  check(projectUnderTest.isDirectory) { "Couldn't find test project" }

  val requestedBuildScript = File(
      "$projectTypeRoot/buildScriptFixtures/$testBuildScriptName.gradle")
  val requestedSettingsFile = File(
      "$projectTypeRoot/buildScriptFixtures/settings.gradle")
  check(requestedBuildScript.isFile) { "Couldn't find the test build script" }

  File("$projectTypeRoot/buildScriptFixtures/gradle.properties")
      .copyTo(File(destDir, "gradle.properties"))

  prepareLocalProperties(destDir)
  projectUnderTest.copyRecursively(destDir)
  requestedSettingsFile.copyTo(File(destDir, "settings.gradle"))

  val buildScript = requestedBuildScript.readText()
      .replace("\$PLUGIN_CLASSPATH", getPluginClasspath())
  File(destDir, "build.gradle").writeText(buildScript)
}

private fun prepareLocalProperties(destDir: File) {
  val localProperties = File(destDir, "local.properties")
  localProperties.writeText("sdk.dir=${androidHome()}")
}

private fun androidHome(): String {
  val envVar = System.getenv("ANDROID_HOME")
  if (envVar != null) {
    return envVar
  }
  val localPropFile = File(workingDir.parentFile, "local.properties")
  if (localPropFile.isFile) {
    val props = Properties()
    localPropFile.inputStream().use { props.load(it) }
    val sdkDir = props.getProperty("sdk.dir")
    if (sdkDir != null) {
      return sdkDir
    }
  }
  throw IllegalStateException("SDK location not found. Define location with sdk.dir in the " +
      "local.properties file or with an ANDROID_HOME environment variable.")
}

private fun getPluginClasspath(): String {
  return PluginUnderTestMetadataReading.readImplementationClasspath()
      .asSequence()
      .map { it.absolutePath.replace("\\", "\\\\") } // escape backslashes on Windows
      .joinToString(", ") { "'$it'" }
}

private val workingDir: File
  get() = File(System.getProperty("user.dir"))

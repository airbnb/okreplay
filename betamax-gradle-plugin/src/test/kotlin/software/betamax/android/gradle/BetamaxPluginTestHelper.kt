package software.betamax.android.gradle

import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*

class BetamaxPluginTestHelper {
  companion object {
    @Throws(IOException::class)
    fun createTempTestDirectory(testProjectName: String): File {
      val dir = File(System.getProperty("user.dir"), "build/integrationTests/$testProjectName")
      FileUtils.deleteDirectory(dir)
      FileUtils.forceMkdir(dir)
      return dir
    }

    @Throws(IOException::class)
    private fun androidHome(): String? {
      val envVar = System.getenv("ANDROID_HOME")
      if (envVar != null) {
        return envVar
      }
      val localPropFile = File(File(System.getProperty("user.dir")).parentFile, "local.properties")
      if (localPropFile.isFile) {
        val props = Properties()
        props.load(FileInputStream(localPropFile))
        val sdkDir = props.getProperty("sdk.dir")
        if (sdkDir != null) {
          return sdkDir
        }
        throw IllegalStateException(
            "SDK location not found. Define location with sdk.dir in the local.properties file or "
                + "with an ANDROID_HOME environment variable.")
      }
      return null
    }

    @Throws(IOException::class)
    fun prepareProjectTestDir(destDir: File, testProjectName: String, testBuildScriptName: String) {
      val testProjectsRoot = "src/test/testProject"
      val projectTypeRoot = File("$testProjectsRoot/android")
      val projectUnderTest = File(System.getProperty("user.dir"),
          "$projectTypeRoot/$testProjectName")
      if (!projectUnderTest.isDirectory) {
        throw IllegalArgumentException("Couldn't find test project")
      }

      val requestedBuildScript =
          File("$projectTypeRoot/buildScriptFixtures/$testBuildScriptName.gradle")
      if (!requestedBuildScript.isFile) {
        throw IllegalArgumentException("Couldn't find the test build script")
      }

      prepareLocalProperties(destDir)
      FileUtils.copyDirectory(projectUnderTest, destDir)
      FileUtils.copyFile(requestedBuildScript, File("$destDir/build.gradle"))
    }

    @Throws(IOException::class)
    private fun prepareLocalProperties(destDir: File) {
      val localProperties = File(destDir, "local.properties")
      localProperties.writeText("sdk.dir=${androidHome()!!}")
    }
  }
}
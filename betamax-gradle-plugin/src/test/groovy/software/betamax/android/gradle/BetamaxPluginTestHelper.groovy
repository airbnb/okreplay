package software.betamax.android.gradle

import org.apache.commons.io.FileUtils

class BetamaxPluginTestHelper {
  static File createTempTestDirectory(String testProjectName) {
    File dir = new File(System.getProperty("user.dir"), "build/integrationTests/$testProjectName")
    FileUtils.deleteDirectory(dir)
    FileUtils.forceMkdir(dir)
    return dir
  }

  static def androidHome() {
    def envVar = System.getenv("ANDROID_HOME")
    if (envVar) {
      return envVar
    }
    File localPropFile =
        new File(new File(System.getProperty("user.dir")).parentFile, "local.properties")
    if (localPropFile.isFile()) {
      Properties props = new Properties()
      props.load(new FileInputStream(localPropFile))
      def sdkDir = props.getProperty("sdk.dir")
      if (sdkDir) {
        return sdkDir
      }
      throw IllegalStateException(
          "SDK location not found. Define location with sdk.dir in the local.properties file or " +
              "with an ANDROID_HOME environment variable.")
    }
  }


  static void prepareProjectTestDir(File destDir, String testProjectName,
      String testBuildScriptName) {
    String testProjectsRoot = "src/test/testProject"
    File projectTypeRoot = new File("$testProjectsRoot/android")
    File projectUnderTest =
        new File(System.getProperty("user.dir"), "$projectTypeRoot/$testProjectName")
    if (!projectUnderTest.isDirectory()) {
      throw new IllegalArgumentException("Couldn't find test project")
    }

    File requestedBuildScript =
        new File("$projectTypeRoot/buildScriptFixtures/${testBuildScriptName}.gradle")
    if (!requestedBuildScript.isFile()) {
      throw new IllegalArgumentException("Couldn't find the test build script")
    }

    prepareLocalProperties(destDir)
    FileUtils.copyDirectory(projectUnderTest, destDir)
    FileUtils.copyFile(requestedBuildScript, new File("$destDir/build.gradle"))
  }

  static def prepareLocalProperties(File destDir) {
    def localProperties = new File(destDir, "local.properties")
    localProperties.write("sdk.dir=${androidHome()}")
  }
}
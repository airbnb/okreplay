package okreplay

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.DynamicFeaturePlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.api.TestedVariant
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project

class OkReplayPlugin : Plugin<Project> {

  override fun apply(project: Project) {
    project.plugins.withType(AppPlugin::class.java) {
      project.registerTasks()
    }
    project.plugins.withType(LibraryPlugin::class.java) {
      project.registerTasks()
    }
    project.plugins.withType(DynamicFeaturePlugin::class.java) {
      project.registerTasks()
    }
  }

  private fun Project.registerTasks() {
    getVariants().all { variant ->
      // Only variants with build type matching android.testBuildType will have a test variant
      val testVariant = (variant as TestedVariant).testVariant ?: return@all

      val androidConfig = androidConfig()
      val adbPath = androidConfig.adbExecutable
      val adbTimeoutMs = androidConfig.adbOptions.timeOutInMs

      val targetName = variant.name.capitalize()
      val pullTapesTask = tasks.register("pull${targetName}OkReplayTapes", PullTapesTask::class.java) {
        it.adbPath.set(adbPath)
        it.adbTimeout.set(adbTimeoutMs)
        it.packageName.set(testVariant.applicationId)
        it.outputDir.set(file(LOCAL_TAPES_DIR))
      }
      val clearTapesTask = tasks.register("clear${targetName}OkReplayTapes", ClearTapesTask::class.java) {
        it.adbPath.set(adbPath)
        it.adbTimeout.set(adbTimeoutMs)
        it.packageName.set(testVariant.applicationId)
      }

      testVariant.connectedInstrumentTestProvider.configure { task ->
        task.dependsOn(clearTapesTask)
        task.finalizedBy(pullTapesTask)
      }
    }
  }

  private fun Project.androidConfig(): BaseExtension {
    return extensions.getByType(BaseExtension::class.java)
  }

  private fun Project.getVariants(): DomainObjectSet<out BaseVariant> {
    return when (val androidConfig = androidConfig()) {
      is AppExtension -> androidConfig.applicationVariants
      is LibraryExtension -> androidConfig.libraryVariants
      else -> throw IllegalStateException("Invalid project type")
    }
  }

  // TODO: Make this configurable from the plugin extension script
  companion object {
    const val LOCAL_TAPES_DIR = "src/androidTest/assets/tapes"
    // This is also hardcoded in AndroidTapeRoot#getSdcardDir()
    // Need to use the same value in both places
    const val REMOTE_TAPES_DIR = "okreplay/tapes"
  }
}

package okreplay

import com.android.build.gradle.*
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.internal.DefaultDomainObjectSet
import javax.inject.Inject

class OkReplayPlugin
@Inject constructor() : Plugin<Project> {
  private lateinit var project: Project

  override fun apply(project: Project) {
    this.project = project
    if (project.plugins.hasPlugin(AppPlugin::class.java)
        || project.plugins.hasPlugin(LibraryPlugin::class.java)) {
      applyPlugin()
    } else {
      throw IllegalArgumentException("OkReplay plugin couldn't be applied. "
          + "The Android or Library plugin must be configured first.")
    }
  }

  private fun Task.runBefore(dependentTaskName: String) {
    try {
      val taskToFind = project.tasks.getByName(dependentTaskName)
      taskToFind.dependsOn(this)
    } catch (e: UnknownTaskException) {
      project.tasks.whenTaskAdded { dependentTask ->
        if (dependentTask.name == dependentTaskName) {
          dependentTask.dependsOn(this)
        }
      }
    }
  }

  private fun Task.runAfter(dependentTaskName: String) {
    try {
      val taskToFind = project.tasks.getByName(dependentTaskName)
      taskToFind.finalizedBy(this)
    } catch (e: UnknownTaskException) {
      project.tasks.whenTaskAdded { dependentTask ->
        if (dependentTask.name == dependentTaskName) {
          dependentTask.finalizedBy(this)
        }
      }
    }
  }

  private fun applyPlugin() {
    project.afterEvaluate {
      it.getVariants().all {
        val flavorNameCapitalized = it.flavorName.capitalize()
        val buildNameCapitalized = it.buildType.name.capitalize()
        val targetName = "$flavorNameCapitalized$buildNameCapitalized"
        val pullTapesTask: TapeTask =
            project.tasks.create("pull${targetName}OkReplayTapes", PullTapesTask::class.java)
        val clearTapesTask: TapeTask =
            project.tasks.create("clear${targetName}OkReplayTapes", ClearTapesTask::class.java)
        val extension = project.extensions.getByType(BaseExtension::class.java)
        val adbPath = extension.adbExecutable
        val adbTimeoutMs = extension.adbOptions.timeOutInMs
        val testApplicationId = project.testApplicationId()
        val deviceBridge = DeviceBridgeProvider.get(adbPath, adbTimeoutMs, project)
        listOf(pullTapesTask, clearTapesTask).forEach {
          it.deviceBridge = deviceBridge
          it.packageName = testApplicationId
        }
        clearTapesTask.runBefore("connected${targetName}AndroidTest")
        pullTapesTask.runAfter("connected${targetName}AndroidTest")
      }
    }
  }

  // TODO: Make this configurable from the plugin extension script
  companion object {
    const val LOCAL_TAPES_DIR = "src/androidTest/assets/tapes"
    // This is also hardcoded in AndroidTapeRoot#getSdcardDir()
    // Need to use the same value in both places
    const val REMOTE_TAPES_DIR = "okreplay/tapes"

    private fun Project.androidConfig(): AndroidConfig {
      return extensions.getByName("android") as BaseExtension
    }

    private fun Project.testApplicationId(): String {
      val androidConfig = androidConfig()
      return if (androidConfig is AppExtension || androidConfig is LibraryExtension) {
        if (!(androidConfig as TestedExtension).testVariants.isEmpty()) {
          androidConfig.testVariants.first().applicationId
        } else {
          ""
        }
      } else {
        throw IllegalStateException("Invalid project type")
      }
    }

    private fun Project.getVariants(): DefaultDomainObjectSet<out BaseVariant> {
      val androidConfig = androidConfig()
      when (androidConfig) {
        is AppExtension -> @Suppress("UNCHECKED_CAST")
        return androidConfig.applicationVariants as DefaultDomainObjectSet<BaseVariant>
        is LibraryExtension -> return androidConfig.libraryVariants
        else -> throw IllegalStateException("Invalid project type")
      }
    }
  }
}

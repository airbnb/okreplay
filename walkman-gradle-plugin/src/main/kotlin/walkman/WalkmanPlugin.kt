package walkman

import com.android.build.gradle.*
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.scope.GlobalScope
import com.android.build.gradle.internal.variant.BaseVariantData
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.internal.DefaultDomainObjectSet
import walkman.PullTapesTask
import walkman.PushTapesTask
import javax.inject.Inject

class WalkmanPlugin
@Inject constructor() : Plugin<Project> {
  var project: Project? = null

  override fun apply(project: Project) {
    this.project = project
    if (project.plugins.hasPlugin(AppPlugin::class.java)
        || project.plugins.hasPlugin(LibraryPlugin::class.java)) {
      applyPlugin()
    } else {
      throw IllegalArgumentException("Walkman plugin couldn't be applied. "
          + "The Android or Library plugin must be configured first.")
    }
  }

  private fun Task.runBefore(dependentTaskName: String) {
    try {
      val taskToFind = project!!.tasks.getByName(dependentTaskName)
      taskToFind.dependsOn(this)
    } catch (e: UnknownTaskException) {
      project!!.tasks.whenTaskAdded { dependentTask ->
        if (dependentTask.name == dependentTaskName) {
          dependentTask.dependsOn(this)
        }
      }
    }
  }

  private fun Task.runAfter(dependentTaskName: String) {
    try {
      val taskToFind = project!!.tasks.getByName(dependentTaskName)
      taskToFind.finalizedBy(this)
    } catch (e: UnknownTaskException) {
      project!!.tasks.whenTaskAdded { dependentTask ->
        if (dependentTask.name == dependentTaskName) {
          dependentTask.finalizedBy(this)
        }
      }
    }
  }

  private fun applyPlugin() {
    if (project != null) {
      val project = this.project!!
      // TODO: Create different tasks per variant
      val pullTapesTask: TapeTask =
          project.tasks.create(PullTapesTask.NAME, PullTapesTask::class.java)
      val pushTapesTask: TapeTask =
          project.tasks.create(PushTapesTask.NAME, PushTapesTask::class.java)
      project.afterEvaluate {
        getVariants().all {
          val flavorNameCapitalized = it.flavorName.capitalize()
          val buildNameCapitalized = it.buildType.name.capitalize()
          val targetName = "$flavorNameCapitalized$buildNameCapitalized"
          val globalScope = it.globalScope()
          val adbPath = globalScope.androidBuilder.sdkInfo.adb
          val adbTimeoutMs = globalScope.extension.adbOptions.timeOutInMs
          val testApplicationId = testApplicationId()
          val deviceBridge = DeviceBridgeProvider.get(adbPath, adbTimeoutMs, project)
          listOf(pullTapesTask, pushTapesTask).forEach {
            it.setDeviceBridge(deviceBridge)
            it.setPackageName(testApplicationId)
          }
          pushTapesTask.runBefore("connected${targetName}AndroidTest")
          pullTapesTask.runAfter("connected${targetName}AndroidTest")
        }
      }
    }
  }

  private fun BaseVariant.globalScope(): GlobalScope {
    val getVariantData = this.javaClass.getDeclaredMethod("getVariantData")
    getVariantData.isAccessible = true
    val variantData = getVariantData.invoke(this) as BaseVariantData<*>
    return variantData.scope.globalScope
  }

  private fun androidConfig(): AndroidConfig {
    return project!!.extensions.getByName("android") as BaseExtension
  }

  private fun testApplicationId(): String {
    val androidConfig = androidConfig()
    if (androidConfig is AppExtension || androidConfig is LibraryExtension) {
      return (androidConfig as TestedExtension).testVariants.first().applicationId
    } else {
      throw IllegalStateException("Invalid project type")
    }
  }

  private fun getVariants(): DefaultDomainObjectSet<out BaseVariant> {
    val androidConfig = androidConfig()
    if (androidConfig is AppExtension) {
      @Suppress("UNCHECKED_CAST")
      return androidConfig.applicationVariants as DefaultDomainObjectSet<BaseVariant>
    } else if (androidConfig is LibraryExtension) {
      return androidConfig.libraryVariants
    } else {
      throw IllegalStateException("Invalid project type")
    }
  }

  companion object {
    val TAPES_DIR = "src/androidTest/walkman/tapes"
  }
}
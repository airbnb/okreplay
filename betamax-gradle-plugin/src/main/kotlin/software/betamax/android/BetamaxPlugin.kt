package software.betamax.android

import com.android.build.gradle.*
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.scope.GlobalScope
import com.android.build.gradle.internal.variant.BaseVariantData
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.DefaultDomainObjectSet
import javax.inject.Inject

class BetamaxPlugin
@Inject constructor() : Plugin<Project> {
  var project: Project? = null

  override fun apply(project: Project) {
    this.project = project
    if (project.plugins.hasPlugin(AppPlugin::class.java)
        || project.plugins.hasPlugin(LibraryPlugin::class.java)) {
      applyBetamaxPlugin()
    } else {
      throw IllegalArgumentException("Betamax plugin couldn't be applied. "
          + "The Android or Library plugin must be configured first.")
    }
  }

  private fun applyBetamaxPlugin() {
    if (project != null) {
      val project = this.project!!
      val pullTapesTask =
          project.tasks.create(PullTapesTask.NAME, PullTapesTask::class.java)
      val pushTapesTask =
          project.tasks.create(PushTapesTask.NAME, PushTapesTask::class.java)
      project.afterEvaluate {
        getVariants().all {
          val globalScope = it.globalScope()
          val adbPath = globalScope.androidBuilder.sdkInfo.adb
          val adbTimeoutMs = globalScope.extension.adbOptions.timeOutInMs
          val applicationId = androidConfig().defaultConfig.applicationId
          pullTapesTask.adbPath = adbPath
          pullTapesTask.adbTimeoutMs = adbTimeoutMs
          pullTapesTask.packageName = applicationId
          pushTapesTask.adbPath = adbPath
          pushTapesTask.adbTimeoutMs = adbTimeoutMs
          pushTapesTask.packageName = applicationId
          val espressoTask = project.tasks.getByName("connectedAndroidTest")
          espressoTask.finalizedBy(pullTapesTask)
          espressoTask.dependsOn(pushTapesTask)
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
    val TAPES_DIR = "betamax/tapes"
  }
}
package software.betamax.android

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariant
import com.android.builder.sdk.SdkInfo
import org.gradle.api.*
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.tasks.Input

import javax.inject.Inject

class BetamaxPlugin implements Plugin<Project> {
  private Project project
  private final FileResolver fileResolver

  @Inject public BetamaxPlugin(FileResolver fileResolver) {
    this.fileResolver = fileResolver
  }

  @Override void apply(Project project) {
    this.project = project
    if (project.plugins.hasPlugin(AppPlugin) || project.plugins.hasPlugin(LibraryPlugin)) {
      applyBetamaxPlugin()
    } else {
      throw new IllegalArgumentException("Betamax plugin couldn't be applied. "
          + "The Android or Library plugin must be configured first.")
    }
  }

  private void applyBetamaxPlugin() {
    project.afterEvaluate {
      getVariants().all { v ->
        SdkInfo sdkInfo = v.variantData.scope.getGlobalScope().getAndroidBuilder().getSdkInfo()
        DefaultTask espressoTask = project.tasks.getByName("connectedAndroidTest")
        Task betamaxPullTapesTask =
            project.tasks.create(BetamaxPullTapesTask.NAME, BetamaxPullTapesTask.class)
        betamaxPullTapesTask.adbPath = sdkInfo.adb
        espressoTask.finalizedBy(betamaxPullTapesTask)
      }
    }
  }

  private DomainObjectCollection<BaseVariant> getVariants() {
    return project.android.applicationVariants ?: project.android.libraryVariants
  }
}
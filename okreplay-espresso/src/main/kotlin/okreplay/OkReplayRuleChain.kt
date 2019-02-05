package okreplay

import androidx.test.rule.ActivityTestRule
import org.junit.rules.RuleChain
import org.junit.rules.TestRule

class OkReplayRuleChain(
    private val configuration: OkReplayConfig,
    private val activityTestRule: ActivityTestRule<*>) {
  fun get(): TestRule {
    return RuleChain.outerRule(PermissionRule(configuration))
        .around(RecorderRule(configuration))
        .around(activityTestRule)
  }
}
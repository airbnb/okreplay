package walkman

import android.support.test.rule.ActivityTestRule
import org.junit.rules.RuleChain
import org.junit.rules.TestRule

class WalkmanRuleChain(
    private val configuration: WalkmanConfig,
    private val activityTestRule: ActivityTestRule<*>) {
  fun get(): TestRule {
    return RuleChain.outerRule(PermissionRule(configuration))
        .around(RecorderRule(configuration))
        .around(activityTestRule)
  }
}
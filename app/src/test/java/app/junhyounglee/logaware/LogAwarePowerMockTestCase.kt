package app.junhyounglee.logaware

import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.powermock.modules.junit4.rule.PowerMockRule
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
@Config(
    shadows = [ShadowLog::class],
    manifest = Config.NONE,
    sdk = [21]
)
open class LogAwarePowerMockTestCase {
    @get:Rule
    val rule = PowerMockRule()

    @Before
    open fun onBeforeTest() {
        ShadowLog.stream = System.out
        MockitoAnnotations.initMocks(this)
    }
}
package app.junhyounglee.logaware

import android.util.Log
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.ArgumentMatchers.*
import org.mockito.stubbing.Answer
import org.powermock.api.mockito.PowerMockito.`when`
import org.powermock.api.mockito.PowerMockito.mockStatic
import org.powermock.core.classloader.annotations.PrepareForTest
import org.robolectric.shadows.ShadowLog
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/**
 * ./gradlew test -i
 */
@PrepareForTest(Log::class)
class LogAwareTest : LogAwarePowerMockTestCase() {

    override fun onBeforeTest() {
        super.onBeforeTest()
        mockStatic(Log::class.java)
    }

    @Test
    fun testLogAware() {
        //below code is same as: val logger = LogAware(LogAwareTest::class.java)
        val logger = LogAware<LogAwareTest>()

        // stub: Log.isLoggable
        `when`(Log.isLoggable(anyString(), anyInt())).thenAnswer(Answer<Boolean> {
            val tag = it.arguments[0] as String
            val level = it.arguments[1] as Int
            //level == Log.INFO
            true
        })
        //`when`(Log.isLoggable(anyString(), anyInt())).thenReturn(true)

        val bos = ByteArrayOutputStream(512)
        val old = ShadowLog.stream

        // stub: Log.INFO
        `when`(Log.i(anyString(), anyString(), any())).thenAnswer(Answer {
            val msg = it.arguments[1] as String
            //ShadowLog.stream.println(msg)
            //println(msg)
            bos.write(msg.toByteArray())
            1
        })

        try {
            ShadowLog.stream = PrintStream(old)

            logger.i("Log information")

            val message = bos.toString()
            assertEquals("Log information", message)
        } finally {
            ShadowLog.stream = old
        }
    }

    companion object {
        private const val TAG = "LogAwareTest"
    }
}
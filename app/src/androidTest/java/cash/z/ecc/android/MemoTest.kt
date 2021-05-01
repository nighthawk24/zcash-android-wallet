package cash.z.ecc.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import cash.z.ecc.android.ui.util.MemoUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(AndroidJUnit4::class)
// @RunWith(Parameterized::class)
class MemoTest(val input: String, val output: String) {

    @Test
    fun testExtractValidAddress() = runBlocking {
        val result = MemoUtil.firstValidAddress(input, ::validateMemo)
        assertEquals(output, result)
    }

    suspend fun validateMemo(memo: String): Boolean {
        delay(20)
        return true
    }

    companion object {
        val validTaddr = "tmWGKMEpxSUf97H12MmGtgiER1drVbGjzWM"
        val validZaddr = "ztestsapling1ukadr59p0hxcl2pq8mfagnfx3h74nsusdkm59gkys7hxze92whxj54mfdn3n37zusum7w4jlj35"
        val invalidAddr = "ztestsaplinn9ukadr59p0hxcl2pq8mfagnfx3h74nsusdkm59gkys7hxze92whxj54mfdn3n37zusum7w4jlj35"

        @JvmStatic
        @Parameterized.Parameters
        fun data() = listOf(
            arrayOf(
                "thanks for the food reply-to: $validZaddr",
                validZaddr
            ),
            arrayOf(
                "thanks for the food reply-to: $validTaddr",
                validTaddr
            )
        )
    }
}

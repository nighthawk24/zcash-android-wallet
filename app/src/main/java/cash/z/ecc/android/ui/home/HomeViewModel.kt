package cash.z.ecc.android.ui.home

import androidx.lifecycle.ViewModel
import cash.z.ecc.android.R
import cash.z.ecc.android.ext.toAppString
import cash.z.ecc.android.sdk.SdkSynchronizer
import cash.z.ecc.android.sdk.Synchronizer
import cash.z.ecc.android.sdk.Synchronizer.Status.DISCONNECTED
import cash.z.ecc.android.sdk.Synchronizer.Status.DOWNLOADING
import cash.z.ecc.android.sdk.Synchronizer.Status.SCANNING
import cash.z.ecc.android.sdk.Synchronizer.Status.SYNCED
import cash.z.ecc.android.sdk.Synchronizer.Status.VALIDATING
import cash.z.ecc.android.sdk.block.CompactBlockProcessor
import cash.z.ecc.android.sdk.exception.RustLayerException
import cash.z.ecc.android.sdk.ext.ZcashSdk.MINERS_FEE_ZATOSHI
import cash.z.ecc.android.sdk.ext.ZcashSdk.ZATOSHI_PER_ZEC
import cash.z.ecc.android.sdk.ext.twig
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scan
import javax.inject.Inject
import kotlin.math.roundToInt

class HomeViewModel @Inject constructor() : ViewModel() {

    @Inject
    lateinit var synchronizer: Synchronizer

    lateinit var uiModels: Flow<UiModel>

    lateinit var _typedChars: ConflatedBroadcastChannel<Char>

    var initialized = false

    fun initializeMaybe(preTypedChars: String = "0") {
        twig("init called")
        if (initialized) {
            twig("Warning already initialized HomeViewModel. Ignoring call to initialize.")
            return
        }

        if (::_typedChars.isInitialized) {
            _typedChars.close()
        }
        _typedChars = ConflatedBroadcastChannel()
        val typedChars = _typedChars.asFlow()
        val decimal = '.' // R.string.key_decimal.toAppString()[0]
        val backspace = R.string.key_backspace.toAppString()[0]
        val zec = typedChars.scan(preTypedChars) { acc, c ->
            when {
                // no-op cases
                acc == "0" && c == '0' ||
                    (c == backspace && acc == "0")
                    || (c == decimal && acc.contains(decimal)) -> {
                    acc
                }
                c == backspace && acc.length <= 1 -> {
                    "0"
                }
                c == backspace -> {
                    acc.substring(0, acc.length - 1)
                }
                acc == "0" && c != decimal -> {
                    c.toString()
                }
                acc.contains(decimal) && acc.length - acc.indexOf(decimal) > 8 -> {
                    acc
                }
                else -> {
                    "$acc$c"
                }
            }
        }
        twig("initializing view models stream")
        uiModels = synchronizer.run {
            combine(status, processorInfo, balances, zec) { s, p, b, z ->
                UiModel(s, p, b.availableZatoshi, b.totalZatoshi, z)
            }.onStart { emit(UiModel()) }
        }.conflate()
    }

    override fun onCleared() {
        super.onCleared()
        twig("HomeViewModel cleared!")
    }

    suspend fun onChar(c: Char) {
        _typedChars.send(c)
    }

    suspend fun refreshBalance() {
        try {
            (synchronizer as SdkSynchronizer).refreshBalance()
        } catch (e: RustLayerException.BalanceException) {
            twig("Balance refresh failed. This is probably caused by a critical error but we'll give the app a chance to try to recover.")
        }
    }

    data class UiModel( // <- THIS ERROR IS AN IDE BUG WITH PARCELIZE
        val status: Synchronizer.Status = DISCONNECTED,
        val processorInfo: CompactBlockProcessor.ProcessorInfo = CompactBlockProcessor.ProcessorInfo(),
        val availableBalance: Long = -1L,
        val totalBalance: Long = -1L,
        val pendingSend: String = "0"
    ) {
        // Note: the wallet is effectively empty if it cannot cover the miner's fee
        val hasFunds: Boolean get() = availableBalance > (MINERS_FEE_ZATOSHI.toDouble() / ZATOSHI_PER_ZEC) // 0.00001
        val hasBalance: Boolean get() = totalBalance > 0
        val isSynced: Boolean get() = status == SYNCED
        val isSendEnabled: Boolean get() = isSynced && hasFunds

        // Processor Info
        val isDownloading = status == DOWNLOADING
        val isScanning = status == SCANNING
        val isValidating = status == VALIDATING
        val isDisconnected = status == DISCONNECTED
        val downloadProgress: Int get() {
            return processorInfo.run {
                if (lastDownloadRange.isEmpty()) {
                    100
                } else {
                    val progress =
                        (((lastDownloadedHeight - lastDownloadRange.first + 1).coerceAtLeast(0).toFloat() / (lastDownloadRange.last - lastDownloadRange.first + 1)) * 100.0f).coerceAtMost(
                            100.0f
                        ).roundToInt()
                    progress
                }
            }
        }
        val scanProgress: Int get() {
            return processorInfo.run {
                if (lastScanRange.isEmpty()) {
                    100
                } else {
                    val progress = (((lastScannedHeight - lastScanRange.first + 1).coerceAtLeast(0).toFloat() / (lastScanRange.last - lastScanRange.first + 1)) * 100.0f).coerceAtMost(100.0f).roundToInt()
                    progress
                }
            }
        }
        val totalProgress: Float get() {
            val downloadWeighted = 0.40f * (downloadProgress.toFloat() / 100.0f).coerceAtMost(1.0f)
            val scanWeighted = 0.60f * (scanProgress.toFloat() / 100.0f).coerceAtMost(1.0f)
            return downloadWeighted.coerceAtLeast(0.0f) + scanWeighted.coerceAtLeast(0.0f)
        }
    }
}

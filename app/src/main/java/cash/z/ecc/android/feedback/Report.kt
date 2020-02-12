package cash.z.ecc.android.feedback

import cash.z.ecc.android.ZcashWalletApp

object Report {
    object Send {
        class SubmitFailure(private val errorCode: Int?, private val errorMessage: String?) : Feedback.Funnel("send.failure.submit") {
            override fun toMap(): MutableMap<String, Any> {
                return super.toMap().apply {
                    put("error.code", errorCode ?: -1)
                    put("error.message", errorMessage ?: "None")
                }
            }
        }

        class EncodingFailure(private val errorCode: Int?, private val errorMessage: String?) : Feedback.Funnel("send.failure.submit") {
            override fun toMap(): MutableMap<String, Any> {
                return super.toMap().apply {
                    put("error.code", errorCode ?: -1)
                    put("error.message", errorMessage ?: "None")
                }
            }
        }

    }

    enum class NonUserAction(override val key: String, val description: String) : Feedback.Action {
        FEEDBACK_STARTED("action.feedback.start", "feedback started"),
        FEEDBACK_STOPPED("action.feedback.stop", "feedback stopped"),
        SYNC_START("action.feedback.synchronizer.start", "sync started");

        override fun toString(): String = description
    }

    enum class MetricType(override val key: String, val description: String) : Feedback.Action {
        ENTROPY_CREATED("metric.entropy.created", "entropy created"),
        SEED_CREATED("metric.seed.created", "seed created"),
        SEED_IMPORTED("metric.seed.imported", "seed imported"),
        SEED_PHRASE_CREATED("metric.seedphrase.created", "seed phrase created"),
        SEED_PHRASE_LOADED("metric.seedphrase.loaded", "seed phrase loaded"),
        WALLET_CREATED("metric.wallet.created", "wallet created"),
        WALLET_IMPORTED("metric.wallet.imported", "wallet imported"),
        ACCOUNT_CREATED("metric.account.created", "account created"),

        // Transactions
        TRANSACTION_INITIALIZED("metric.tx.initialized", "transaction initialized"),
        TRANSACTION_CREATED("metric.tx.created", "transaction created successfully"),
        TRANSACTION_SUBMITTED("metric.tx.submitted", "transaction submitted successfully"),
        TRANSACTION_MINED("metric.tx.mined", "transaction mined")
    }
}

/**
 * Creates a metric with a start time of ZcashWalletApp.creationTime and an end time of when this
 * instance was created. This can then be passed to [Feedback.report].
 */
class LaunchMetric private constructor(private val metric: Feedback.TimeMetric) :
    Feedback.Metric by metric {
    constructor() : this(
        Feedback
            .TimeMetric(
                "metric.app.launch",
                "app launched",
                mutableListOf(ZcashWalletApp.instance.creationTime)
            )
            .markTime()
    )
    override fun toString(): String = metric.toString()
}

inline fun <T> Feedback.measure(type: Report.MetricType, block: () -> T): T =
    this.measure(type.key, type.description, block)
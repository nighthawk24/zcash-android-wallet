package cash.z.ecc.android.ui.detail

import androidx.lifecycle.ViewModel
import cash.z.wallet.sdk.Synchronizer
import cash.z.wallet.sdk.ext.twig
import javax.inject.Inject

class WalletDetailViewModel @Inject constructor() : ViewModel() {

    @Inject
    lateinit var synchronizer: Synchronizer

    val transactions get() = synchronizer.clearedTransactions

    override fun onCleared() {
        super.onCleared()
        twig("WalletDetailViewModel cleared!")
    }
}

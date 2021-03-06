package cash.z.ecc.android.ui.send

import android.content.ClipboardManager
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.viewModelScope
import cash.z.ecc.android.R
import cash.z.ecc.android.databinding.FragmentSendBinding
import cash.z.ecc.android.di.viewmodel.activityViewModel
import cash.z.ecc.android.ext.*
import cash.z.ecc.android.feedback.Report
import cash.z.ecc.android.feedback.Report.Tap.*
import cash.z.ecc.android.sdk.block.CompactBlockProcessor.WalletBalance
import cash.z.ecc.android.sdk.ext.*
import cash.z.ecc.android.sdk.validate.AddressType
import cash.z.ecc.android.ui.base.BaseFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SendFragment : BaseFragment<FragmentSendBinding>(),
    ClipboardManager.OnPrimaryClipChangedListener {
    override val screen = Report.Screen.SEND_ADDRESS

    private var maxZatoshi: Long? = null
    private var availableZatoshi: Long? = null

    val sendViewModel: SendViewModel by activityViewModel()

    override fun inflate(inflater: LayoutInflater): FragmentSendBinding =
        FragmentSendBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Apply View Model
        applyViewModel(sendViewModel)


        // Apply behaviors

        binding.buttonSend.setOnClickListener {
            onSubmit().also { tapped(SEND_SUBMIT) }
        }

        binding.checkIncludeAddress.setOnCheckedChangeListener { _, _->
            onIncludeMemo(binding.checkIncludeAddress.isChecked)
        }

        binding.inputZcashAddress.apply {
            doAfterTextChanged {
                val textStr = text.toString()
                val trim = textStr.trim()
                // bugfix: prevent cursor from moving while backspacing and deleting whitespace
                if (text.toString() != trim) {
                    setText(trim)
                    setSelection(selectionEnd - (textStr.length - trim.length))
                }
                onAddressChanged(trim)
            }
        }

        binding.backButtonHitArea.onClickNavUp { tapped(SEND_ADDRESS_BACK) }
//
//        binding.clearMemo.setOnClickListener {
//            onClearMemo().also { tapped(SEND_MEMO_CLEAR) }
//        }

        binding.inputZcashMemo.doAfterTextChanged {
            sendViewModel.memo = binding.inputZcashMemo.text?.toString() ?: ""
            onMemoUpdated()
        }

        binding.textLayoutAddress.setEndIconOnClickListener {
            mainActivity?.maybeOpenScan().also { tapped(SEND_ADDRESS_SCAN) }
        }

        // banners

        binding.backgroundClipboard.setOnClickListener {
            onPaste().also { tapped(SEND_ADDRESS_PASTE) }
        }
        binding.containerClipboard.setOnClickListener {
            onPaste().also { tapped(SEND_ADDRESS_PASTE) }
        }
        binding.backgroundLastUsed.setOnClickListener {
            onReuse().also { tapped(SEND_ADDRESS_REUSE) }
        }
        binding.containerLastUsed.setOnClickListener {
            onReuse().also { tapped(SEND_ADDRESS_REUSE) }
        }
    }

    private fun applyViewModel(model: SendViewModel) {
        // apply amount
        val roundedAmount =
            model.zatoshiAmount.coerceAtLeast(0L).convertZatoshiToZecStringUniform(8)
        binding.textSendAmount.text = "\$$roundedAmount"
        // apply address
        binding.inputZcashAddress.setText(model.toAddress)
        // apply memo
        binding.inputZcashMemo.setText(model.memo)
        binding.checkIncludeAddress.isChecked = model.includeFromAddress
        onMemoUpdated()
    }

    private fun onMemoUpdated() {
        val totalLength = sendViewModel.createMemoToSend().length
        binding.textLayoutMemo.helperText = "$totalLength/${ZcashSdk.MAX_MEMO_SIZE} chars"
        val color = if (totalLength > ZcashSdk.MAX_MEMO_SIZE) R.color.zcashRed else R.color.text_light_dimmed
        binding.textLayoutMemo.setHelperTextColor(ColorStateList.valueOf(color.toAppColor()))
    }

    private fun onClearMemo() {
        binding.inputZcashMemo.setText("")
    }

    private fun onIncludeMemo(checked: Boolean) {
        sendViewModel.afterInitFromAddress {
            sendViewModel.includeFromAddress = checked
            onMemoUpdated()
            tapped(if (checked) SEND_MEMO_INCLUDE else SEND_MEMO_EXCLUDE)
        }
    }

    private fun onAddressChanged(address: String) {
        resumedScope.launch {
            val validation = sendViewModel.validateAddress(address)
            binding.buttonSend.isActivated = !validation.isNotValid
            var type = when (validation) {
                is AddressType.Transparent -> "This is a valid transparent address" to R.color.zcashGreen
                is AddressType.Shielded -> "This is a valid shielded address" to R.color.zcashGreen
                is AddressType.Invalid -> "This address appears to be invalid" to R.color.zcashRed
            }
            if (address == sendViewModel.synchronizer.getAddress()) type =
                "Warning, this appears to be your address!" to R.color.zcashRed
            binding.textLayoutAddress.helperText = type.first
            binding.textLayoutAddress.setHelperTextColor(ColorStateList.valueOf(type.second.toAppColor()))

            // if we have the clipboard address but we're changing it, then clear the selection
            if (binding.imageClipboardAddressSelected.isVisible) {
                loadAddressFromClipboard().let { clipboardAddress ->
                    if (address != clipboardAddress) {
                        updateClipboardBanner(false, clipboardAddress)
                    }
                }
            }
            // if we have the last used address but we're changing it, then clear the selection
            if (binding.imageLastUsedAddressSelected.isVisible) {
                loadLastUsedAddress().let { lastAddress ->
                    if (address != lastAddress) {
                        updateLastUsedBanner(false, lastAddress)
                    }
                }
            }
        }
    }


    private fun onSubmit(unused: EditText? = null) {
        sendViewModel.toAddress = binding.inputZcashAddress.text.toString()
        sendViewModel.validate(availableZatoshi, maxZatoshi).onFirstWith(resumedScope) { errorMessage ->
            if (errorMessage == null) {
                mainActivity?.authenticate("Please confirm that you want to send ${sendViewModel.zatoshiAmount.convertZatoshiToZecString(8)} ZEC to\n${sendViewModel.toAddress.toAbbreviatedAddress()}") {
//                    sendViewModel.funnel(Send.AddressPageComplete)
                    mainActivity?.safeNavigate(R.id.action_nav_send_to_nav_send_final)
                }
            } else {
                resumedScope.launch {
                    binding.textAddressError.text = errorMessage
                    delay(2500L)
                    binding.textAddressError.text = ""
                }
            }
        }
    }

    private fun onMax() {
        if (maxZatoshi != null) {
//            binding.inputZcashAmount.apply {
//                setText(maxZatoshi.convertZatoshiToZecString(8))
//                postDelayed({
//                    requestFocus()
//                    setSelection(text?.length ?: 0)
//                }, 10L)
//            }
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity?.clipboard?.addPrimaryClipChangedListener(this)
    }

    override fun onDetach() {
        super.onDetach()
        mainActivity?.clipboard?.removePrimaryClipChangedListener(this)
    }

    override fun onResume() {
        super.onResume()
        updateClipboardBanner()
        updateLastUsedBanner()
        sendViewModel.synchronizer.balances.collectWith(resumedScope) {
            onBalanceUpdated(it)
        }
        binding.inputZcashAddress.text.toString().let {
            if (!it.isNullOrEmpty()) onAddressChanged(it)
        }
    }

    private fun onBalanceUpdated(balance: WalletBalance) {
//        binding.textLayoutAmount.helperText =
//            "You have ${balance.availableZatoshi.coerceAtLeast(0L).convertZatoshiToZecString(8)} available"
        maxZatoshi = (balance.availableZatoshi - ZcashSdk.MINERS_FEE_ZATOSHI).coerceAtLeast(0L)
        availableZatoshi = balance.availableZatoshi
    }

    override fun onPrimaryClipChanged() {
        twig("clipboard changed!")
        updateClipboardBanner()
        updateLastUsedBanner()
    }

    private fun updateClipboardBanner(selected: Boolean = false, address: String? = loadAddressFromClipboard()) {
        binding.apply {
            updateAddressBanner(
                groupClipboard,
                clipboardAddress,
                imageClipboardAddressSelected,
                imageShield,
                clipboardAddressLabel,
                selected,
                address
            )
        }
//        binding.dividerClipboard.text = "On Clipboard"
    }

    private fun updateLastUsedBanner(
        selected: Boolean = false,
        address: String? = loadLastUsedAddress()
    ) {
        val isBoth = address == loadAddressFromClipboard()
        binding.apply {
            updateAddressBanner(
                groupLastUsed,
                lastUsedAddress,
                imageLastUsedAddressSelected,
                imageLastUsedShield,
                lastUsedAddressLabel,
                selected,
                address.takeUnless { isBoth })
        }
        binding.dividerClipboard.text = if (isBoth) "Last Used and On Clipboard" else "On Clipboard"
    }

    private fun updateAddressBanner(
        group: Group,
        addressTextView: TextView,
        checkIcon: ImageView,
        shieldIcon: ImageView,
        addressLabel: TextView,
        selected: Boolean = false,
        address: String? = loadLastUsedAddress()
    ) {
        resumedScope.launch {
            if (address == null) {
                group.gone()
            } else {
                val userAddress = sendViewModel.synchronizer.getAddress()
                group.visible()
                addressTextView.text = address.toAbbreviatedAddress(16, 16)
                checkIcon.goneIf(!selected)
                ImageViewCompat.setImageTintList(shieldIcon, ColorStateList.valueOf(if (selected) R.color.colorPrimary.toAppColor() else R.color.zcashWhite_12.toAppColor()))
                addressLabel.setText(if (address == userAddress) R.string.send_banner_address_user else R.string.send_banner_address_unknown)
                addressLabel.setTextColor(if(selected) R.color.colorPrimary.toAppColor() else R.color.text_light.toAppColor())
                addressTextView.setTextColor(if(selected) R.color.text_light.toAppColor() else R.color.text_light_dimmed.toAppColor())
            }
        }
    }

    private fun onPaste() {
        mainActivity?.clipboard?.let { clipboard ->
            if (clipboard.hasPrimaryClip()) {
                val address = clipboard.text().toString()
                val applyValue = binding.imageClipboardAddressSelected.isGone
                updateClipboardBanner(applyValue, address)
                binding.inputZcashAddress.setText(address.takeUnless { !applyValue })
            }
        }
    }

    private fun onReuse() {
        val address = loadLastUsedAddress()
        val applyValue = binding.imageLastUsedAddressSelected.isGone
        updateLastUsedBanner(applyValue, address)
        binding.inputZcashAddress.setText(address.takeUnless { !applyValue })
    }

    private fun loadAddressFromClipboard(): String? {
        mainActivity?.clipboard?.apply {
            if (hasPrimaryClip()) {
                text()?.let { text ->
                    if (text.startsWith("zs") && text.length > 70) {
                        return@loadAddressFromClipboard text.toString()
                    }
                    // treat t-addrs differently in the future
                    if (text.startsWith("t1") && text.length > 32) {
                        return@loadAddressFromClipboard text.toString()
                    }
                }
            }
        }
        return null
    }

    private var lastUsedAddress: String? = null
    private fun loadLastUsedAddress(): String? {
        if (lastUsedAddress == null) sendViewModel.viewModelScope.launch {
            lastUsedAddress = sendViewModel.synchronizer.sentTransactions.first().firstOrNull { !it.toAddress.isNullOrEmpty() }?.toAddress
            updateLastUsedBanner(binding.imageLastUsedAddressSelected.isVisible, lastUsedAddress)
        }
        return lastUsedAddress
    }


    private fun ClipboardManager.text(): CharSequence =
        primaryClip!!.getItemAt(0).coerceToText(mainActivity)
}
package cash.z.ecc.android.ui.receive

import android.content.Context
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import cash.z.android.qrecycler.QRecycler
import cash.z.ecc.android.databinding.FragmentReceiveNewBinding
import cash.z.ecc.android.di.viewmodel.viewModel
import cash.z.ecc.android.ext.distribute
import cash.z.ecc.android.ext.onClickNavBack
import cash.z.ecc.android.feedback.Report
import cash.z.ecc.android.feedback.Report.Tap.RECEIVE_BACK
import cash.z.ecc.android.sdk.ext.twig
import cash.z.ecc.android.ui.base.BaseFragment
import cash.z.ecc.android.ui.util.AddressPartNumberSpan
import kotlinx.coroutines.launch

class ReceiveFragment : BaseFragment<FragmentReceiveNewBinding>() {
    override val screen = Report.Screen.RECEIVE

    private val viewModel: ReceiveViewModel by viewModel()

    lateinit var qrecycler: QRecycler

    lateinit var addressParts: Array<TextView>

    override fun inflate(inflater: LayoutInflater): FragmentReceiveNewBinding =
        FragmentReceiveNewBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addressParts = arrayOf(
            binding.textAddressPart1,
            binding.textAddressPart2,
            binding.textAddressPart3,
            binding.textAddressPart4,
            binding.textAddressPart5,
            binding.textAddressPart6,
            binding.textAddressPart7,
            binding.textAddressPart8
        )

        binding.backButtonHitArea.onClickNavBack() { tapped(RECEIVE_BACK) }
    }

    override fun onAttach(context: Context) {
        qrecycler = QRecycler() // inject! :)
        super.onAttach(context)
    }

    override fun onResume() {
        super.onResume()
        resumedScope.launch {
            onAddressLoaded(viewModel.getAddress())
        }
    }

    private fun onAddressLoaded(address: String) {
        twig("address loaded:  $address length: ${address.length}")
        qrecycler.load(address)
            .withQuietZoneSize(3)
            .withCorrectionLevel(QRecycler.CorrectionLevel.MEDIUM)
            .into(binding.receiveQrCode)

        address.distribute(8) { i, part ->
            setAddressPart(i, part)
        }
    }

    private fun setAddressPart(index: Int, addressPart: String) {
        twig("setting address for part $index) $addressPart")
        val thinSpace = "\u2005" // 0.25 em space
        val textSpan = SpannableString("${index + 1}$thinSpace$addressPart")

        textSpan.setSpan(AddressPartNumberSpan(), 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        addressParts[index].text = textSpan
    }
}

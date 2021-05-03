package cash.z.ecc.android.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import cash.z.ecc.android.feedback.Report
import cash.z.ecc.android.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

abstract class BaseFragment<T : ViewBinding> : Fragment() {
    val mainActivity: MainActivity? get() = activity as MainActivity?

    lateinit var binding: T

    lateinit var resumedScope: CoroutineScope

    open val screen: Report.Screen? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = inflate(inflater)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        mainActivity?.reportScreen(screen)
        resumedScope = lifecycleScope.coroutineContext.let {
            CoroutineScope(Dispatchers.Main + SupervisorJob(it[Job]))
        }
    }

    override fun onPause() {
        super.onPause()
        resumedScope.cancel()
    }

    // inflate is static in the ViewBinding class so we can't handle this ourselves
    // each fragment must call FragmentMyLayoutBinding.inflate(inflater)
    abstract fun inflate(@NonNull inflater: LayoutInflater): T

    fun onBackPressNavTo(navResId: Int, block: (() -> Unit) = {}) {
        mainActivity?.onFragmentBackPressed(this) {
            block()
            mainActivity?.safeNavigate(navResId)
        }
    }

    fun tapped(tap: Report.Tap) {
        mainActivity?.reportTap(tap)
    }

    /**
     * Launch the given block once, within the 'resumedScope', once the Synchronizer is ready. This
     * utility function helps solve the problem of taking action with the synchronizer before it
     * is created. This surfaced while loading keys from secure storage: the HomeFragment would
     * resume and start monitoring the synchronizer for changes BEFORE the onAttach function
     * returned, meaning before the synchronizerComponent is created. So a state variable needed to
     * exist with a longer lifecycle than the synchronizer. This function just takes care of all the
     * boilerplate of monitoring that state variable until it returns true.
     */
    fun launchWhenSyncReady(block: () -> Unit) {
        resumedScope.launch {
            mainActivity?.let {
                it.mainViewModel.syncReady.filter { isReady -> isReady }.onEach {
                    block()
                }.first()
            }
        }
    }
}

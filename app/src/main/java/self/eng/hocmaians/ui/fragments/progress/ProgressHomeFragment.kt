package self.eng.hocmaians.ui.fragments.progress

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import self.eng.hocmaians.R
import self.eng.hocmaians.databinding.FragmentProgressHomeBinding

class ProgressHomeFragment : Fragment(R.layout.fragment_progress_home) {

    // view binding
    private var _binding: FragmentProgressHomeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProgressHomeBinding.bind(view)

        binding.apply {
            val changeTabAdapter = ProgressHomeChangeTabAdapter(this@ProgressHomeFragment)
            vpProgressHome.adapter = changeTabAdapter

            val tabLayoutMediator = TabLayoutMediator(
                progressHomeTabLayout,
                vpProgressHome
            ) { tab, position ->
                when (position) {
                    0 -> tab.text = getString(R.string.detail_score_tab)
                    1 -> tab.text = getString(R.string.overall_tab)
                }
            }
            tabLayoutMediator.attach()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
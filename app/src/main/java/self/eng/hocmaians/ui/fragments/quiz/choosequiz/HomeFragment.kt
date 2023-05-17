package self.eng.hocmaians.ui.fragments.quiz.choosequiz

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import self.eng.hocmaians.R
import self.eng.hocmaians.databinding.FragmentHomeBinding

/**
 * Holder Fragment sở hữu có chứa Chọn câu đố theo chủ đề và Chọn fragment câu đố hỗn hợp.
 * ViewPager2 chịu trách nhiệm xử lý 2 fragment đó.
 */
class HomeFragment : Fragment(R.layout.fragment_home) {

    // view binding
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        binding.apply {
            val changeTabAdapter = HomeChangeTabAdapter(this@HomeFragment)
            homeTabLayoutViewPager.adapter = changeTabAdapter

            val tabLayoutMediator = TabLayoutMediator(
                homeTabLayout,
                homeTabLayoutViewPager
            ) { tab, position ->
                when (position) {
                    0 -> tab.text = getString(R.string.choose_test_by_topic_tab)
                    1 -> tab.text = getString(R.string.choose_mixed_test_tab)
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
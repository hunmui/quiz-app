package self.eng.hocmaians.ui.fragments.progress

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import self.eng.hocmaians.util.Constants.PROGRESS_HOME_TAB_LAYOUT_NUMBER

/**
 * ViewPager2 Adapter, handling changing 2 tabs: Detail Score and Overall Fragment
 *
 * @param fragment host fragment which contains the viewPager2 (ProgressHomeFragment)
 */
class ProgressHomeChangeTabAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = PROGRESS_HOME_TAB_LAYOUT_NUMBER

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> DetailScoreFragment()
        1 -> OverallFragment()
        else -> DetailScoreFragment()
    }
}
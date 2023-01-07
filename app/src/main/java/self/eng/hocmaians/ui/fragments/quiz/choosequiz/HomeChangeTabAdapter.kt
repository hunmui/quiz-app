package self.eng.hocmaians.ui.fragments.quiz.choosequiz

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import self.eng.hocmaians.util.Constants.HOME_TAB_LAYOUT_NUMBER

/**
 * ViewPager2 Adapter, handling changing 2 tabs: Choose Quiz By Topic and Choose Mixed Quiz Fragment
 *
 * @param fragment host fragment which contains the viewPager2 (HomeFragment)
 */
class HomeChangeTabAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = HOME_TAB_LAYOUT_NUMBER

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> ChooseQuizByTopicFragment()
        1 -> ChooseMixedQuizFragment()
        else -> ChooseQuizByTopicFragment()
    }
}
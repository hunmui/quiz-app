package self.eng.hocmaians.ui.fragments.quiz.choosequiz

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import self.eng.hocmaians.util.Constants.HOME_TAB_LAYOUT_NUMBER

/**
 * ViewPager2 Adapter, xử lý thay đổi 2 tab: Choose Quiz By Topic và Choose Mixed Quiz Fragment
 *
 * @param Fragment Host Fragment chứa viewPager2 (HomeFragment)
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
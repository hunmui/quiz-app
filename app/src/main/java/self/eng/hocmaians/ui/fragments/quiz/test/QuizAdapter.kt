package self.eng.hocmaians.ui.fragments.quiz.test

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import self.eng.hocmaians.data.entities.Question

class QuizAdapter(
        fragment: Fragment,
        private val questions: List<Question>
) : FragmentStateAdapter(fragment) {

    // radio buttons click listener
    private var answerForQuestion: ((Int, Int) -> Unit)? = null

    fun returnAnswerToHostFragment(listener: (Int, Int) -> Unit) {
        answerForQuestion = listener
    }

    override fun getItemCount(): Int = questions.size

    override fun createFragment(position: Int): Fragment {
        val fragment = EachQuizFragment()
        fragment.init(currentQuestion = questions[position], pos = position)

        fragment.setOnRadioButtonClickListener { answerNumber, questionPosition ->
            this@QuizAdapter.answerForQuestion?.let {
                it(answerNumber, questionPosition)
            }
        }

        return fragment
    }
}
package self.eng.hocmaians.ui.fragments.quiz.result

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import self.eng.hocmaians.QuizApplication
import self.eng.hocmaians.R
import self.eng.hocmaians.databinding.EachAnswerReviewBinding
import self.eng.hocmaians.data.entities.Question
import self.eng.hocmaians.data.entities.realtions.AnswerAndQuestion
import self.eng.hocmaians.util.CommonMethods
import self.eng.hocmaians.util.Constants
import self.eng.hocmaians.util.Constants.QUESTION_BOOKMARKED

// have to have this variable to handle recycler view re-render correctly
private var reRenderPos: Int = -1

class ReviewAnswerAdapter(
        private val whichFragment: Int
) : RecyclerView.Adapter<ReviewAnswerAdapter.ReviewAnswerViewHolder>() {

    private val differCallback = object : DiffUtil.ItemCallback<AnswerAndQuestion>() {
        override fun areItemsTheSame(
                oldItem: AnswerAndQuestion,
                newItem: AnswerAndQuestion
        ): Boolean = (oldItem.userAnswer.uaId == newItem.userAnswer.uaId)

        override fun areContentsTheSame(
                oldItem: AnswerAndQuestion,
                newItem: AnswerAndQuestion
        ): Boolean = (oldItem == newItem)
    }

    val differ: AsyncListDiffer<AnswerAndQuestion> = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewAnswerViewHolder {
        val binding = EachAnswerReviewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
        )
        return ReviewAnswerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewAnswerViewHolder, position: Int) {
        val userAnswer = differ.currentList[position].userAnswer.answerNumber
        val question = differ.currentList[position].question
        val questionNumber = position + 1
        holder.bind(questionNumber, userAnswer, question)
    }

    override fun getItemCount(): Int = differ.currentList.size

    /* ----------------- Bookmark click listener ----------------- */
    private var onBookmarkClickListener: ((Boolean, Int) -> Unit)? = null

    fun setOnBookmarkClickListener(listener: (Boolean, Int) -> Unit) {
        onBookmarkClickListener = listener
    }

    inner class ReviewAnswerViewHolder(
            private val binding: EachAnswerReviewBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Chế độ xem ràng buộc
         *
         * @param questionNumber số câu hỏi mà con người có thể đọc (có nghĩa là bắt đầu từ 1)
         * @param userAnswer câu trả lời của người dùng ở dạng số nguyên
         * @param question câu hỏi cho toàn bộ đối tượng câu hỏi
         */
        fun bind(questionNumber: Int, userAnswer: Int, question: Question) {
            binding.apply {

                var showExplanation = true

                // nếu re-render chính xác vị trí câu hỏi thì hiện giải thích
                // khác ẩn lời giải thích
                if (questionNumber - 1 == reRenderPos && showExplanation) {
                    explainLayout.visibility = View.VISIBLE
                } else {
                    showExplanation = false
                    explainLayout.visibility = View.GONE
                }

                // hiện HOẶC ẩn phần giải thích
                headerLayout.setOnClickListener {
                    showExplanation = !showExplanation
                    if (showExplanation) {
                        explainLayout.visibility = View.VISIBLE
                    } else {
                        explainLayout.visibility = View.GONE
                    }
                }

                /* ------------------ Header part ------------------ */
                tvQuestionNumber.text = questionNumber.toString()
                tvUserAnswer.text = CommonMethods.convertIndexToText(userAnswer)

                when (userAnswer) {
                    question.answerNr -> {
                        tvUserAnswer.setTextColor(Color.GREEN)
                        tvCorrectAnswer.text = ""
                    }
                    else -> {
                        tvUserAnswer.setTextColor(Color.RED)
                        tvCorrectAnswer.text = CommonMethods.convertIndexToText(question.answerNr)
                        tvCorrectAnswer.setTextColor(Color.GREEN)
                    }
                }

                /* ------------------ Explanation part ------------------ */
                var isBookmarked: Boolean

                if (whichFragment == Constants.GRAPH_FRAGMENT) {
                    // nếu đến từ đoạn biểu đồ, chỉ hiển thị trạng thái dấu trang
                    ivReviewAddBookmark.visibility = View.GONE
                    tvReviewAddBookmark.visibility = View.GONE

                    tvIsBookmarkedYet.visibility = View.VISIBLE
                    tvIsBookmarkedYet.text = if (question.isBookmark == QUESTION_BOOKMARKED) {
                        QuizApplication.resource.getString(R.string.bookmark_added)
                    } else {
                        QuizApplication.resource.getString(R.string.bookmark_not_added)
                    }
                } else {
                    // nếu đến từ đoạn câu hỏi, cho phép người dùng thêm dấu trang. Ẩn trạng thái dấu trang
                    ivReviewAddBookmark.visibility = View.VISIBLE
                    tvReviewAddBookmark.visibility = View.VISIBLE

                    tvIsBookmarkedYet.visibility = View.GONE
                }

                // thay đổi văn bản và biểu tượng dựa trên điều kiện đánh dấu của câu hỏi hiện tại
                if (question.isBookmark == QUESTION_BOOKMARKED) {
                    isBookmarked = true
                    whenBookmarked()
                } else {
                    isBookmarked = false
                    whenNotBookmarked()
                }

                tvQuestion.text = question.question
                tvOption1.text = question.option1
                tvOption2.text = question.option2
                tvOption3.text = question.option3
                tvOption4.text = question.option4
                tvExplanation.text = question.explanation

                // lengthy but necessary
                when (question.answerNr) {
                    1 -> {
                        ivFirstOption.setImageResource(R.drawable.ic_correct_option)
                        ivSecondOption.setImageResource(R.drawable.ic_option)
                        ivThirdOption.setImageResource(R.drawable.ic_option)
                        ivFourthOption.setImageResource(R.drawable.ic_option)
                    }
                    2 -> {
                        ivFirstOption.setImageResource(R.drawable.ic_option)
                        ivSecondOption.setImageResource(R.drawable.ic_correct_option)
                        ivThirdOption.setImageResource(R.drawable.ic_option)
                        ivFourthOption.setImageResource(R.drawable.ic_option)
                    }
                    3 -> {
                        ivFirstOption.setImageResource(R.drawable.ic_option)
                        ivSecondOption.setImageResource(R.drawable.ic_option)
                        ivThirdOption.setImageResource(R.drawable.ic_correct_option)
                        ivFourthOption.setImageResource(R.drawable.ic_option)
                    }
                    4 -> {
                        ivFirstOption.setImageResource(R.drawable.ic_option)
                        ivSecondOption.setImageResource(R.drawable.ic_option)
                        ivThirdOption.setImageResource(R.drawable.ic_option)
                        ivFourthOption.setImageResource(R.drawable.ic_correct_option)
                    }
                }

                ivReviewAddBookmark.setOnClickListener {
                    reRenderPos = questionNumber - 1
                    isBookmarked = !isBookmarked

                    if (isBookmarked) {
                        whenBookmarked()
                    } else {
                        whenNotBookmarked()
                    }

                    this@ReviewAnswerAdapter.onBookmarkClickListener?.let {
                        it(isBookmarked, (questionNumber - 1))
                    }
                }
            }
        }

        private fun whenBookmarked() {
            binding.apply {
                tvReviewAddBookmark.text = QuizApplication.resource.getString(R.string.bookmark_added)
                ivReviewAddBookmark.setImageResource(R.drawable.ic_bookmark_added_red)
            }
        }

        private fun whenNotBookmarked() {
            binding.apply {
                tvReviewAddBookmark.text = QuizApplication.resource.getString(R.string.bookmark_this_question)
                ivReviewAddBookmark.setImageResource(R.drawable.ic_add_bookmark)
            }
        }
    }
}
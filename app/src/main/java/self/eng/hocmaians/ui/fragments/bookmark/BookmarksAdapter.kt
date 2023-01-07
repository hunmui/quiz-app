package self.eng.hocmaians.ui.fragments.bookmark

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import self.eng.hocmaians.QuizApplication
import self.eng.hocmaians.R
import self.eng.hocmaians.databinding.EachBookmarkBinding
import self.eng.hocmaians.data.entities.Question

class BookmarksAdapter : RecyclerView.Adapter<BookmarksAdapter.BookmarksViewHolder>() {

    private var onQuestionClickListener: ((Question) -> Unit)? = null

    fun setOnQuestionClickListener(listener: (Question) -> Unit) {
        onQuestionClickListener = listener
    }

    inner class BookmarksViewHolder(
        private val binding: EachBookmarkBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(question: Question) {

            var showExplanation = false

            binding.apply {

                tvBookmarkQuestion.text = question.question

                tvBookmarkOption1.text = question.option1
                tvBookmarkOption2.text = question.option2
                tvBookmarkOption3.text = question.option3
                tvBookmarkOption4.text = question.option4

                // lengthy but necessary
                when (question.answerNr) {
                    1 -> {
                        ivBookmarkFirstOption.setImageResource(R.drawable.ic_correct_option)
                        ivBookmarkSecondOption.setImageResource(R.drawable.ic_option)
                        ivBookmarkThirdOption.setImageResource(R.drawable.ic_option)
                        ivBookmarkFourthOption.setImageResource(R.drawable.ic_option)
                    }
                    2 -> {
                        ivBookmarkFirstOption.setImageResource(R.drawable.ic_option)
                        ivBookmarkSecondOption.setImageResource(R.drawable.ic_correct_option)
                        ivBookmarkThirdOption.setImageResource(R.drawable.ic_option)
                        ivBookmarkFourthOption.setImageResource(R.drawable.ic_option)
                    }
                    3 -> {
                        ivBookmarkFirstOption.setImageResource(R.drawable.ic_option)
                        ivBookmarkSecondOption.setImageResource(R.drawable.ic_option)
                        ivBookmarkThirdOption.setImageResource(R.drawable.ic_correct_option)
                        ivBookmarkFourthOption.setImageResource(R.drawable.ic_option)
                    }
                    4 -> {
                        ivBookmarkFirstOption.setImageResource(R.drawable.ic_option)
                        ivBookmarkSecondOption.setImageResource(R.drawable.ic_option)
                        ivBookmarkThirdOption.setImageResource(R.drawable.ic_option)
                        ivBookmarkFourthOption.setImageResource(R.drawable.ic_correct_option)
                    }
                }

                tvShowExplanation.setOnClickListener {
                    showExplanation = !showExplanation

                    if (showExplanation) {
                        tvShowExplanation.text = QuizApplication.resource.getString(
                            R.string.tv_hide_explanation
                        )

                        tvBookmarkExplanation.apply {
                            visibility = View.VISIBLE
                            text = question.explanation
                        }

                    } else {
                        tvShowExplanation.text = QuizApplication.resource.getString(
                            R.string.tv_show_explanation
                        )

                        tvBookmarkExplanation.apply {
                            visibility = View.GONE
                        }
                    }
                }
            }
            itemView.setOnClickListener {
                onQuestionClickListener?.let {
                    it(question)
                }
            }
        }
    }

    private val differCallback = object : DiffUtil.ItemCallback<Question>() {
        override fun areItemsTheSame(oldItem: Question, newItem: Question): Boolean =
            (oldItem.id == newItem.id)

        override fun areContentsTheSame(oldItem: Question, newItem: Question): Boolean =
            (oldItem == newItem)
    }

    val differ: AsyncListDiffer<Question> = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarksViewHolder {
        val binding = EachBookmarkBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return BookmarksViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookmarksViewHolder, position: Int) {
        val question = differ.currentList[position]
        holder.bind(question)
    }

    override fun getItemCount(): Int = differ.currentList.size
}
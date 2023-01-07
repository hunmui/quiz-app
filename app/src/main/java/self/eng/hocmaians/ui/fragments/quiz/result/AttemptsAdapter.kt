package self.eng.hocmaians.ui.fragments.quiz.result

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import self.eng.hocmaians.databinding.EachAttemptBinding
import self.eng.hocmaians.data.entities.Score
import self.eng.hocmaians.util.CommonMethods

class AttemptsAdapter : RecyclerView.Adapter<AttemptsAdapter.AttemptViewHolder>() {

    // this is a variable that has the type of a function (lambda function)
    // that function takes an Score as argument, and return nothing
    private var onScoreClickListener: ((Score) -> Unit)? = null

    fun setOnScoreClickListener(listener: (Score) -> Unit) {
        onScoreClickListener = listener
    }

    inner class AttemptViewHolder(
        private val binding: EachAttemptBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(score: Score, position: Int) {
            binding.apply {
                tvAttemptOrder.text = (position + 1).toString()
                tvAttemptDate.text = CommonMethods.millisToDateTime(score.timestamp)
                tvAttemptScore.text = CommonMethods.userScoreInString(
                    score.totalCorrect,
                    score.totalQuestions
                )
            }
            itemView.setOnClickListener {
                this@AttemptsAdapter.onScoreClickListener?.let {
                    it(score)
                }
            }
        }
    }

    private val differCallback = object : DiffUtil.ItemCallback<Score>() {
        override fun areItemsTheSame(oldItem: Score, newItem: Score): Boolean {
            return oldItem.timestamp == newItem.timestamp
        }

        override fun areContentsTheSame(oldItem: Score, newItem: Score): Boolean {
            return oldItem == newItem
        }
    }

    val differ: AsyncListDiffer<Score> = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttemptViewHolder {
        val binding = EachAttemptBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return AttemptViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AttemptViewHolder, position: Int) {
        val score = differ.currentList[position]
        holder.bind(score, position)
    }

    override fun getItemCount(): Int = differ.currentList.size
}
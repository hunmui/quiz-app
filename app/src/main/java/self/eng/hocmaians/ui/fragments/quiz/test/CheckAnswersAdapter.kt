package self.eng.hocmaians.ui.fragments.quiz.test

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import self.eng.hocmaians.databinding.EachAnswerCheckBinding
import self.eng.hocmaians.util.CommonMethods

class CheckAnswersAdapter : RecyclerView.Adapter<CheckAnswersAdapter.CheckAnswersViewHolder>() {

    private val differCallback = object : DiffUtil.ItemCallback<Int>() {
        override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean =
            (oldItem == newItem)

        override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean =
            (oldItem == newItem)
    }

    val differ: AsyncListDiffer<Int> = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckAnswersViewHolder {
        val binding = EachAnswerCheckBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return CheckAnswersViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CheckAnswersViewHolder, position: Int) {
        val eachAnswer = differ.currentList[position]
        holder.binding(position, eachAnswer)
    }

    override fun getItemCount(): Int = differ.currentList.size

    private var onCheckAnswerClickListener: ((Int) -> Unit)? = null

    fun setOnCheckAnswerClickListener(listener: (Int) -> Unit) {
        onCheckAnswerClickListener = listener
    }

    inner class CheckAnswersViewHolder(
        private val binding: EachAnswerCheckBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun binding(position: Int, eachAnswer: Int) {
            binding.apply {
                tvQuestionNumber.text = (position + 1).toString()
                tvUserAnswer.text = CommonMethods.convertIndexToText(eachAnswer)
            }
            itemView.setOnClickListener {
                this@CheckAnswersAdapter.onCheckAnswerClickListener?.let {
                    it(bindingAdapterPosition)
                }
            }
        }
    }
}
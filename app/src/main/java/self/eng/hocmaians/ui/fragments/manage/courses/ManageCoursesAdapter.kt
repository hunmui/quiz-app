package self.eng.hocmaians.ui.fragments.manage.courses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import self.eng.hocmaians.data.entities.Course
import self.eng.hocmaians.databinding.EachCourseAndTopicBinding

class ManageCoursesAdapter : RecyclerView.Adapter<ManageCoursesAdapter.ManageCoursesViewHolder>() {

    // khi người dùng nhấp vào một itemView (toàn bộ khóa học), sau đó chuyển khóa courseId và courseName
    private var onCourseClickListener: ((Int, String) -> Unit)? = null

    fun setOnCourseClickListener(listener: (Int, String) -> Unit) {
        onCourseClickListener = listener
    }

    inner class ManageCoursesViewHolder(
        private val binding: EachCourseAndTopicBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(course: Course) {
            binding.apply {
                tvCourseTopicName.text = course.name
                ivEditCourseTopic.visibility = View.GONE
            }
            itemView.setOnClickListener {
                onCourseClickListener?.let {
                    it(course.id, course.name)
                }
            }
        }
    }

    private val differCallback = object : DiffUtil.ItemCallback<Course>() {
        override fun areItemsTheSame(oldItem: Course, newItem: Course): Boolean =
            (oldItem.id == newItem.id)

        override fun areContentsTheSame(oldItem: Course, newItem: Course): Boolean =
            (oldItem == newItem)
    }

    val differ: AsyncListDiffer<Course> = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManageCoursesViewHolder {
        val binding = EachCourseAndTopicBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return ManageCoursesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ManageCoursesViewHolder, position: Int) {
        val course = differ.currentList[position]
        holder.bind(course)
    }

    override fun getItemCount(): Int = differ.currentList.size
}
package self.eng.hocmaians.ui.fragments.manage.courses

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import self.eng.hocmaians.data.entities.Course
import self.eng.hocmaians.repositories.IRepository
import javax.inject.Inject

@HiltViewModel
class ManageCoursesViewModel @Inject constructor(
    repository: IRepository
) : ViewModel() {

    // dữ liệu trực tiếp từ db để quản lý các đoạn liên quan đến khóa học để quan sát
    val courses: LiveData<List<Course>> = repository.getAllCourses()
}
package self.eng.hocmaians.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.*
import dagger.hilt.android.AndroidEntryPoint
import self.eng.hocmaians.databinding.ActivityMainBinding
import self.eng.hocmaians.R


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // để gọi findNavController() trong onCreate()
    private lateinit var navController: NavController

    // cấu hình thanh ứng dụng
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()

        // tạo homeFragment và searchFragment làm đích cấp cao nhất
        // và lấy icon hamburger cho DrawerLayout
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.bookmarksFragment,
                R.id.manageCoursesFragment,
                R.id.progressHomeFragment
            ),
            binding.drawerLayoutMainActivity
        )

        // thay thế ActionBar bằng Thanh công cụ
        setSupportActionBar(binding.toolbar)
        // kết nối ActionBar (nay là Toolbar) với NavController
        setupActionBarWithNavController(navController, appBarConfiguration)

        // liên kết ngăn điều hướng với biểu đồ điều hướng
        binding.apply {
            navView.setupWithNavController(navController)

            // hiển thị đúng hình icon
            navView.itemIconTintList = null
        }
    }

    /**
     * Xử lý nút Lên trong ActionBar
     *
     * @return true nếu điều hướng thành công, nếu không hãy gọi siêu hàm tạo
     */
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
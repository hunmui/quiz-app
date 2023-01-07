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

    // in order to call findNavController() in onCreate()
    private lateinit var navController: NavController

    // config the app bar
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()

        // making homeFragment, and searchFragment as top level destinations
        // and get the hamburger icon for the drawerLayout
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.bookmarksFragment,
                R.id.manageCoursesFragment,
                R.id.progressHomeFragment
            ),
            binding.drawerLayoutMainActivity
        )

        // replace ActionBar with our Toolbar
        setSupportActionBar(binding.toolbar)
        // connect ActionBar (now is Toolbar) to NavController
        setupActionBarWithNavController(navController, appBarConfiguration)

        // binding navigation drawer to nav graph
        binding.apply {
            navView.setupWithNavController(navController)

            // display icon image properly
            navView.itemIconTintList = null
        }
    }

    /**
     * Handles the Up button in ActionBar
     *
     * @return true if navigation was successful, else call the super constructor
     */
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
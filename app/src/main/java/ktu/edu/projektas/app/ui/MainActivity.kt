package ktu.edu.projektas.app.ui

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import ktu.edu.projektas.R
import androidx.core.view.GravityCompat
import androidx.fragment.app.activityViewModels
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import ktu.edu.projektas.app.data.ScheduleViewModel
import ktu.edu.projektas.app.data.ScheduleViewModelFactory
import ktu.edu.projektas.app.data.User

class MainActivity : AppCompatActivity(){
    private lateinit var navController : NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var drawerLayout:DrawerLayout
    private lateinit var navigationView : NavigationView
    private lateinit var mAuth : FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()


        navController = findNavController(R.id.nav_host_fragment_container)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigationView)
        navigationView.setupWithNavController(navController)

        navigationView.setNavigationItemSelectedListener{
            when(it.itemId) {
                R.id.nav_logout -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    logOut()
                }
                R.id.homeFragment ->    {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    navController.navigate(R.id.homeFragment)
                }
                R.id.scheduleFragment ->    {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    navController.navigate(R.id.scheduleFragment)
                }
                R.id.profileFragment ->    {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    navController.navigate(R.id.profileFragment)
                }
                R.id.notificationFragment ->    {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    navController.navigate(R.id.notificationFragment)
                }
            }
            true
        }

        appBarConfiguration =   AppBarConfiguration(setOf(R.id.homeFragment, R.id.scheduleFragment), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)


        if(mAuth.currentUser == null){
            navController.navigate(R.id.action_homeFragment_to_loginFragment)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_container)

        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return false
    }

    private fun logOut(){
        mAuth.signOut()
        navController.navigate(R.id.loginFragment)
    }

}
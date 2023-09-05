package com.nelayanku.apps.redirect

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.nelayanku.apps.R
import com.nelayanku.apps.act.admin.InfoFragment
import com.nelayanku.apps.act.admin.SettingsFragment
import com.nelayanku.apps.act.admin.TarikFragment
import com.nelayanku.apps.act.admin.TopupFragment
import com.nelayanku.apps.act.admin.HomeFragment


class AdminActivity : AppCompatActivity() {

    private lateinit var fragmentContainer: FrameLayout
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        fragmentContainer = findViewById(R.id.fragmentContainer)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        val homeFragment = HomeFragment()
        //dapatkan intent dari activity sebelumnya
        val intent = intent
        //dapatkan data dari intent
        val fragment = intent.getStringExtra("fragment")
        if (fragment != null) {
            if (fragment == "home") {
                val orderFragment = HomeFragment()
                replaceFragment(orderFragment)
                bottomNavigationView.selectedItemId = R.id.menu_home
            }else if (fragment == "tarik") {
                val orderFragment = TarikFragment()
                replaceFragment(orderFragment)
                bottomNavigationView.selectedItemId = R.id.menu_tarik
            }else if (fragment == "topup") {
                val orderFragment = TopupFragment()
                replaceFragment(orderFragment)
                bottomNavigationView.selectedItemId = R.id.menu_topup
            }else if (fragment == "info") {
                val orderFragment = InfoFragment()
                replaceFragment(orderFragment)
                bottomNavigationView.selectedItemId = R.id.menu_info
            }else if (fragment == "setting") {
                val orderFragment = SettingsFragment()
                replaceFragment(orderFragment)
                bottomNavigationView.selectedItemId = R.id.menu_settings
            }else if (fragment == "profile") {
                val orderFragment = com.nelayanku.apps.act.admin.ProfileFragment()
                replaceFragment(orderFragment)
                bottomNavigationView.selectedItemId = R.id.menu_profile
            }else{
                val orderFragment = com.nelayanku.apps.act.admin.HomeFragment()
                replaceFragment(orderFragment)
                bottomNavigationView.selectedItemId = R.id.menu_home
            }
        }else{
            replaceFragment(homeFragment)
        }

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_home -> {
                    val homeFragment = HomeFragment()
                    replaceFragment(homeFragment)
                    true
                }
                R.id.menu_info -> {
                    val orderFragment = InfoFragment()
                    replaceFragment(orderFragment)
                    true
                }

                R.id.menu_tarik -> {
                    val profileFragment = TarikFragment()
                    replaceFragment(profileFragment)
                    true
                }
                R.id.menu_topup -> {
                    val profileFragment = TopupFragment()
                    replaceFragment(profileFragment)
                    true
                }
                R.id.menu_settings -> {
                    val profileFragment = SettingsFragment()
                    replaceFragment(profileFragment)
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
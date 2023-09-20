package com.nelayanku.apps.redirect

import android.content.ClipData.newIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.nelayanku.apps.R
import com.nelayanku.apps.act.admin.TarikFragment
import com.nelayanku.apps.act.admin.TopupFragment
import com.nelayanku.apps.act.seller.HomeFragment
import com.nelayanku.apps.act.seller.OrderFragment
import com.nelayanku.apps.act.seller.ProfileFragment
import com.nelayanku.apps.act.seller.WalletFragment
import com.nelayanku.apps.chat.ChatListActivity


class SellerActivity : AppCompatActivity() {

    private lateinit var fragmentContainer: FrameLayout
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var btnChat: ImageButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller)

        fragmentContainer = findViewById(R.id.fragmentContainer)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        btnChat = findViewById(R.id.btnChat)

        btnChat.setOnClickListener{
            //pindah ke chatlistActivity
            val intent2  = Intent(this, ChatListActivity::class.java)
            startActivity(intent2)
        }
        val homeFragment = HomeFragment()
        //dapatkan intent dari activity sebelumnya
        val intent = intent
        //dapatkan data dari intent
        val fragment = intent.getStringExtra("fragment")
        if (fragment != null) {
            if (fragment == "wallet") {
                val orderFragment = WalletFragment()
                replaceFragment(orderFragment)
                bottomNavigationView.selectedItemId = R.id.menu_wallet
            }else if (fragment == "home") {
                val orderFragment = HomeFragment()
                replaceFragment(orderFragment)
                bottomNavigationView.selectedItemId = R.id.menu_home
            }else if (fragment == "order") {
                val orderFragment = OrderFragment()
                replaceFragment(orderFragment)
                bottomNavigationView.selectedItemId = R.id.menu_order
            }else if (fragment == "profil") {
                val orderFragment = ProfileFragment()
                replaceFragment(orderFragment)
                bottomNavigationView.selectedItemId = R.id.menu_profile
            }
            else{
                replaceFragment(homeFragment)
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
                R.id.menu_order -> {
                    val orderFragment = OrderFragment()
                    replaceFragment(orderFragment)
                    true
                }
                R.id.menu_wallet -> {
                    val walletFragment = WalletFragment()
                    replaceFragment(walletFragment)
                    true
                }
                R.id.menu_profile -> {
                    val profileFragment = ProfileFragment()
                    replaceFragment(profileFragment)
                    true
                }
                R.id.menu_topup -> {
                    val profileFragment = TopupFragment()
                    replaceFragment(profileFragment)
                    true
                }
                R.id.menu_profile -> {
                    val profileFragment = TarikFragment()
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

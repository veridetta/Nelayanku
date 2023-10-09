package com.nelayanku.apps.act.seller

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.widget.LinearLayout
import com.nelayanku.apps.R

class LayananActivity : AppCompatActivity() {
    lateinit var btnWa : LinearLayout
    lateinit var btnFb : LinearLayout
    lateinit var btnGm : LinearLayout
    lateinit var btnYt : LinearLayout
    lateinit var btnX : LinearLayout
    lateinit var btnTt : LinearLayout
    lateinit var btnIg : LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_layanan)
        initView()
        initClick()
    }

    private fun initView(){
        btnWa = findViewById(R.id.btnWa)
        btnFb = findViewById(R.id.btnFb)
        btnGm = findViewById(R.id.btnGm)
        btnYt = findViewById(R.id.btnYt)
        btnX = findViewById(R.id.btnX)
        btnTt = findViewById(R.id.btnTt)
        btnIg = findViewById(R.id.btnIg)
    }

    private fun initClick(){
        btnWa.setOnClickListener {
            intentWhatsapp()
        }
        btnIg.setOnClickListener {
            intentInstagram()
        }
        btnFb.setOnClickListener {
            intentFacebook()
        }
        btnGm.setOnClickListener {
            intentGmail()
        }
        btnX.setOnClickListener {
            intentX()
        }
        btnYt.setOnClickListener {
            intentYoutube()
        }
        btnTt.setOnClickListener {
            intentTt()
        }
    }
    private fun intentWhatsapp() {
        val phoneNumber = "0881027207572"
        val message = "Halo Nelayanku!"
        val packageName = "com.whatsapp"

        try {
            val whatsappIntent = Intent(Intent.ACTION_SEND)
            whatsappIntent.type = "text/plain"
            whatsappIntent.`package` = packageName

            // Mengisi nomor HP dan pesan ke Intent
            whatsappIntent.putExtra("jid", PhoneNumberUtils.stripSeparators("$phoneNumber@s.whatsapp.net"))
            whatsappIntent.putExtra(Intent.EXTRA_TEXT, message)

            startActivity(whatsappIntent)
        } catch (e: Exception) {
            // WhatsApp tidak terinstall, coba buka melalui browser
            val url = "https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}"
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(browserIntent)
        }
    }
    private fun intentInstagram() {
        val username = "nelayan.ku"
        val packageName = "com.instagram.android"

        try {
            // Cek apakah Instagram terinstall
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)

            // Jika terinstall, buka profil Instagram
            val instagramIntent = Intent(Intent.ACTION_VIEW)
            instagramIntent.data = Uri.parse("https://www.instagram.com/$username/")
            instagramIntent.setPackage(packageName)
            startActivity(instagramIntent)
        } catch (e: PackageManager.NameNotFoundException) {
            // Instagram tidak terinstall, coba buka melalui browser
            val url = "https://www.instagram.com/$username/"
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(browserIntent)
        }
    }
    private fun intentFacebook() {
        val profileUrl = "https://www.facebook.com/profile.php?id=100094069810482"
        val packageName = "com.facebook.katana"

        try {
            val facebookIntent = Intent(Intent.ACTION_VIEW)
            facebookIntent.data = Uri.parse(profileUrl)
            facebookIntent.setPackage(packageName)

            if (packageManager.queryIntentActivities(facebookIntent, 0).isNotEmpty()) {
                // Aplikasi Facebook terinstall, buka profil menggunakan aplikasi
                startActivity(facebookIntent)
            } else {
                // Facebook tidak terinstall, coba buka profil melalui browser
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(profileUrl))
                startActivity(browserIntent)
            }
        } catch (e: Exception) {
            // Handle exception if needed
        }
    }

    private fun intentGmail() {
        val emailAddress = "nelayankuofficial@gmail.com"
        val packageName = "com.google.android.gm"

        try {
            val gmailIntent = Intent(Intent.ACTION_SENDTO)
            gmailIntent.data = Uri.parse("mailto:$emailAddress")
            gmailIntent.setPackage(packageName)

            if (packageManager.queryIntentActivities(gmailIntent, 0).isNotEmpty()) {
                // Aplikasi Gmail terinstall, buka aplikasi untuk mengirim email
                startActivity(gmailIntent)
            } else {
                // Gmail tidak terinstall, coba buka melalui browser
                val url = "https://mail.google.com/"
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(browserIntent)
            }
        } catch (e: Exception) {
            // Handle exception if needed
        }
    }
    private fun intentYoutube() {
        val username = "nelayankuofficial"
        val packageName = "com.google.android.youtube"

        try {
            val youtubeIntent = Intent(Intent.ACTION_VIEW)
            youtubeIntent.data = Uri.parse("https://www.youtube.com/user/$username")
            youtubeIntent.setPackage(packageName)

            if (packageManager.queryIntentActivities(youtubeIntent, 0).isNotEmpty()) {
                // Aplikasi YouTube terinstall, buka kanal menggunakan aplikasi
                startActivity(youtubeIntent)
            } else {
                // YouTube tidak terinstall, coba buka kanal melalui browser
                val url = "https://www.youtube.com/user/$username"
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(browserIntent)
            }
        } catch (e: Exception) {
            // Handle exception if needed
        }
    }


    private fun intentX() {
        val username = "nelayanku_id"
        val packageName = "com.twitter.android"

        try {
            val twitterIntent = Intent(Intent.ACTION_VIEW)
            twitterIntent.data = Uri.parse("https://twitter.com/$username")
            twitterIntent.setPackage(packageName)

            if (packageManager.queryIntentActivities(twitterIntent, 0).isNotEmpty()) {
                // Aplikasi Twitter terinstall, buka profil menggunakan aplikasi
                startActivity(twitterIntent)
            } else {
                // Twitter tidak terinstall, coba buka profil melalui browser
                val url = "https://twitter.com/$username"
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(browserIntent)
            }
        } catch (e: Exception) {
            // Handle exception if needed
        }
    }

    private fun intentTt() {
        val username = "nelayan.ku"
        val packageName = "com.zhiliaoapp.musically"

        try {
            // Cek apakah TikTok terinstall
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)

            // Jika terinstall, buka profil TikTok
            val tiktokIntent = Intent(Intent.ACTION_VIEW)
            tiktokIntent.data = Uri.parse("https://www.tiktok.com/@$username")
            tiktokIntent.setPackage(packageName)
            startActivity(tiktokIntent)
        } catch (e: PackageManager.NameNotFoundException) {
            // TikTok tidak terinstall, coba buka profil melalui browser
            val url = "https://www.tiktok.com/@$username"
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(browserIntent)
        }
    }

}
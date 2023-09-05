package com.nelayanku.apps.account

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import com.google.android.gms.maps.model.LatLng
import com.nelayanku.apps.R

class SkActivity : AppCompatActivity() {
    var htmlUmum=""
    var htmlRegister=""
    var htmlOlah=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sk)
        html()
        val sk = intent.getStringExtra("sk") ?: "umum"
        var html = ""
        if(sk.equals("umum")){
            html=htmlUmum
        }
        if(sk.equals("register")){
            html=htmlRegister
        }
        if(sk.equals("olah")){
            html=htmlOlah
        }
        val webView = findViewById<WebView>(R.id.webView)
        val webViewSettings = webView.settings
        webViewSettings.javaScriptEnabled = true
        //webViewSettings.loadWithOverviewMode = true
        //webViewSettings.useWideViewPort = true
        webView.clearView();
        webView.loadData(html,"text/html","UTF-8")
        webView.requestLayout();
    }
    fun html(){
        htmlUmum = """
            <p style='margin-top:0cm;margin-right:0cm;margin-bottom:8.0pt;margin-left:0cm;font-size:18.0pt;font-family:"Calibri",sans-serif;text-align:center;line-height:115%;'>
                <strong><span style='font-size:21px;line-height:115%;font-family:"Times New Roman",serif;'>SK Mitra (<em>Seller</em>) Umum (Bergabung Aplikasi &ldquo;Nelayanku&rdquo;)</span></strong>
            </p>
            <p style='margin-top:0cm;margin-right:0cm;margin-bottom:8.0pt;margin-left:0cm;font-size:11.0pt;font-family:"Calibri",sans-serif;text-align:center;line-height:115%;'>
                <span style='font-size:16px;line-height:115%;font-family:"Times New Roman",serif;'>Syarat dan Ketentuan Mitra (<em>Seller</em>) Umum Registrasi Aplikasi &ldquo;Nelayanku&rdquo;</span>
            </p>
        
            <ol start="1" style="list-style-type: decimal;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Kualifikasi dan Verifikasi:&nbsp;</span></li>
            </ol>
            <ol style="list-style-type: lower-alpha;margin-left:44px;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Untuk menjadi penjual atau mitra dalam aplikasi &ldquo;Nelayanku&rdquo;, Anda harus memenuhi kualifikasi dan persyaratan yang ditentukan oleh kami.&nbsp;</span></li>
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Kami berhak untuk melakukan verifikasi data dan informasi yang Anda berikan selama proses registrasi.&nbsp;</span></li>
            </ol>
        
            <ol start="2"  style="list-style-type: undefined;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Informasi Akun:</span></li>
            </ol>
            <ol style="list-style-type: lower-alpha;margin-left:44px;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Anda bertanggung jawab atas keakuratan dan kelengkapan informasi yang Anda berikan saat mendaftar sebagai penjual atau mitra.&nbsp;</span></li>
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Pastikan untuk menjaga kerahasiaan informasi login dan tidak memberikan akses akun Anda kepada pihak lain.&nbsp;</span></li>
            </ol>
            <ol  start="3" style="list-style-type: undefined;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Produk yang Ditawarkan:</span></li>
            </ol>
            <ol start="1" style="list-style-type: lower-alpha;margin-left:44px;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Anda bertanggung jawab atas produk ikan atau layanan yang ditawarkan di aplikasi ini.&nbsp;</span></li>
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Pastikan produk yang Anda jual sesuai dengan deskripsi yang diberikan dan sesuai dengan peraturan yang berlaku.&nbsp;</span></li>
            </ol>
            <ol  start="4" style="list-style-type: undefined;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Kode Etik:</span></li>
            </ol>
            <ol start="1" style="list-style-type: lower-alpha;margin-left:44px;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Sebagai penjual atau mitra, Anda diharapkan untuk berperilaku dengan etika, menghormati pengguna lain, dan tidak terlibat dalam praktik ilegal atau merugikan.&nbsp;</span></li>
            </ol>
            <ol  start="5" style="list-style-type: undefined;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Harga dan Pembayaran:</span></li>
            </ol>
            <ol start="1" style="list-style-type: lower-alpha;margin-left:44px;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Anda harus menyediakan harga produk atau layanan yang jelas dan akurat di aplikasi ini.&nbsp;</span></li>
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Metode pembayaran akan ditentukan oleh kebijakan aplikasi, dan Anda harus memastikan penggunaan metode pembayaran yang sah dan aman.&nbsp;</span></li>
            </ol>
            <ol  start="6" style="list-style-type: undefined;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Kebijakan Pengiriman dan Pengembalian:</span></li>
            </ol>
            <ol start="1" style="list-style-type: lower-alpha;margin-left:44px;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Anda harus mematuhi kebijakan pengiriman dan pengembalian yang berlaku di aplikasi ini.&nbsp;</span></li>
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Pastikan untuk memberikan informasi pengiriman yang benar dan mengikuti prosedur pengembalian sesuai ketentuan yang berlaku.&nbsp;</span></li>
            </ol>
            <ol  start="7" style="list-style-type: undefined;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Kebijakan Privasi:</span></li>
            </ol>
            <ol start="1" style="list-style-type: lower-alpha;margin-left:44px;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Kami akan melindungi data pribadi Anda sesuai dengan kebijakan privasi kami.&nbsp;</span></li>
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Informasi yang Anda berikan saat registrasi akan digunakan untuk tujuan bisnis dan tidak akan dibagikan kepada pihak ketiga tanpa izin.&nbsp;</span></li>
            </ol>
            <ol start="8" style="list-style-type: undefined;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Tanggung Jawab Produk:</span></li>
            </ol>
            <ol style="list-style-type: lower-alpha;margin-left:44px;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Anda bertanggung jawab atas kualitas dan keaslian produk yang Anda jual melalui aplikasi &ldquo;Nelayanku&rdquo;.&nbsp;</span></li>
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Tidak ada produk palsu, ilegal, atau berbahaya yang diizinkan di aplikasi &ldquo;Nelayanku&rdquo;. &nbsp;</span></li>
            </ol>
        
            <ol  start="9" style="list-style-type: undefined;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Perubahan Syarat dan Ketentuan:</span></li>
            </ol>
            <ol style="list-style-type: lower-alpha;margin-left:44px;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Syarat dan ketentuan ini dapat berubah dari waktu ke waktu.&nbsp;</span></li>
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Anda akan diberitahu tentang perubahan tersebut dan diminta untuk menyetujui syarat dan ketentuan baru.&nbsp;</span></li>
            </ol>
        
            <ol  start="10" style="list-style-type: undefined;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Pembekuan Akun:</span></li>
            </ol>
            <ol style="list-style-type: lower-alpha;margin-left:44px;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Kami berhak untuk membekukan akun penjual atau mitra jika ada pelanggaran serius terhadap syarat dan ketentuan ini.&nbsp;</span></li>
            </ol>
        
            <ol start="11"  style="list-style-type: undefined;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Pembatalan Mitra:</span></li>
            </ol>
            <ol style="list-style-type: lower-alpha;margin-left:44px;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Kami berhak untuk membatalkan status mitra Anda jika Anda melanggar syarat dan ketentuan ini atau jika ada alasan lain yang sah.&nbsp;</span></li>
            </ol>
        
            <ol start="12"  style="list-style-type: undefined;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Hukum yang Berlaku:</span></li>
            </ol>
            <ol style="list-style-type: lower-alpha;margin-left:44px;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Syarat dan ketentuan ini tunduk pada hukum yang berlaku di wilayah hukum kami.&nbsp;</span></li>
            </ol>
        """
        htmlRegister = """
            <p style='margin-top:0cm;margin-right:0cm;margin-bottom:8.0pt;margin-left:0cm;font-size:11.0pt;font-family:"Calibri",sans-serif;text-align:center;line-height:115%;'>
                <strong><span style='font-size:21px;line-height:115%;font-family:"Times New Roman",serif;'>SK Mitra (<em>Seller</em>) Berbasis Edukasi atau Pemahaman Mitra terkait Penggunaan Alat Tangkap Ikan sesuai SOP dan Pemanfaatan Sumber Daya Maritim Lestari dan Keberlanjutan Sesuai <em>14<sup>th</sup> SDGs Program</em>&rdquo; (Bergabung Aplikasi &ldquo;Nelayanku&rdquo;)</span></strong>
            </p>
            <p style='margin-top:0cm;margin-right:0cm;margin-bottom:8.0pt;margin-left:0cm;font-size:11.0pt;font-family:"Calibri",sans-serif;text-align:center;line-height:115%;'>
                <span style='font-size:16px;line-height:115%;font-family:"Times New Roman",serif;'>Syarat dan Ketentuan Mitra (<em>Seller</em>) &nbsp;Registrasi Aplikasi &ldquo;Nelayanku&rdquo;</span>
            </p>
            <p style='margin-top:0cm;margin-right:0cm;margin-bottom:8.0pt;margin-left:0cm;font-size:11.0pt;font-family:"Calibri",sans-serif;line-height:115%;'>
                <span style='font-size:16px;line-height:115%;font-family:"Times New Roman",serif;'>&nbsp;</span>
            </p>
            <ol style="list-style-type: decimal;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Pemahaman Tentang SDGs dan Keberlanjutan:</span></li>
            </ol>
            <ol start="1" style="list-style-type: lower-alpha;margin-left:44px;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Sebagai penjual atau mitra di aplikasi &ldquo;Nelayanku&rdquo;, Anda harus memiliki pemahaman tentang tujuan 14 dari Sustainable Development Goals (SDGs) yang berkaitan dengan konservasi dan pemanfaatan sumber daya laut secara lestari.&nbsp;</span></li>
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Anda harus berkomitmen untuk mendukung keberlanjutan sumber daya maritim dan tidak terlibat dalam praktik yang merugikan lingkungan laut.&nbsp;</span></li>
            </ol>
            <ol style="list-style-type: undefined;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Informasi dan Pelaporan:</span></li>
            </ol>
            <ol start="2" style="list-style-type: lower-alpha;margin-left:44px;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Kami meminta Anda untuk menyediakan informasi yang jelas dan akurat tentang asal-usul ikan yang Anda jual, termasuk metode tangkap, wilayah penangkapan, dan jenis alat tangkap ikan yang digunakan.&nbsp;</span></li>
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Anda harus bersedia melaporkan jumlah tangkapan, upaya tangkap, dan informasi relevan lainnya sesuai dengan SOP undang-undang pemerintah terkait.&nbsp;</span></li>
            </ol>
            <ol style="list-style-type: undefined;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Pemanfaatan Sumber Daya Lestari:</span></li>
            </ol>
            <ol start="3" style="list-style-type: lower-alpha;margin-left:44px;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Sebagai penjual atau mitra, Anda harus berkomitmen untuk memanfaatkan sumber daya maritim secara lestari dan bertanggung jawab terhadap keberlanjutan ekosistem laut.&nbsp;</span></li>
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Anda harus menghindari metode tangkap ikan yang merusak habitat laut atau mengancam spesies yang terancam punah.&nbsp;</span></li>
            </ol>
            <ol style="list-style-type: undefined;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Kepatuhan pada SOP dan Undang-Undang:</span></li>
            </ol>
            <ol start="4" style="list-style-type: lower-alpha;margin-left:44px;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Anda harus mematuhi semua peraturan dan standar operasional prosedur (SOP) yang ditetapkan oleh pemerintah Indonesia dalam melakukan aktivitas penangkapan ikan dan pengelolaan sumber daya laut.&nbsp;</span></li>
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Segala bentuk pelanggaran SOP atau undang-undang pemerintah dapat mengakibatkan pembekuan akun dan tindakan hukum lainnya.&nbsp;</span></li>
            </ol>
            <ol style="list-style-type: undefined;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Penyuluhan dan Edukasi:</span></li>
            </ol>
            <ol start="5" style="list-style-type: lower-alpha;margin-left:44px;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Kami mendorong Anda untuk terus meningkatkan pengetahuan tentang praktik penangkapan ikan yang berkelanjutan dan berpartisipasi dalam kegiatan penyuluhan atau edukasi terkait.&nbsp;</span></li>
            </ol>
            <ol style="list-style-type: undefined;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Pengecekan Legalitas Produk:</span></li>
            </ol>
            <ol start="6" style="list-style-type: lower-alpha;margin-left:44px;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Anda harus memastikan bahwa produk ikan yang Anda jual di aplikasi ini telah diperoleh secara legal dan sesuai dengan peraturan dan perundang-undangan yang berlaku.&nbsp;</span></li>
            </ol>
            <ol style="list-style-type: undefined;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Hukuman atas Pelanggaran:</span></li>
            </ol>
            <ol start="7" style="list-style-type: lower-alpha;margin-left:44px;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Jika terjadi pelanggaran terhadap ketentuan ini, Anda menerima bahwa kami berhak untuk menangguhkan atau membatalkan status penjual atau mitra Anda di aplikasi ini. Selain itu, tindakan hukum sesuai peraturan pemerintah juga dapat diterapkan.&nbsp;</span></li>
            </ol>
            <ol style="list-style-type: undefined;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Perubahan Syarat dan Ketentuan:</span></li>
            </ol>
            <ol start="8" style="list-style-type: lower-alpha;margin-left:44px;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Syarat dan ketentuan ini dapat berubah dari waktu ke waktu.&nbsp;</span></li>
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Anda akan diberitahu tentang perubahan tersebut dan diminta untuk menyetujui syarat dan ketentuan baru.&nbsp;</span></li>
            </ol>
            <ol style="list-style-type: undefined;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Hukum yang Berlaku:</span></li>
            </ol>
            <ol start="9" style="list-style-type: lower-alpha;margin-left:44px;">
                <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Syarat dan ketentuan ini tunduk pada hukum yang berlaku di wilayah hukum Indonesia.&nbsp;</span></li>
            </ol>
            """
        htmlOlah = """
        <p style='margin-top:0cm;margin-right:0cm;margin-bottom:8.0pt;margin-left:0cm;font-size:11.0pt;font-family:"Calibri",sans-serif;text-align:center;line-height:115%;'>
            <strong><span style='font-size:21px;line-height:115%;font-family:"Times New Roman",serif;'>SK Mitra (<em>Seller</em>) untuk Olahan Ikan (Bergabung Aplikasi &ldquo;Nelayanku&rdquo;)</span></strong>
        </p>
        <p style='margin-top:0cm;margin-right:0cm;margin-bottom:8.0pt;margin-left:0cm;font-size:11.0pt;font-family:"Calibri",sans-serif;text-align:center;line-height:115%;'>
            <span style='font-size:16px;line-height:115%;font-family:"Times New Roman",serif;'>Syarat dan Ketentuan Mitra (<em>Seller</em>) untuk Olahan Ikan Registrasi Aplikasi &ldquo;Nelayanku&rdquo;</span>
        </p>
        <p style='margin-top:0cm;margin-right:0cm;margin-bottom:8.0pt;margin-left:0cm;font-size:11.0pt;font-family:"Calibri",sans-serif;line-height:115%;'>
            <span style='font-size:16px;line-height:115%;font-family:"Times New Roman",serif;'>&nbsp;</span>
        </p>
        <ol style="list-style-type: decimal;">
            <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Kualifikasi Produk:</span></li>
        </ol>
        <ol start="1" style="list-style-type: lower-alpha;margin-left:44px;">
            <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Produk olahan makanan ikan laut yang dijual melalui aplikasi &ldquo;Nelayanku&rdquo; harus sesuai dengan standar keamanan pangan dan peraturan kesehatan yang berlaku.&nbsp;</span></li>
            <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Penjual atau mitra harus memiliki izin dan sertifikasi yang diperlukan untuk memproduksi dan menjual produk olahan makanan ikan laut.&nbsp;</span></li>
        </ol>
        <ol style="list-style-type: undefined;">
            <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Labeling dan Informasi Produk:</span></li>
        </ol>
        <ol start="2" style="list-style-type: lower-alpha;margin-left:44px;">
            <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Produk olahan ikan yang dijual harus memiliki labeling yang jelas, termasuk keterangan tentang bahan-bahan yang digunakan, tanggal kadaluwarsa, dan informasi gizi.&nbsp;</span></li>
            <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Pastikan informasi produk yang diberikan akurat dan sesuai dengan kenyataan.&nbsp;</span></li>
        </ol>
        <ol style="list-style-type: undefined;">
            <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Kualitas dan Kebersihan Produksi:</span></li>
        </ol>
        <ol start="3" style="list-style-type: lower-alpha;margin-left:44px;">
            <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Penjual atau mitra harus menjaga kebersihan dan kualitas produksi olahan makanan ikan laut secara konsisten.&nbsp;</span></li>
            <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Pabrik atau tempat produksi harus memenuhi standar sanitasi dan kebersihan untuk mencegah kontaminasi produk.&nbsp;</span></li>
        </ol>
        <ol style="list-style-type: undefined;">
            <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Penggunaan Bahan Tambahan dan Pengawet:</span></li>
        </ol>
        <ol start="4" style="list-style-type: lower-alpha;margin-left:44px;">
            <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Jika ada penggunaan bahan tambahan atau pengawet dalam produk olahan, pastikan untuk mencantumkan informasi tersebut dengan jelas pada kemasan dan sesuai dengan peraturan yang berlaku.&nbsp;</span></li>
            <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Pastikan penggunaan bahan tambahan dan pengawet sesuai dengan batas yang diizinkan oleh badan regulasi.&nbsp;</span></li>
        </ol>
        <ol style="list-style-type: undefined;">
            <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Kebijakan Pengiriman dan Pengembalian:</span></li>
        </ol>
        <ol start="5" style="list-style-type: lower-alpha;margin-left:44px;">
            <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Penjual atau mitra harus mematuhi kebijakan pengiriman dan pengembalian yang berlaku di aplikasi ini.&nbsp;</span></li>
            <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Pastikan untuk memberikan informasi pengiriman yang benar dan mengikuti prosedur pengembalian sesuai ketentuan yang berlaku.&nbsp;</span></li>
        </ol>
        <ol style="list-style-type: undefined;">
            <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Kebijakan Privasi:</span></li>
        </ol>
        <ol start="6" style="list-style-type: lower-alpha;margin-left:44px;">
            <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Kami akan melindungi data pribadi Anda sesuai dengan kebijakan privasi kami.&nbsp;</span></li>
            <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Informasi yang Anda berikan saat registrasi akan digunakan untuk tujuan bisnis dan tidak akan dibagikan kepada pihak ketiga tanpa izin.&nbsp;</span></li>
        </ol>
        <ol style="list-style-type: undefined;">
            <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Kepatuhan pada Regulasi dan Undang-Undang:</span></li>
        </ol>
        <ol start="7" style="list-style-type: lower-alpha;margin-left:44px;">
            <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Penjual atau mitra harus mematuhi semua peraturan dan undang-undang yang berlaku terkait produksi dan penjualan olahan makanan ikan laut.&nbsp;</span></li>
            <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Segala bentuk pelanggaran regulasi dan undang-undang dapat mengakibatkan pembekuan akun dan tindakan hukum lainnya.&nbsp;</span></li>
        </ol>
        <ol style="list-style-type: undefined;">
            <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Pembatalan Mitra:</span></li>
        </ol>
        <ol start="8" style="list-style-type: lower-alpha;margin-left:44px;">
            <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Kami berhak untuk membatalkan status mitra Anda jika Anda melanggar syarat dan ketentuan ini atau jika ada alasan lain yang sah.&nbsp;</span></li>
        </ol>
        <ol style="list-style-type: undefined;">
            <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Perubahan Syarat dan Ketentuan:</span></li>
        </ol>
        <ol start="9" style="list-style-type: lower-alpha;margin-left:44px;">
            <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Syarat dan ketentuan ini dapat berubah dari waktu ke waktu.&nbsp;</span></li>
            <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Anda akan diberitahu tentang perubahan tersebut dan diminta untuk menyetujui syarat dan ketentuan baru.&nbsp;</span></li>
        </ol>
        <ol style="list-style-type: undefined;">
            <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-family:"Times New Roman",serif;font-size:12.0pt;'>Hukum yang Berlaku:</span></li>
        </ol>
        <ol start="10" style="list-style-type: lower-alpha;margin-left:44px;">
            <li><span style='line-height:115%;font-family:"Times New Roman",serif;font-size:16px;'>Syarat dan ketentuan ini tunduk pada hukum yang berlaku di wilayah hukum Indonesia.</span></li>
        </ol>
        """

    }
}
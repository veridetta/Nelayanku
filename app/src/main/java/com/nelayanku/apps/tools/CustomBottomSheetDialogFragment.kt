package com.nelayanku.apps.tools

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.DialogFragment
import com.nelayanku.apps.R

class CustomBottomSheetDialogFragment(private val htmlContent: String) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bottom_sheet, container, false)

        val webView = view.findViewById<WebView>(R.id.webView)
        val webViewSettings = webView.settings
        webViewSettings.javaScriptEnabled = true
        webViewSettings.loadWithOverviewMode = true
        webViewSettings.useWideViewPort = true
        webView.loadDataWithBaseURL(null,htmlContent,"text/html","UTF-8",null)

        return view
    }
}

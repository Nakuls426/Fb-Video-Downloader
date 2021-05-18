package com.fb.downloader

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import com.fb.downloader.databinding.LoginActivityBinding
import kotlinx.android.synthetic.main.login_activity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.util.regex.Pattern
import javax.net.ssl.HttpsURLConnection

data class LoginData(var data: String = "", var name: String = "")

open class LoginActivity : AppCompatActivity() {
    private val TAG = "LoginActivity"
    private lateinit var binding: LoginActivityBinding

    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginActivityBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        facebookWeb()
        login_progress.visibility = View.GONE
    }

    @JavascriptInterface
    fun processVideo(sdURL: String, id: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val bg = object : Background() {
                override fun getURLs(file: HoldData) {
                    Log.d(TAG, file.hd)
                    Log.d(TAG, file.sd)
                    this@LoginActivity.runOnUiThread {
                        login_progress.visibility = View.VISIBLE
                        val intent = Intent(this@LoginActivity, BrowserActivity::class.java)
                        intent.putExtra("hd", file.hd)
                        intent.putExtra("sd", file.sd)
                        intent.putExtra("file", file.fileName)
                        startActivity(intent)
                        login_progress.visibility = View.GONE
                    }
                }
            }
            bg.getData(getResponse(id))
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun facebookWeb() {
        binding.webView.addJavascriptInterface(this, "LoginAct")
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (Uri.parse(url).host == "https://m.facebook.com/login/") {
                    return true
                }
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                Log.d(TAG, "URL : ${view?.url}")
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {

                view?.loadUrl("javascript:(function() { var el = document.querySelectorAll('div[data-sigil]');for(var i=0;i<el.length; i++){var sigil = el[i].dataset.sigil;if(sigil.indexOf('inlineVideo') > -1){delete el[i].dataset.sigil;var jsonData = JSON.parse(el[i].dataset.store);el[i].setAttribute('onClick', 'LoginAct.processVideo(\"'+jsonData['src']+'\");');}})}()")
                view?.evaluateJavascript("javascript:window.LoginAct.processHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');".trimMargin()) {
                    Log.d(TAG, it)
                }

                super.onPageFinished(view, url)
            }

            override fun onLoadResource(view: WebView?, url: String?) {
                view?.loadUrl("javascript:(function prepareVideo() { var el = document.querySelectorAll('div[data-sigil]');for(var i=0;i<el.length; i++){var sigil = el[i].dataset.sigil;if(sigil.indexOf('inlineVideo') > -1){delete el[i].dataset.sigil;console.log(i);var jsonData = JSON.parse(el[i].dataset.store);el[i].setAttribute('onClick', 'LoginAct.processVideo(\"'+jsonData['src']+'\",\"'+jsonData['videoID']+'\");');}}})()")
                login_progress.visibility = View.GONE
            }
        }
        binding.webView.loadUrl("https://m.facebook.com/login/")
    }

    private fun getResponse(videoId: String): String {
        val url1 = URL("https://www.facebook.com/video.php?v=$videoId")
        val connection: HttpsURLConnection = url1.openConnection() as HttpsURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty(
            "User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.114 Safari/537.36"
        )
        connection.setRequestProperty(
            "cookie",
            "datr=Pd6cYLFaavbDPO0Ga-wbeMoy; fr=1HhSF5VOCY6GCdOmf..BgnN49.lQ.AAA.0.0.BgnN5D.AWVFFOCsi4g"
        )

        val br = BufferedReader(InputStreamReader(connection.inputStream, "utf-8"))
        val builder = StringBuilder("")
        var line: String?
        val ld = LoginData()
        while (br.readLine().also { line = it } != null) {
            builder.append(line)
            ld.data = builder.toString()
        }

        val pattern = Pattern.compile("(<link rel=\"canonical\" href=\"(.+/\"))")
        val matcher = pattern.matcher(ld.data)
        if (matcher.find()) {
            val u =
                matcher.group(0).removePrefix("<link rel=\"canonical\" href=\"").split("\" />")[0]
            ld.name = u
            Log.d(TAG, "modified : $u")
        }
        return ld.name
    }

    private fun loginResponse(url: String) {
        val url1 = URL(url)
        val connection: HttpsURLConnection = url1.openConnection() as HttpsURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty(
            "User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.114 Safari/537.36"
        )
        connection.setRequestProperty(
            "cookie",
            "datr=bTdvYER7nyiLcpzzpWBA9lDr; locale=en_GB; fr=1XF9elHkx5lKqKpcf..Bgbzdt.X7.AAA.0.0.BgbzjA.AWUM7UGyHrE; wd=1366x157; fr=1XF9elHkx5lKqKpcf..Bgbzdt.X7.AAA.0.0.BgdH_R.AWXkL70WKvI"
        )
        val br = BufferedReader(InputStreamReader(connection.inputStream, "utf-8"))
        val builder = StringBuilder("")
        var line: String?
        val sources = HoldData()
        while (br.readLine().also { line = it } != null) {
            builder.append(line)
            sources.stream = builder.toString()
        }

        if (url.contains("https://fb.watch") || url.contains("videos") || url.contains("video")) {
            val sdPattern = Pattern.compile("(sd_src:(.+?\"))")
            val hdPattern = Pattern.compile("(hd_src:(.+?\"))")

            val matcher1 = sdPattern.matcher(sources.stream)
            val matcher = hdPattern.matcher(sources.stream)

            if (matcher1.find() && matcher.find()) {
                val low = matcher1.group(0)
                val high = matcher.group(0)
                val fileName = low?.substring(58)?.split("_n.mp4?")?.get(0)
                sources.fileName = fileName!!
                sources.hd = high?.removePrefix("hd_src:\"")?.removeSuffix("\"")!!
                sources.sd = low.removePrefix("sd_src:\"").removeSuffix("\"")
                Log.d(TAG, "hd: ${sources.hd}")
            } else {
                Log.d(TAG, "No match found.")
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && web_view.canGoBack()) {
            web_view.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}

/*@JavascriptInterface
    open fun processHTML(html: String?) {
        Log.d(TAG,html!!)

    }*/

//val pattern = Pattern.compile("(<meta http-equiv=\"refresh\" content=\"0; URL(.+/>?))")
//var u = matcher.group(0).removeSuffix("<meta http-equiv=\"refresh\" content=\"0; URL=").split("/?")[0]
//u = u.replace("<meta http-equiv=\"refresh\" content=\"0; URL=", "")
////(<link rel="canonical" href="(.+\/"))
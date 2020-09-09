package com.fb.downloader

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.Pattern

abstract class BackgroundActivity(context: Context, private var url: String, private var showLogs: Boolean) :
    AsyncTask<Void, Int, GetAndSet>() {
    private var userAgent =
        "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.115 Safari/537.36"
    private var reader: BufferedReader? = null
    private var httpURLConnection: HttpURLConnection? = null
    private var builder: StringBuilder = StringBuilder()
    private var exception: Exception? = null
    private val fbFile = GetAndSet()
    private var startTime:Long = 0

    init {
        if (showLogs)
            Log.e("Extract","Extraction started $startTime Ms")
        this.execute()
    }

    abstract fun onExtractionComplete(getAndSet: GetAndSet)
    abstract fun onExtractionFail(exception: Exception)

    private fun parseHtml(url: String): GetAndSet {
        try {
            val getUrl = URL(url)
            httpURLConnection = getUrl.openConnection() as HttpURLConnection
            httpURLConnection!!.setRequestProperty("User-Agent", userAgent)

            try {
                reader = BufferedReader(InputStreamReader(httpURLConnection!!.inputStream))
                var line: String
                while (reader!!.readLine().also { line = it } != null) {
                    builder.append(line)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                reader?.close()
                httpURLConnection!!.disconnect()
                onCancelled()
            }
        } finally {
            reader?.close()
            httpURLConnection?.disconnect()
        }
        if (builder.toString().contains("You must log-in to continue")) {
            exception = RuntimeException("You must log-in to continue")
        } else {
            val sdVideo = Pattern.compile("(sd_src):\"(.+?)\"")
            val sdVMatcher = sdVideo.matcher(builder)
            val hdVideo = Pattern.compile("(hd_src):\"(.+?)\"")
            val hdVMatcher = hdVideo.matcher(builder)

            fbFile.ext = ".mp4"

            if (sdVMatcher.find()) {
                var videoURL = sdVMatcher.group()
                videoURL = videoURL.substring(8, videoURL.length - 1)
                fbFile.sdUrl = videoURL
            } else fbFile.sdUrl = null.toString()
            if (hdVMatcher.find()) {
                var videoURL2 = hdVMatcher.group()
                videoURL2 = videoURL2.substring(8, videoURL2.length - 1)
                fbFile.hdUrl = videoURL2
            } else fbFile.hdUrl = null.toString()
            if (fbFile.hdUrl == null && fbFile.sdUrl == null) {
                exception = RuntimeException("Url not valid")
            }

        }
        return fbFile
    }

    override fun doInBackground(vararg p0: Void?): GetAndSet {
        return parseHtml(url)
    }

    override fun onPostExecute(result: GetAndSet?) {
        super.onPostExecute(result)
        if (showLogs)
            Log.e("FacebookAnother","Extraction time taken ${System.currentTimeMillis()-startTime} Ms")
        if (result != null)
            onExtractionComplete(result)
        else
            exception?.let { onExtractionFail(it) }
    }
}
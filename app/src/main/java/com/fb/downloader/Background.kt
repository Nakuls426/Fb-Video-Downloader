package com.fb.downloader

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.net.URL
import java.util.regex.Pattern
import javax.net.ssl.HttpsURLConnection

data class HoldData(var stream:String = "", var hd:String = "", var sd:String = "" ,var fileName:String = "")

abstract class Background  {

    abstract fun getURLs(file:HoldData)

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun getResponse (url: String) : HoldData{
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
        val br = BufferedReader(InputStreamReader(connection.inputStream,"utf-8"))
        val builder = StringBuilder("")
        var line:String?
        val sources = HoldData()
        while (br.readLine().also { line = it } != null) {
            builder.append(line)
            sources.stream = builder.toString()
//            logDebug(line.toString())
        }

        if (url.contains("https://fb.watch") || url.contains("videos")) {
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
                logDebug(sources.fileName)
            } else
                logDebug("No match found.")
        }
        getURLs(sources)
        return sources
    }

    suspend fun getData(u:String):HoldData = getResponse(u)

    open fun logDebug(msg:String) = Log.d("Background",msg)

}
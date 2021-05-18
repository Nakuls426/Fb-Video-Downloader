package com.fb.downloader

import android.app.DownloadManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.MediaController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fb.downloader.databinding.VideoPreviewBinding
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import kotlin.properties.Delegates

open class BrowserActivity : AppCompatActivity() {
    private lateinit var browserBinding: VideoPreviewBinding
    private lateinit var mediaController: MediaController
    private var videoId by Delegates.notNull<Long>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        browserBinding = VideoPreviewBinding.inflate(layoutInflater)
        val view = browserBinding.root
        setContentView(view)

        val low = intent.getStringExtra("sd")
        val high = intent.getStringExtra("hd")
        val file = intent.getStringExtra("file")

        Log.d("BrowserActivity", file!!)
        Log.d("BrowserActivity", high!!)
        Log.d("BrowserActivity", low!!)
        playVideo(low)

        browserBinding.browserSd.setOnClickListener {
            download(low, file)
        }

        browserBinding.browserHd.setOnClickListener {
            if (high == "hd_src:null,sd_src:") {
                Toast.makeText(this@BrowserActivity, "Not available", Toast.LENGTH_SHORT).show()
            } else
                download(high, file)
        }
    }

    private fun playVideo(source: String?) {
        mediaController = MediaController(this)
        browserBinding.browserVideoPreview.setVideoURI(Uri.parse(source))
        mediaController.setAnchorView(video_view)
        browserBinding.browserVideoPreview.setMediaController(mediaController)
        browserBinding.browserVideoPreview.start()
        if (browserBinding.browserVideoPreview.isPlaying) browserBinding.browserVideoPreview.stopPlayback()

    }


    private fun download(url: String, videoName: String) {
        val videoTitle = "$videoName.mp4"
        val createD = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "")
        val file = File(createD, videoTitle)

        try {
            val request = DownloadManager.Request(Uri.parse(url)).also {
                it.allowScanningByMediaScanner()
                it.setTitle(videoTitle)
                    .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
                    .setDestinationUri(Uri.fromFile(file))
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setNotificationVisibility(View.VISIBLE)
                    .setTitle(videoTitle)
            }
            val downloadManager: DownloadManager =
                this@BrowserActivity.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            videoId = downloadManager.enqueue(request)
            if (videoId > 0) Toast.makeText(this@BrowserActivity, "Downloading", Toast.LENGTH_LONG)
                .show()
            else Toast.makeText(this@BrowserActivity, "Failed to download", Toast.LENGTH_LONG)
                .show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
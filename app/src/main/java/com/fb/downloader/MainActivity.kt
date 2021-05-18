package com.fb.downloader

import android.app.DownloadManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.MediaController
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import com.fb.downloader.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var videoId: Long = 0
    private lateinit var mediaController: MediaController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        progress_Bar.visibility = View.GONE
        sd_quality_btn.visibility = View.GONE
        hd_quality_btn.visibility = View.GONE

        login_with_facebook.setOnClickListener {
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
        }
        binding.searchingBtn.setOnClickListener {
            progress_Bar.visibility = View.VISIBLE
            if (binding.searchBar1.text.startsWith("https://fb.watch/") ||
                binding.searchBar1.text.contains("videos")
            ) {
                val url = search_bar1.text.toString()

                CoroutineScope(Dispatchers.IO).launch {
                    val bg = object : Background() {
                        override fun getURLs(file: HoldData) {
                            this@MainActivity.runOnUiThread {
                                playVideo(file.sd)
                                Log.d("MainActivity", file.hd)
                                Log.d("MainActivity", file.sd)
                                hd_quality_btn.visibility = View.VISIBLE
                                sd_quality_btn.visibility = View.VISIBLE

                                hd_quality_btn.setOnClickListener {
                                    if (file.hd == "hd_src:null,sd_src:") {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Not available",
                                            LENGTH_SHORT
                                        ).show()
                                    } else
                                        downloadHDVideo(file.hd, file.fileName)
                                }

                                sd_quality_btn.setOnClickListener {
                                    downloadHDVideo(file.sd, file.fileName)

                                }
                                progress_Bar.visibility = View.GONE
                            }
                        }
                    }
                    bg.getData(url)
                }

            } else if (search_bar1.text.isEmpty()) {
                progress_Bar.visibility = View.GONE
                Toast.makeText(this, "Paste url", LENGTH_SHORT).show()
            }
        }
    }

    private fun downloadHDVideo(url: String, videoName: String) {
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
                this@MainActivity.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            videoId = downloadManager.enqueue(request)
            if (videoId > 0) Toast.makeText(this@MainActivity, "Downloading", LENGTH_LONG).show()
            else Toast.makeText(this@MainActivity, "Failed to download", LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun playVideo(source: String?) {
        mediaController = MediaController(this)
        binding.videoView.setVideoURI(Uri.parse(source))
        mediaController.setAnchorView(video_view)
        binding.videoView.setMediaController(mediaController)
        binding.videoView.start()
        if (binding.videoView.isPlaying) binding.videoView.stopPlayback()

    }
}